package cj.jukebox.plugins.settings

import cj.jukebox.database.User
import cj.jukebox.utils.getUserSession

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.settings() {
    routing {
        authenticate("auth-session") {
            route("/settings") {
                get {
                    call.respondHtmlTemplate(Settings(call.getUserSession()!!)) {}
                }
                post {
                    val parameters = call.receiveParameters()
                    val style = parameters["style"]

                    val userSession = call.getUserSession()!!
                    userSession.theme = style
                    User.findUser(userSession.id)?.setTheme(style)

                    call.respondRedirect("settings")
                }
            }
        }
    }
}