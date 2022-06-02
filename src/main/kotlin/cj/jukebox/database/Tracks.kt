package cj.jukebox.database

import cj.jukebox.database
import cj.jukebox.plugins.search.SearchEngine
import cj.jukebox.utils.Loggers
import cj.jukebox.utils.UserSession

import io.ktor.http.*
import io.ktor.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

//const val urlReg = """^(https?://)?((www\\.)?).+/.+$"""

object Tracks : IntIdTable() {
    val url = varchar("url", 200)

    val source_ = varchar("source", 20)
    val track = varchar("track", 200).nullable()
    val artist = varchar("artist", 200).nullable()
    val album = varchar("album", 200).nullable()
    val albumArtUrl = varchar("albumArtUrl", 200).nullable()
    val duration = integer("duration")

    val blacklisted = bool("blacklisted")
        .default(false)
    val obsolete = bool("obsolete")
        .default(false)
}

/**
 * Une data class pour gérer les Tracks sans forcément avoir besoin de les mettre dans la BDD.
 * @author Khatharsis
 */
@Serializable
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

    val user: String? = null,                         // for playlist management
    val randomid: Int = (0..Int.MAX_VALUE).random(),  // for playlist management
) {
    constructor(track: Track, user: String? = null) : this(
        url = track.url,
        source = track.source,
        track = track.track,
        artist = track.artist,
        album = track.album,
        albumArtUrl = track.albumArtUrl,
        duration = track.duration,
        blacklisted = track.blacklisted,
        obsolete = track.obsolete,
        user = user
    )

    constructor(parameters: Parameters, user: String? = null) : this(
        url = parameters["url"]!!,
        source = parameters["source"]!!,
        track = parameters["track"],
        artist = parameters["artist"],
        album = parameters["album"],
        albumArtUrl = parameters["albumArtUrl"],
        duration = Integer.parseInt(parameters["duration"]),
        blacklisted = parameters["blacklisted"] == "true",
        obsolete = parameters["obsolete"] == "true",
        user = user,
        randomid = parameters["randomid"]?.toInt() ?: (0..Int.MAX_VALUE).random()
    )

    constructor(track: Track, user: User? = null) : this(track, user?.name)

    constructor(track: Track, user: UserSession) : this(track, user.name)

    constructor(parameters: Parameters, user: User? = null) : this(parameters, user?.name)

    constructor(parameters: Parameters, user: UserSession) : this(parameters, user.name)

    companion object {
        fun createFromYTApi(metadata: JsonObject, source: String): TrackData {
            val snippet = metadata["snippet"] as JsonObject

            val id = Json.decodeFromJsonElement<String>(metadata["id"]!!)
            val albumArtUrl = ((snippet["thumbnails"] as JsonObject)["medium"] as JsonObject)["url"]
                ?.let { Json.decodeFromJsonElement<String>(it) }
            val track = snippet["title"]
                ?.let { Json.decodeFromJsonElement<String>(it) }
            val artist = snippet["channelTitle"]
                ?.let { Json.decodeFromJsonElement<String>(it) }
            val duration = (metadata["contentDetails"] as JsonObject)["duration"]
                ?.let { Json.decodeFromJsonElement<String>(it) }

            return TrackData(
                url = "https://www.youtube.com/watch?v=${id}",
                source = source,
                track = track,
                artist = artist,
                album = null,
                albumArtUrl = albumArtUrl,
                duration = duration?.let { Duration.parseIsoString(it).toInt(DurationUnit.SECONDS) } ?: 0,
                blacklisted = false,
                obsolete = false
            )

        }

        fun createFromYoutubeDL(metadata: JsonObject, source: String): TrackData {
            val url = Json.decodeFromJsonElement<String>(metadata["webpage_url"]!!)
            val track = (metadata["title"] ?: metadata["track"])
                ?.let { Json.decodeFromJsonElement<String>(it) }
            val artist = (metadata["artist"] ?: metadata["uploader"])
                ?.let { Json.decodeFromJsonElement<String>(it) }
            val album = metadata["album"]
                ?.let { Json.decodeFromJsonElement<String>(it) }
            val albumArtUrl = metadata["thumbnail"]
                ?.let { Json.decodeFromJsonElement<String>(it) }
            val duration = metadata["duration"]
                ?.let { Json.decodeFromJsonElement<Int>(it) }

            return TrackData(
                url = url,
                source = source,
                track = track,
                artist = artist,
                album = album,
                albumArtUrl = albumArtUrl,
                duration = try {
                    duration
                } catch (e: Exception) {
                    Loggers.DL.error(e)
                    Loggers.DL.error(metadata["duration"])
                    null
                } ?: 0,
                blacklisted = false,
                obsolete = false
            )
        }
    }
}

