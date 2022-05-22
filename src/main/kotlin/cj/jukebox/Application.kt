package cj.jukebox

import cj.jukebox.config.Config
import cj.jukebox.database.DatabaseFactory
import cj.jukebox.plugins.*

import io.ktor.server.engine.*
import io.ktor.server.netty.*

val config = Config("src/main/resources/config.json")
val database = DatabaseFactory(config.data.DATABASE_PATH)

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
    }.start(wait = true)
}