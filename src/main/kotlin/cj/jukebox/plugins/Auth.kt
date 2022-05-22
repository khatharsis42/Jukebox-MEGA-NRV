package cj.jukebox.plugins

import cj.jukebox.config
import cj.jukebox.database
import cj.jukebox.database.User
import cj.jukebox.database.Users

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

import org.jetbrains.exposed.sql.and

import templates.*

data class UserSession(val name: String, val theme: String?) : Principal

fun Application.auth() {
    install(Sessions) {
        cookie<UserSession>("user-session") {
            val secretEncryptKey = hex(config.data.SECRET_ENCRYPT_KEY)
            val secretSignKey = hex(config.data.SECRET_SIGN_KEY)
            cookie.path = "/"
            cookie.maxAgeInSeconds = 300
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }

    authentication {
        session<UserSession>("auth-session") {
            validate { sessions.get<UserSession>() }
            challenge("auth")
        }
    }

    routing {
        route("/auth") {
            get {
                if (call.authentication.principal<UserSession>() != null) {
                    call.respondRedirect("app")
                } else {
                    call.respondHtmlTemplate(Auth()) {}
                }
            }

            post {
                val parameters = call.receiveParameters()

                val userName = parameters["user"].toString()
                val password = parameters["pass"].toString()

                when(parameters["action"].toString()) {
                    "login" -> {
                        val res = database.dbQuery {
                            User
                                .find { (Users.name eq userName) and (Users.pass eq password) }
                                .limit(1).toList()
                        }
                        if (res.isNotEmpty()) {
                            val user = res.first()
                            call.sessions.set(UserSession(name = user.name, theme = user.theme))
                            UserIdPrincipal(userName)
                            call.respondRedirect("app")
                        } else {
                            call.respondRedirect("auth")
                        }
                    }

                    "signup" -> {
                        val res = database.dbQuery {
                            User.find { Users.name eq userName }.limit(1).toList()
                        }
                        if (res.isEmpty()) {
                            database.dbQuery {
                                User.new {
                                    name = userName
                                    pass = password
                                }
                            }
                            call.sessions.set(UserSession(name = userName, theme = null))
                            UserIdPrincipal(userName)
                            call.respondRedirect("app")
                        } else {
                            call.respondRedirect("auth")
                        }
                    }

                    else -> call.respondRedirect("auth")
                }
            }
        }

        authenticate("auth-session") {
            route("/logout") {
                get {
                    val userName = call.sessions.get<UserSession>()!!.name
                    call.respondHtmlTemplate(Logout(userName)) {}
                }
                post {
                    call.sessions.clear<UserSession>()
                    call.respondRedirect("auth")
                }
            }
        }
    }
}
