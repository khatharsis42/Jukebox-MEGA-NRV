package cj.jukebox.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*

fun Application.routing() {
    routing {
        get("/") {
            call.respondRedirect("app")
        }

        get("/app") {
            call.respondText("app")
        }

        get("/help") {
            call.respondText("help")
        }

        get("/status") {
            call.respondText("status")
        }
    }
}
