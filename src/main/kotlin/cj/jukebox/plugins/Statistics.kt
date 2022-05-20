package cj.jukebox.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.statistics() {
    routing {
        route("/statistics") {
            get {
                call.respondText("statistics")
            }

            get("/user/{username}") {
                val username = call.parameters.getOrFail("username")
                call.respondText("stat user $username")
            }

            get("/track/{track}") {
                val track = call.parameters.getOrFail("track").toInt()
                call.respondText("stat track $track")
            }
        }
        route("/history") {
            get("/{count}") {
                val count = this.call.parameters.getOrFail("count").toInt()
                this.call.respondText("history $count")
            }

            get("") {
                call.respondRedirect("history/50")
            }
        }


    }
}