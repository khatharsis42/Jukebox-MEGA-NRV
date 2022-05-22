package cj.jukebox.plugins

import cj.jukebox.config
import cj.jukebox.database
import cj.jukebox.database.User
import cj.jukebox.database.Users

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

import org.jetbrains.exposed.sql.and

import templates.*

data class UserSession(val name: String, val theme: String?) : Principal

fun Application.auth() {
    authentication {
        form("auth-login") {
            userParamName = "user"
            passwordParamName = "pass"

            validate { credentials ->
                val res = database.dbQuery {
                    User
                        .find { (Users.name eq credentials.name) and (Users.pass eq credentials.password) }
                        .limit(1).toList()
                }
                if (res.isNotEmpty()) {
                    val user = res.first()
                    sessions.set(UserSession(name = user.name, theme = user.theme))
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge("auth")
        }

        form("auth-signup") {
            userParamName = "user"
            passwordParamName = "pass"

            validate { credentials ->
                val res = database.dbQuery {
                    User.find { Users.name eq credentials.name }.limit(1).toList()
                }
                if (res.isEmpty()) {
                    database.dbQuery {
                        User.new {
                            name = credentials.name
                            pass = credentials.password
                        }
                    }
                    sessions.set(UserSession(name = credentials.name, theme = null))
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge("auth")
        }

        session<UserSession>("auth-session") {
            validate { sessions.get<UserSession>() }
            challenge("auth")
        }
    }

    install(Sessions) {
        cookie<UserSession>("user-session") {
            val secretEncryptKey = hex(config.data.SECRET_ENCRYPT_KEY)
            val secretSignKey = hex(config.data.SECRET_SIGN_KEY)
            cookie.path = "/"
            cookie.maxAgeInSeconds = 300
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }

    routing {
        route("/auth") {
            get {
                if (call.sessions.get<UserSession>() != null) {
                    call.respondRedirect("app")
                } else {
                    call.respondHtmlTemplate(Auth()) {}
                }
            }
        }

        authenticate("auth-login") {
            post("/login") {
                call.respondRedirect("app")
            }
        }

        authenticate("auth-signup") {
            post("/signup") {
                call.respondRedirect("app")
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
