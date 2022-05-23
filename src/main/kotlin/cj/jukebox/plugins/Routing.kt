package cj.jukebox.plugins

import cj.jukebox.search.Youtube
import cj.jukebox.templates.Help
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
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

        post("/search") {
            val parameters = call.receiveParameters()
            val query = parameters["q" ]
            if (query != null && query.isNotBlank())
            Youtube().downloadSingle(query)
        }

        static("/assets") {
            staticRootFolder = File("src/main/resources")
            files(".")
        }
    }
}
