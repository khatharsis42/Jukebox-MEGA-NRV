package cj.jukebox.plugins

import cj.jukebox.database
import cj.jukebox.database.*
import cj.jukebox.templates.*
import cj.jukebox.utils.getParam
import cj.jukebox.utils.getUserSession

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.statistics() {
    routing {
        authenticate("auth-session") {
            route("/statistics") {
                get {
                    val user = call.getUserSession()!!.user
                    call.respondHtmlTemplate(GlobalStatistics(user)) {}
                }

                get("/user/{username}") {
                    val user = call.getUserSession()!!.user

                    val username = call.getParam("username")
                    val res = database.dbQuery {
                        User
                            .find { Users.name eq username }
                            .limit(1).toList()
                    }
                    if (res.isNotEmpty()) {
                        val lookedUpUser = res.first()
                        call.respondHtmlTemplate(UserStatistics(user, lookedUpUser)) {}
                    } else {
                        call.respondText("Invalid username")
                    }
                }

                get("/song/{song}") {
                    val user = call.getUserSession()!!.user

                    val songId = call.getParam("song").toInt()
                    val song = database.dbQuery {
                        Song.findById(songId)
                    }
                    if (song != null) {
                        call.respondHtmlTemplate(SongStatistics(user, song)) {}
                    } else {
                        call.respondText("Invalid song")
                    }
                }
            }
            route("/history") {
                get("/{count}") {
                    val user = call.getUserSession()!!.user

                    val count = call.getParam("count").toInt()
                    call.respondHtmlTemplate(History(user, count)) {}
                }
                get {
                    call.respondRedirect("/history/50")
                }
            }
        }
    }
}