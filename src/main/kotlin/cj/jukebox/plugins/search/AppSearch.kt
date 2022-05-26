package cj.jukebox.plugins.search

import cj.jukebox.database.Track

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.search() {
    routing {
        authenticate("auth-session") {
            post("/search") {
                val parameters = call.receiveParameters()
                val query = parameters["q"]
                println(query)
                if (query != null && query.isNotBlank()) {
                    for (a in SearchEngine.values()) {
                        if (query.matches(a.urlRegex))
                            println(a)
                        if (a.queryRegex != null) {
                            if (query.matches(a.queryRegex!!))
                                println(a)
                        }
                    }
                }
            }

            post("/refresh-track") {
                val parameters = call.request.queryParameters
                val url = parameters["url"]
                if (url != null && url.isNotBlank()) {
                    Track.refreshTrack(url)
                } else {
                    println("xd")
                }
            }
        }
    }
}