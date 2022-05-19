package cj.jukebox.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*

fun Application.configureRouting() {
    routing {
        get("/") {
            this.call.respondRedirect("app")
        }

        get("/app") {
            this.call.respondText("app")
        }

        get("/help") {
            this.call.respondText("help")
        }

        get("/status") {
            this.call.respondText("status")
        }
    }
}
