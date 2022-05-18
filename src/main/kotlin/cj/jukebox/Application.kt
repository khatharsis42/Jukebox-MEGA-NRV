package cj.jukebox

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import cj.jukebox.plugins.*

fun main() {
    embeddedServer(Netty, port = 9000, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureSecurity()
    }.start(wait = true)
}
