package cj.jukebox.plugins.playlist

import cj.jukebox.database.Track
import cj.jukebox.database.TrackData
import cj.jukebox.playlist
import cj.jukebox.utils.getParam
import cj.jukebox.utils.getUserSession

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.playlist() {
    routing {
        authenticate("auth-session") {
            post("/add") {
                val parameters = call.receiveParameters()
                (Track.refresh(parameters["url"]!!) ?: Track.createTrack(
                    TrackData(
                        url = parameters["url"]!!,
                        source = parameters["source"]!!,
                        track = parameters["track"],
                        artist = parameters["artist"],
                        album = parameters["album"],
                        albumArtUrl = parameters["albumArtUrl"],
                        duration = Integer.parseInt(parameters["duration"]),
                        blacklisted = parameters["blacklisted"] == "true",
                        obsolete = parameters["obsolete"] == "true"
                    )
                )).also { playlist.addIfPossible(it, call.getUserSession()!!) }
            }

            post("/add/{url}") {
                val session = call.getUserSession()!!
                val trackUrl = call.getParam("url")
                Track.getFromUrl(trackUrl)?.also { playlist.addIfPossible(it, session) }
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