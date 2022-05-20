package cj.jukebox.plugins

import cj.jukebox.database.Song
import cj.jukebox.database.Songs
import cj.jukebox.database.User
import cj.jukebox.database.Users
import cj.jukebox.templates.GlobalStatistics
import cj.jukebox.templates.History
import cj.jukebox.templates.TrackStatistics
import cj.jukebox.templates.UserStatistics
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.dao.id.EntityID

fun Application.statistics() {
    routing {
        route("/statistics") {
            get {
                call.respondHtmlTemplate(GlobalStatistics("test")) {}
            }

            get("/user/{username}") {
                val username = call.parameters.getOrFail("username")
                call.respondHtmlTemplate(UserStatistics("test", User(EntityID(0, Users)))) {}
            }

            get("/track/{track}") {
                val track = call.parameters.getOrFail("track").toInt()
                call.respondHtmlTemplate(TrackStatistics("test", Song(EntityID(0, Songs)))) {}
            }
        }
        route("/history") {
            get("/{count}") {
                val count = this.call.parameters.getOrFail("count").toInt()
                call.respondHtmlTemplate(History("test", count)) {}
            }
            get {
                call.respondRedirect("/history/50")
            }
        }
    }
}