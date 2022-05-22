package cj.jukebox.plugins

import cj.jukebox.database.*
import cj.jukebox.templates.*
import cj.jukebox.utils.getParam

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import org.jetbrains.exposed.dao.id.EntityID

fun Application.statistics() {
    routing {
        authenticate("auth-session") {
            route("/statistics") {
                get {
                    call.respondHtmlTemplate(GlobalStatistics("test")) {}
                }

                get("/user/{username}") {
                    val username = call.getParam("username")
                    call.respondHtmlTemplate(UserStatistics("test", User(EntityID(0, Users)))) {}
                }

                get("/song/{song}") {
                    val song = call.getParam("song").toInt()
                    call.respondHtmlTemplate(SongStatistics("test", Song(EntityID(0, Songs)))) {}
                }
            }
            route("/history") {
                get("/{count}") {
                    val count = call.getParam("count").toInt()
                    call.respondHtmlTemplate(History("test", count)) {}
                }
                get {
                    call.respondRedirect("/history/50")
                }
            }
        }
    }
}