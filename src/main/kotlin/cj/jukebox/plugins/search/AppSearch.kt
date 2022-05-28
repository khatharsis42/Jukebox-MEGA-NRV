package cj.jukebox.plugins.search

import cj.jukebox.database.Track

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.search() {
    routing {
        authenticate("auth-session") {
            post("/search") {
                val parameters = call.receiveParameters()

                val query = parameters.getOrFail("q").takeIf { it.isNotBlank() } ?: return@post
                println(query)

                for (a in SearchEngine.values()) {
                    if (query.matches(a.urlRegex)) println(a)
                    a.queryRegex?.let { if (query.matches(it)) println(a) }
                }
            }

            post("/refresh-track") {
                val parameters = call.request.queryParameters

                val url = parameters.getOrFail("url").takeIf { it.isNotBlank() } ?: return@post
                Track.refreshTrack(url)
            }
        }
    }
}