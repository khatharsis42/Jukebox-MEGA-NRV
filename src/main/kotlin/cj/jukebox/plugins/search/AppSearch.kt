package cj.jukebox.plugins.search

import cj.jukebox.database.Track
import cj.jukebox.database.TrackData
import cj.jukebox.playlist
import cj.jukebox.utils.Loggers
import cj.jukebox.utils.getUserSession
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
                val session = call.getUserSession()!!
                val parameters = call.receiveParameters()

                val query = parameters.getOrFail("q").takeIf { it.isNotBlank() } ?: return@post

                val trackList: List<TrackData>
                for (engine in SearchEngine.values()) {
                    if (query.matches(engine.urlRegex)) {
                        Loggers.GEN.info("Matching URL for ${engine.name} : $query")

                        trackList = engine.downloadSingle(query)
                        if (trackList.size == 1) {
                            trackList.first()
                                .let { Track.refresh(it.url) ?: Track.createTrack(it) }
                                .also { playlist.addIfPossible(TrackData(it, session)) }
                            return@post
                        }
                        call.respond(Json.encodeToString(ListSerializer(TrackData.serializer()), trackList))
                        return@post
                    }

                    if (engine.queryRegex.let { (it != null) && query.matches(it) }) {
                        Loggers.GEN.info("Matching query for ${engine.name} : $query")

                        trackList = engine.downloadMultiple(query)
                        call.respond(Json.encodeToString(ListSerializer(TrackData.serializer()), trackList))
                        return@post
                    }
                }

                Loggers.GEN.info("Matching nothing, using generic Youtube search : $query")

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