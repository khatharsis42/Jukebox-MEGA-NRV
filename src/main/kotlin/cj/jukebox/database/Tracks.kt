package cj.jukebox.database

import cj.jukebox.database
import cj.jukebox.search.SearchEngine

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

import io.ktor.util.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

const val urlReg =
    """https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)"""

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

/**
 * Une data class pour gérer les Tracks sans forcément avoir besoin de les mettre dans la BDD.
 */
data class TrackData(
    val url: String,
    val source: String,
    val track: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String,
    val duration: String,
    val blacklisted: String,
    val obsolete: String
)

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

    /**
     * Refresh les metadatas d'une track dans la BDD.
     * @param[metadatas] Les metadatas, dans une TrackData.
     */
    fun refreshTrack(metadatas: TrackData) {
        for (property in this::class.memberProperties) {
            if (property is KMutableProperty<*> && property.name in metadatas::class.memberProperties.map { it.name }) {
                property.setter.call(metadatas::class.memberProperties.first { it.name == property.name }.getter.call())
            }
        }
        // This should work, right ?
        // C'est vraiment parce que j'avais la flemme de tout écrire à la main.
    }

    companion object : IntEntityClass<Track>(Tracks) {
        /**
         * Importe une Track à partir de son id dans la BDD.
         * @param[id] [Int] indiquant son entier dans la BDD.
         * @return Une [Track] si l'ID est dans la DB, [Nothing] sinon
         */
        fun importFromId(id: Int): Track? = database.dbQuery { Track.findById(id) }

        /**
         * Importe toutes les tracks dont le nom corresponds à la [String] donnée en argument.
         * @param[name] Nom que l'on utilise pour matcher.
         * @return Une [List] des [Tracks] dont le nom match.
         */
        fun importFromName(name: String): List<Track> =
            database.dbQuery { Track.find { Tracks.track eq name }.toList() }

        /**
         * Importe une track à partir de son URL.
         * @param[trackUrl] L'URL d'une track.
         * @return Une [Track] si l'URL est dans la DB, [Nothing] sinon
         */
        fun importFromUrl(trackUrl: String): Track? =
            database.dbQuery {
                val res = Track
                    .find { Tracks.url eq trackUrl }
                    .limit(1).toList()
                if (res.isNotEmpty()) res.first() else null
            }

        /**
         * Refresh une track dans la BDD depuis son URL. Ne fait rien s'il n'existe pas de Track correspondante.
         * @param[trackUrl] L'URL d'une track.
         */
        fun refreshTrack(trackUrl: String) {
            val track = importFromUrl(trackUrl)
            if (track != null) {
                val sourceEngine: SearchEngine
                if (SearchEngine.values().map { it.toString() }
                        .contains(track.source.toUpperCasePreservingASCIIRules())) {
                    sourceEngine = SearchEngine.valueOf(track.source.toUpperCasePreservingASCIIRules())
                } else {
                    sourceEngine = SearchEngine.values().first { track.url.matches(it.urlRegex) }
                    track.source = sourceEngine.toString()
                }
                val metadatas = sourceEngine.downloadSingle(track.url).first()
                track.refreshTrack(metadatas)
            }
        }
    }
}
