package cj.jukebox.plugins

import cj.jukebox.templates.Help
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import templates.Accueil
import java.io.File

fun Application.routing() {
    routing {
        get("/") {
            call.respondRedirect("app")
        }

        get("/app") {
            call.respondHtmlTemplate(Accueil("Test")) {}
        }

        get("/help") {
            call.respondHtmlTemplate(Help("Test")) {}
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
