package cj.jukebox.database

import cj.jukebox.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

const val urlReg = """https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)"""

object Tracks : IntIdTable() {
    val url = varchar("url", 200)
        .check { it.match(urlReg) }
        .uniqueIndex()

    val source_ = varchar("source", 20)
    val track = varchar("track", 50).nullable()
    val artist = varchar("artist", 50).nullable()
    val album = varchar("album", 50).nullable()
    val albumArtUrl = varchar("albumArtUrl", 200).nullable()
    val duration = integer("duration").nullable()

    val blacklisted = bool("blacklisted")
        .default(false)
    val obsolete = bool("obsolete")
        .default(false)
}

class Track(id: EntityID<Int>) : IntEntity(id) {
    var url by Tracks.url
    var source by Tracks.source_
    var track by Tracks.track
    var artist by Tracks.artist
    var album by Tracks.album
    var albumArtUrl by Tracks.albumArtUrl
    var duration by Tracks.duration
    var blacklisted by Tracks.blacklisted
    var obsolete by Tracks.obsolete

    companion object : IntEntityClass<Track>(Tracks) {
        fun importFromId(id: Int): Track? = database.dbQuery { Track.findById(id) }

        fun importFromName(name: String): Iterable<Track> =
            database.dbQuery { Track.find { Tracks.track eq name } }

        fun importFromUrl(trackUrl: String): Track? =
            database.dbQuery {
                val res = Track
                    .find { Tracks.url eq trackUrl }
                    .limit(1).toList()
                if (res.isNotEmpty()) res.first() else null
            }
    }
}