package cj.jukebox.database

import cj.jukebox.database
import cj.jukebox.plugins.search.SearchEngine

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
    val duration = integer("duration")

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
    val track: String?,
    val artist: String?,
    val album: String?,
    val albumArtUrl: String?,
    val duration: Int,
    val blacklisted: Boolean,
    val obsolete: Boolean,
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
         * Renvoie la [Track] ayant pour id [id] dans la [Tracks].
         * @param[id] L'id à chercher.
         * @return La [Track] correspondante à l'id fourni (si existante).
         */
        fun getTrack(id: Int): Track? = database.dbQuery { Track.findById(id) }

        /**
         * Donne toutes les [Track] pour lesquelles [name] correspond.
         * @param[name] Nom à chercher.
         * @return Une [List] des [Track] dont le nom match.
         */
        fun getFromName(name: String): List<Track> =
            database.dbQuery { Track.find { Tracks.track eq name }.toList() }

        /**
         * Importe une [Track] à partir de [trackUrl].
         * @param[trackUrl] L'URL à chercher.
         * @return La [Track] correspondante à l'URL fournie (si existante).
         */
        fun getFromUrl(trackUrl: String): Track? =
            database.dbQuery {
                Track
                    .find { Tracks.url eq trackUrl }
                    .limit(1)
                    .toList()
                    .firstOrNull()
            }

        /**
         * Met à jour les métadonnées de la [Track] liée à [trackUrl].
         * Ne fait rien s'il n'existe pas de [Track] correspondante.
         * @param[trackUrl] L'URL d'une track.
         * @return La [Track] correspondante à l'URL fournie (si existante) et mise à jour.
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