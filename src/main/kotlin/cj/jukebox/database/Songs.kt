package cj.jukebox.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

const val urlReg = """https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)"""

object Songs : IntIdTable() {
    val url = varchar("url", 200)
        .check { it.match(urlReg) }

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

class Song(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Song>(Songs)

    var url by Songs.url
    var source by Songs.source_
    var track by Songs.track
    var artist by Songs.artist
    var album by Songs.album
    var albumArtUrl by Songs.albumArtUrl
    var duration by Songs.duration
    var blacklisted by Songs.blacklisted
    var obsolete by Songs.obsolete
}