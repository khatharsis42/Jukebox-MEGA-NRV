package cj.jukebox

import cj.jukebox.config.Config
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import cj.jukebox.plugins.*
import io.ktor.server.resources.*

fun main() {
    val config = Config("src/main/resources/config.json")

    embeddedServer(
        Netty,
        port = config.data.LISTEN_PORT,
        host = config.data.LISTEN_ADDRESS
    ) {
        configureRouting()
        configureSerialization()
        configureSecurity()
        statistics()
        settings()
    }.start(wait = true)
}