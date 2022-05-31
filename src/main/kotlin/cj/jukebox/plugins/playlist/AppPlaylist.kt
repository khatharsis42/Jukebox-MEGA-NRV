package cj.jukebox.plugins.playlist

import cj.jukebox.player
import cj.jukebox.playlist
import cj.jukebox.utils.getParam
import cj.jukebox.utils.getUserSession
import cj.jukebox.utils.sendSignal

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.playlist() {
    routing {
        authenticate("auth-session") {
            post("/add") {
                TODO("waiting for progress in /search before going further")
            }

            // TODO: this is just for tests, but don't forget to make this post after
            get("/add/{id}") {
                val session = call.getUserSession()!!

                val trackId = call.getParam("id").toInt()
                playlist.addIfPossible(trackId, session)

                player.sendSignal(SigName.SIGUSR2)
            }

            post("/remove") {
                val parameters = call.receiveParameters()

                val trackId = parameters.getOrFail("randomid").toInt()
                playlist.removeIfPossible(trackId)
            }

            post("/move-track") {
                val parameters = call.receiveParameters()

                val direction = parameters.getOrFail("action")
                val trackId = parameters.getOrFail("randomid").toInt()
                playlist.map { it.trackId.id.value }.indexOf(trackId).takeIf { it >= 0 }
                    ?.let { playlist.move(it, Direction.valueOf(direction)) }
            }

            post("/sync") {
                TODO("front end stuff for @Khatharsis")
            }

            post("/suggest") {
                TODO("front end stuff for @Khatharsis")
            }
        }
    }
}