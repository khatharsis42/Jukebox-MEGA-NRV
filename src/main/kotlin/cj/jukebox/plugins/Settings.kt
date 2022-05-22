package cj.jukebox.plugins

import cj.jukebox.templates.Settings
import cj.jukebox.utils.getUserSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.settings() {
    routing {
        authenticate("auth-session") {
            route("/settings") {
                get {
                    call.respondHtmlTemplate(Settings(call.getUserSession()!!.user.name)) {}
                }
                post {
                    val parameters = call.receiveParameters()
                    val style = parameters["style"]

                    val session = call.getUserSession()!!
                    transaction { session.user.theme = style }

                    call.respondRedirect("settings")
                }
            }
        }
    }
}