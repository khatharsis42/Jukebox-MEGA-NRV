package cj.jukebox.plugins

import cj.jukebox.templates.Settings
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.settings() {
    routing {
        route("/settings") {
            get {
                call.respondHtmlTemplate(Settings("Test")) {}
            }
            post {
                println("post settings")
                call.respondRedirect("settings")
            }
        }
    }
}