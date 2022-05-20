package cj.jukebox.plugins

import cj.jukebox.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

import kotlinx.html.*
import templates.Auth
import templates.Logout

data class UserSession(val name: String) : Principal

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
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == "jetbrains" && credentials.password == "foobar") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        session<UserSession>("auth-session") {
            validate { session ->
                if(session.name.startsWith("jet")) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/auth")
            }
        }
    }

    routing {
        route("/auth") {
            get {
                call.respondHtmlTemplate(Auth()) {}
            }

            authenticate("auth-form") {
                post {
                    val userName = call.principal<UserIdPrincipal>()?.name.toString()
                    call.sessions.set(UserSession(name = userName))
                    call.respondText("${call.sessions.get<UserSession>()?.name}")
                }
            }
        }

        get("/logout") {
            call.respondHtmlTemplate(Logout("Test")) {}
        }
        post("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("auth")
        }
    }
}
