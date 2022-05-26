package cj.jukebox.plugins.statistics

import cj.jukebox.database
import cj.jukebox.database.*
import cj.jukebox.utils.getParam
import cj.jukebox.utils.getUserSession

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Module de statistiques du jukebox.
 * Gère les statistiques globales ou spécifiques à un·e [User], une [Track]
 * ainsi que l'historique des dernières [Track].
 * @author Ukabi
 */
fun Application.statistics() {
    routing {
        authenticate("auth-session") {
            route("/statistics") {
                get {
                    val user = call.getUserSession()!!
                    call.respondHtmlTemplate(GlobalStatistics(user)) {}
                }

                get("/user/{username}") {
                    val user = call.getUserSession()!!

                    val username = call.getParam("username")
                    val res = database.dbQuery {
                        User.find { Users.name eq username }
                            .limit(1).toList()
                    }
                    if (res.isNotEmpty()) {
                        val lookedUpUser = res.first()
                        call.respondHtmlTemplate(UserStatistics(user, lookedUpUser)) {}
                    } else {
                        // TODO: proper redirection
                        call.respondText("Invalid username")
                    }
                }

                get("/track/{track}") {
                    val user = call.getUserSession()!!

                    val trackId = call.getParam("track").toInt()
                    val track = database.dbQuery { Track.findById(trackId) }
                    if (track != null) {
                        call.respondHtmlTemplate(TrackStatistics(user, track)) {}
                    } else {
                        // TODO: proper redirection
                        call.respondText("Invalid track !")
                    }
                }
            }
            route("/history") {
                get("/{count}") {
                    val user = call.getUserSession()!!

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

fun giveTestArray() = arrayOf(
    arrayOf("Name 1", "Name 2").toList(),
    arrayOf("Test1", "1").toList(),
    arrayOf("Test2", "6").toList(),
    arrayOf("Test3", "9").toList()
).toList()