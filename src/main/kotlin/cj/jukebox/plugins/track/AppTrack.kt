package cj.jukebox.plugins.track

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.track() {
    routing {
        authenticate("auth-session") {
            post("/pause-play") {

            }

            post("/change-volume") {

            }

            post("/rewind-play") {

            }

            post("/forward-play") {

            }

            post("/jump-play") {

            }
        }
    }
}