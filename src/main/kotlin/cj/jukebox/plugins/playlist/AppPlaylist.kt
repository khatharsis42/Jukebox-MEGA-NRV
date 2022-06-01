package cj.jukebox.plugins.playlist

import cj.jukebox.database.Track
import cj.jukebox.database.TrackData
import cj.jukebox.playlist
import cj.jukebox.utils.Loggers
import cj.jukebox.utils.getParam
import cj.jukebox.utils.getUserSession

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

fun Application.playlist() {
    routing {
        authenticate("auth-session") {
            post("/add") {
                val parameters = call.receiveParameters()
                if (playlist.addIfPossible(TrackData(parameters, call.getUserSession()!!)))
                    call.respond("ok")
                else call.respond("nok")
            }

            post("/add/{url}") {
                val session = call.getUserSession()!!
                val trackUrl = call.getParam("url")
                Track.getFromUrl(trackUrl)?.also { playlist.addIfPossible(TrackData(it, session.toUser())) }
            }

            post("/remove") {
                val parameters = call.receiveParameters()
                val trackId = parameters.getOrFail("randomid").toInt()
                playlist.removeIfPossible(trackId)
            }

            post("/move-track") {
                val parameters = call.receiveParameters()
                val direction = parameters.getOrFail("action")
                val trackId = Integer.parseInt(parameters.getOrFail("randomid"))
                playlist
                    .map { it.randomid }
                    .indexOf(trackId)
                    .takeIf { it >= 0 }
                    ?.also { playlist.move(it, Direction.valueOf(direction)) }
            }

            get("/sync") {
                call.respond(
                    Json.encodeToString(
                        JsonObject(
                            mapOf(
                                "playlist" to Json.encodeToJsonElement(ListSerializer(TrackData.serializer()), playlist),
                                "volume" to Json.encodeToJsonElement(1),
                                "time" to Json.encodeToJsonElement(1),
                                "playlistLength" to Json.encodeToJsonElement(playlist.duration())
                            )
                        )
                    )
                )
            }

            get("/suggest") {
                TODO("front end stuff for @Khatharsis")
            }
        }
    }
}