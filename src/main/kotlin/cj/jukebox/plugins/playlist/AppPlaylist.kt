package cj.jukebox.plugins.playlist

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.playlist() {
    routing {
        authenticate("auth-session") {
            post("/add") {

            }

            post("/add/{indent}") {

            }

            post("/remove") {

            }

            post("/move-track") {

            }

            post("/sync") {

            }

            post("/suggest") {

            }
        }
    }
}