package cj.jukebox.plugins

import cj.jukebox.templates.Help
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import cj.jukebox.templates.Accueil
import cj.jukebox.utils.getUserSession
import io.ktor.server.auth.*
import java.io.File

fun Application.routing() {
    routing {
        authenticate("auth-session") {
            get("/") {
                call.respondRedirect("app")
            }

            get("/app") {
                val user = call.getUserSession()!!.user
                call.respondHtmlTemplate(Accueil(user)) {}
            }

            get("/help") {
                val user = call.getUserSession()!!.user
                call.respondHtmlTemplate(Help(user)) {}
            }

            get("/status") {
                call.respondText("status")
            }
        }

        static("/assets") {
            staticRootFolder = File("src/main/resources")
            files(".")
        }
    }
}
