package cj.jukebox.plugins

import cj.jukebox.database
import cj.jukebox.templates.Settings
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
                    call.respondHtmlTemplate(Settings(call.getUserSession()!!.user)) {}
                }
                post {
                    val parameters = call.receiveParameters()
                    val style = parameters["style"]

                    val session = call.getUserSession()!!
                    database.dbQuery { session.user.theme = style }

                    call.respondRedirect("settings")
                }
            }
        }
    }
}