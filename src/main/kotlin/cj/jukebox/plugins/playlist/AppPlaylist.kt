package cj.jukebox.plugins.playlist

import cj.jukebox.database.Track
import cj.jukebox.database.TrackData
import cj.jukebox.playlist
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
                val session = call.getUserSession()!!

                val added = playlist.addIfPossible(TrackData(parameters, session))

                if (added)
                    call.respond("ok")
                else
                    call.respond("nok")
            }

            post("/add/{url}") {
                val session = call.getUserSession()!!

                val added = call.getParam("url")
                    .let { Track.getFromUrl(it) }
                    ?.let { playlist.addIfPossible(TrackData(it, session)) }
                    ?.takeIf { it }  // effectively added if track exists (1st let) AND track added (2nd let)

                added?.also { call.respond("ok") } ?: call.respond("nok")
            }

            post("/remove") {
                val parameters = call.receiveParameters()

                val removed = parameters.getOrFail("randomid").toInt()
                    .let { playlist.removeIfPossible(it) }

                removed?.also { call.respond("ok") } ?: call.respond("nok")
            }

            post("/move-track") {
                val parameters = call.receiveParameters()

                val direction = parameters.getOrFail("action")
                val trackId = parameters.getOrFail("randomid").toInt()
                val trackIndex = playlist.map { it.randomid }.indexOf(trackId).takeIf { it >= 0 }
                val moved = trackIndex?.let { playlist.move(it, Direction.valueOf(direction)) }

                moved?.also { call.respond("ok") } ?: call.respond("nok")
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