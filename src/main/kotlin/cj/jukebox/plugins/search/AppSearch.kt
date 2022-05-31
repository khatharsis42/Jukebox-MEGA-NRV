package cj.jukebox.plugins.search

import cj.jukebox.database.Track
import cj.jukebox.database.TrackData

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

fun Application.search() {
    routing {
        authenticate("auth-session") {
            post("/search") {
                val parameters = call.receiveParameters()

                val query = parameters.getOrFail("q").takeIf { it.isNotBlank() } ?: return@post

                val trackList: List<TrackData>
                for (engine in SearchEngine.values()) {
                    if (query.matches(engine.urlRegex)) {
                        println("Matching URL for ${engine.name} : $query")

                        trackList = engine.downloadSingle(query)
                        if (trackList.size == 1) {
                            // TODO Jouer cette unique track
                        }
                        call.respond(Json.encodeToString(ListSerializer(TrackData.serializer()), trackList))
                        return@post
                    }

                    if (engine.queryRegex.let { (it != null) && query.matches(it) }) {
                        println("Matching query for ${engine.name} : $query")

                        trackList = engine.downloadMultiple(query)
                        call.respond(Json.encodeToString(ListSerializer(TrackData.serializer()), trackList))
                        return@post
                    }
                }

                println("Matching nothing, using generic Youtube search.")

                trackList = SearchEngine.YOUTUBE.downloadMultiple(query)
                call.respond(Json.encodeToString(ListSerializer(TrackData.serializer()), trackList))
            }

            post("/refresh-track") {
                val parameters = call.request.queryParameters

                val url = parameters.getOrFail("url").takeIf { it.isNotBlank() } ?: return@post
                Track.refresh(url)
            }
        }
    }
}