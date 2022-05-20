package cj.jukebox.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import templates.Acceuil
import java.io.File

fun Application.routing() {
    routing {
        get("/") {
            call.respondRedirect("app")
        }

        get("/app") {
            call.respondHtmlTemplate(Acceuil("Test")) {}
        }

        get("/help") {
            call.respondText("help")
        }

        get("/status") {
            call.respondText("status")
        }

        static("/assets") {
            staticRootFolder = File("src/main/resources")
            files(".")
        }
    }
}
