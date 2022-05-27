package cj.jukebox

import cj.jukebox.config.Config
import cj.jukebox.database.DatabaseFactory

import cj.jukebox.plugins.auth.auth
import cj.jukebox.plugins.nav.nav
import cj.jukebox.plugins.playlist.playlist
import cj.jukebox.plugins.search.search
import cj.jukebox.plugins.settings.settings
import cj.jukebox.plugins.statistics.statistics
import cj.jukebox.plugins.track.track

import io.ktor.server.engine.*
import io.ktor.server.netty.*

val config = Config("src/main/resources/config.json")
val database = DatabaseFactory(config.data.DATABASE_PATH)

/**
 * Lieu de lancement de l'application jukebox.
 * Récupère port, adresse et module puis lance le serveur.
 */
fun main() {
    embeddedServer(
        Netty,
        port = config.data.LISTEN_PORT,
        host = config.data.LISTEN_ADDRESS
    ) {
        auth()
        nav()
        settings()
        statistics()

        playlist()
        track()
        search()
    }.start(wait = true)
}