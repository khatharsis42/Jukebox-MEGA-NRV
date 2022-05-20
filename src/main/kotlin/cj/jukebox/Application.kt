package cj.jukebox

import cj.jukebox.config.Config
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import cj.jukebox.plugins.*

val config = Config("src/main/resources/config.json")

fun main() {

    embeddedServer(
        Netty,
        port = config.data.LISTEN_PORT,
        host = config.data.LISTEN_ADDRESS
    ) {
        auth()
        routing()
        statistics()
        settings()
        serialization()
    }.start(wait = true)
}