/**
 * Représentation objet d'une ligne de la table [Tracks]
 * @author Ukabi
 */
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
     * Refresh les métadonnées d'une [Track].
     * @author Khatharsis
     * @author Ukabi
     */
    fun refresh() {
        val sourceEngine = source
            .toUpperCasePreservingASCIIRules()
            .takeIf { trackSource -> trackSource in SearchEngine.values().map { it.name } }
            ?.let { SearchEngine.valueOf(it) }
            ?: SearchEngine.values().first { url.matches(it.urlRegex) }

        val metadata = sourceEngine.downloadSingle(url).first()
        database.dbQuery {
            source = metadata.source
            track = metadata.track
            artist = metadata.artist
            album = metadata.album
            albumArtUrl = metadata.albumArtUrl
            duration = metadata.duration
            blacklisted = metadata.blacklisted
            obsolete = metadata.obsolete
        }
    }

    companion object : IntEntityClass<Track>(Tracks) {
        /**
         * Créé une nouvelle occurrence dans la table [Tracks].
         * @param[metadata] Les métadonnées à enregistrer.
         * @return La [Track] fraichement créée.
         * @author Ukabi
         */
        fun createTrack(metadata: TrackData): Track =
            database.dbQuery {
                Track.new {
                    url = metadata.url
                    source = metadata.source
                    track = metadata.track
                    artist = metadata.artist
                    album = metadata.album
                    albumArtUrl = metadata.albumArtUrl
                    duration = metadata.duration
                    blacklisted = metadata.blacklisted
                    obsolete = metadata.obsolete
                }
            }

        /**
         * Renvoie la [Track] ayant pour id [id] dans la [Tracks].
         * @param[id] L'id à chercher.
         * @return La [Track] correspondante à l'id fourni (si existante).
         * @author Ukabi
         */
        fun getTrack(id: Int): Track? = database.dbQuery { Track.findById(id) }

        /**
         * Donne toutes les [Track] pour lesquelles [name] correspond.
         * @param[name] Nom à chercher.
         * @return Une [List] des [Track] dont le nom match.
         * @author Ukabi
         */
        fun getFromName(name: String): List<Track> = database.dbQuery { Track.find { Tracks.track eq name }.toList() }

        /**
         * Importe une [Track] à partir de [trackUrl].
         * @param[trackUrl] L'URL à chercher.
         * @return La [Track] correspondante à l'URL fournie (si existante).
         * @author Ukabi
         */
        fun getFromUrl(trackUrl: String): Track? =
            database.dbQuery {
                Track
                    .find { Tracks.url eq trackUrl }
                    .limit(1)
                    .firstOrNull()
            }

        /**
         * Met à jour les métadonnées de la [Track] liée à [trackUrl].
         * Ne fait rien s'il n'existe pas de [Track] correspondante.
         * @param[trackUrl] L'URL d'une track.
         * @return La [Track] correspondante à l'URL fournie (si existante) et mise à jour.
         * @author Khatharsis
         * @author Ukabi
         */
        fun refresh(trackUrl: String): Track? = getFromUrl(trackUrl)?.also { it.refresh() }
    }
}