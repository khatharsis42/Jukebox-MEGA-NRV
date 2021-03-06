package cj.jukebox.plugins.auth

import cj.jukebox.config
import cj.jukebox.database.User
import cj.jukebox.utils.UserSession
import cj.jukebox.utils.clearUserSession
import cj.jukebox.utils.getUserSession
import cj.jukebox.utils.setUserSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

/**
 * Module d'authentification du jukebox.
 * Gestion du login, logout, sign-in et de la session, ainsi que toutes leurs exceptions.
 * @author Ukabi
 */
fun Application.auth() {
    authentication {
        form("auth-login") {
            userParamName = "user"
            passwordParamName = "pass"

            validate { credentials ->
                val user = User.findUser(credentials.name, credentials.password) ?: return@validate null

                sessions.setUserSession(user.id, user.name, user.theme)
                UserIdPrincipal(credentials.name)
            }
            challenge("/auth?failed=true")
        }

        form("auth-signup") {
            userParamName = "user"
            passwordParamName = "pass"

            validate { credentials ->
                User.findUser(credentials.name)?.let { return@validate null }

                val user = User.createUser(credentials.name, credentials.password)

                sessions.setUserSession(user.id, user.name, user.theme)
                UserIdPrincipal(credentials.name)
            }
            challenge("/auth?failed=true")
        }

        session<UserSession>("auth-session") {
            validate { getUserSession() }
            challenge("/auth")
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
                call.getUserSession() ?: run {
                    val correct = call.request.queryParameters["failed"] != "true"
                    call.respondHtmlTemplate(Auth(correct)) {}
                    return@get
                }
                call.respondRedirect("/app")
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