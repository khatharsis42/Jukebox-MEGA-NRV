package cj.jukebox.plugins

import cj.jukebox.config
import cj.jukebox.database
import cj.jukebox.database.*
import cj.jukebox.templates.*
import cj.jukebox.utils.*

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

import org.jetbrains.exposed.sql.and

fun Application.auth() {
    authentication {
        form("auth-login") {
            userParamName = "user"
            passwordParamName = "pass"

            validate { credentials ->
                val res = database.dbQuery {
                    User.find { (Users.name eq credentials.name) and (Users.pass eq credentials.password) }
                        .limit(1).toList()
                }
                if (res.isNotEmpty()) {
                    val user = res.first()
                    sessions.setUserSession(user.id, user.name, user.theme)
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge("auth?failed=true")
        }

        form("auth-signup") {
            userParamName = "user"
            passwordParamName = "pass"

            validate { credentials ->
                val res = database.dbQuery {
                    User.find { Users.name eq credentials.name }.limit(1).toList()
                }
                if (res.isEmpty()) {
                    val user = database.dbQuery {
                        User.new {
                            name = credentials.name
                            pass = credentials.password
                        }
                    }
                    sessions.setUserSession(user.id, user.name, user.theme)
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge("auth?failed=true")
        }

        session<UserSession>("auth-session") {
            validate { getUserSession() }
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
                if (call.getUserSession() != null) {
                    call.respondRedirect("app")
                } else {
                    val correct = call.request.queryParameters["failed"] != "true"
                    call.respondHtmlTemplate(Auth(correct)) {}
                }
            }
        }

        authenticate("auth-login") {
            post("/login") {
                call.respondRedirect("/app")
            }
        }

        authenticate("auth-signup") {
            post("/signup") {
                call.respondRedirect("/app")
            }
        }

        authenticate("auth-session") {
            route("/logout") {
                get {
                    val user = call.getUserSession()!!
                    call.respondHtmlTemplate(Logout(user)) {}
                }
                post {
                    call.clearUserSession()
                    call.respondRedirect("/auth")
                }
            }
        }
    }
}
