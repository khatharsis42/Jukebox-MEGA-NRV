package cj.jukebox.plugins.search

import cj.jukebox.database.Track
import cj.jukebox.search.Youtube

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
                if (query != null && query.isNotBlank())
                    Youtube().downloadSingle(query)
            }

            post("/refresh-track") {
                val parameters = call.request.queryParameters
                val url = parameters["url"]
                if (url != null) {
                    Track.refresh(url)
                } else {
                    println("xd")
                }
            }
        }
    }
}