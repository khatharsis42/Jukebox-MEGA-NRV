package cj.jukebox.plugins.nav

import cj.jukebox.utils.UserSession
import cj.jukebox.utils.getUserSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.io.File

fun Application.nav() {
    routing {
        authenticate("auth-session") {
            get("/") {
                call.respondRedirect("/app")
            }

            get("/app") {
                val user = call.sessions.get<UserSession>()!!
                call.respondHtmlTemplate(App(user)) {}
            }

            get("/help") {
                val user = call.getUserSession()!!
                call.respondHtmlTemplate(Help(user)) {}
            }

            get("/status") {
                // TODO: proper reply
                call.respondText("status")
            }
        }

        static("/assets") {
            staticRootFolder = File("src/main/resources")
            files(".")
        }
    }
}
