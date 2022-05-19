package cj.jukebox.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.settings() {
    routing {
        route("/settings") {
            get {
                this.call.respondText("settings")
            }
            post {
                println("post settings")
                this.call.respondRedirect("settings")
            }
        }
    }
}