package cj.jukebox.plugins.search

import cj.jukebox.config
import cj.jukebox.database.TrackData
import cj.jukebox.utils.runCommand

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

import java.io.File


/**
 * Pour créer facilement des regex à partir d'un nom de domaine.
 */
private fun urlRegexMaker(domain: String) = Regex("^(https?://)?((www\\.)?$domain)/.+$")

/**
 * Enumération de toutes les sources prises en compte.
 * NB: Ce n'est pas forcément très beau, mais ça marche très bien.
 * @author khatharsis
 */
enum class SearchEngine {
    /**
     * Correspond à une URL directe vers une musique de Jamendo.
     */
    JAMENDO { override val urlRegex = urlRegexMaker("jamendo\\.com") },

    /**
     * Correspond à une URL directe vers une vidéo de Twitch.
     */
    TWITCH { override val urlRegex = urlRegexMaker("twitch\\.tv") },

    /**
     * Correspond à une URL directe vers une musique Bandcamp.
     */
    BANDCAMP { override val urlRegex = urlRegexMaker("(.+\\.)?bandcamp\\.com") },

    /**
     * Correspond à une URL directe vers une musique.
     */
    DIRECT_FILE {
        override val urlRegex = Regex("^(https?://)?.*\\.(mp3|mp4|ogg|flac|wav|webm)")
//        override val queryRegex = Regex("^!direct .+\$")
    },

    /**
     * Correspond à une recherche Soundcloud.
     */
    SOUNDCLOUD {
        override fun downloadMultiple(request: String) =
            searchYoutubeDL("ytsearch5:\"${request.removePrefix("!sc ")}\"").map { jsonToTrack(it) }

        override val urlRegex = urlRegexMaker("soundcloud\\.com")
        override val queryRegex = Regex("^!sc .+\$")
    },

    /**
     * Correspond à la recherche avec YouTube.
     */
    YOUTUBE {
        override fun downloadSingle(url: String): List<TrackData> {
            val args = url.split("?", "&")
            val head = args.first()
                .substringAfter("youtu.be/")     // for short URLs (aka no tail["v"])
                .substringAfter("youtube.com/")  // only useful for playlist case (.../playlist?list=...)
            val tail = args.drop(1)
                .map { arg -> arg.split("=", limit = 2).let { mapOf(it.first() to it.last()) } }
                .reduce { acc, map -> acc + map }

            val new = "https://www.youtube.com/watch?v=${tail["v"] ?: head}${tail["list"]?.let { "?list=$it" } ?: ""}"
            return super.downloadSingle(new)
        }

        override fun downloadMultiple(request: String) =
            searchYoutubeDL("ytsearch5:\"${request.removePrefix("!yt ")}\"").map { jsonToTrack(it) }

        override val youtubeArgs: Map<String, String> = mapOf("yes-playlist" to "")
        override val urlRegex = urlRegexMaker("youtube\\.com|youtu\\.be")
        override val queryRegex = "^!yt .+\$".toRegex()
    };

    /**
     * Permet de télécharger des metadatas depuis une URL. Utilise quasi exclusivement youtube-dl.
     * @param[url] Une URL vers une musique ou une playlist.
     * @return Une [List] de [TrackData], correspondant à l'URL.
     */
    open fun downloadSingle(url: String): List<TrackData> = searchYoutubeDL(url, true).map { jsonToTrack(it) }

    /**
     * Convertit un objet JSON en une TrackData.
     */
    open fun jsonToTrack(metadata: JsonObject): TrackData = TrackData(
        url = Json.decodeFromJsonElement(metadata["webpage_url"]!!),
        source = name,
        track = (metadata["title"] ?: metadata["track"])?.let { Json.decodeFromJsonElement(it) },
        artist = (metadata["artist"] ?: metadata["uploader"])?.let { Json.decodeFromJsonElement(it) },
        album = (metadata["album"])?.let { Json.decodeFromJsonElement(it) },
        albumArtUrl = metadata["thumbnail"]?.let { Json.decodeFromJsonElement(it) },
        duration = try {
            Integer.parseInt(
                metadata["duration"]
                    .toString()
                    .removeSurrounding("\"")
                    .substringBefore(".")
            )
        } catch (e: Exception) {
            println(e)
            println(metadata["duration"])
            0
        },
        blacklisted = false,
        obsolete = false
    )

    /**
     * Permet de télécharger des metadatas depuis une requête textuelle.
     * Est implémenté ssi [queryRegex] est non null.
     * @param[request] Une URL vers une musique ou une playlist.
     * @return Une [List] de [TrackData], correspondant à la requête.
     */
    open fun downloadMultiple(request: String): List<TrackData> {
        throw NotImplementedError()
    }

    /**
     * Les arguments donnés à youtube-dl.
     */
    protected open val youtubeArgs: Map<String, String> = mapOf()

    /**
     * Une regex permettant de reconnaître une URL.
     */
    abstract val urlRegex: Regex

    /**
     * Un regex permettant de reconnaître une query.
     */
    open val queryRegex: Regex? = null

    private val tmpDir = File(config.data.TMP_PATH)

    /**
     * Exécute une recherche via youtube-dl. Une liste des métadonnées en Json.
     * @param[request] Une [List]<[JsonObject]> correspondant aux métadonnées de la requête.
     */
    protected fun searchYoutubeDL(request: String, direct_url: Boolean = false): List<JsonObject> {
        val args = youtubeArgs.takeIf { it.isNotEmpty() }
            ?.map { (k, v) -> "--$k $v" }
            ?.reduce { acc, s -> "$acc $s" }
            ?.let { " $it" }
            ?: ""
        val wholeRequest = "yt-dlp --id --write-info-json --skip-download$args $request"

        val randomValue = (0..Int.MAX_VALUE).random()
        val workingDir = tmpDir.resolve(randomValue.toString())
        workingDir.mkdirs()

        wholeRequest.runCommand(workingDir)
        return workingDir
            .listFiles { _, s -> s.endsWith(".info.json") }
            ?.sortedBy { it.lastModified() }
            ?.map { Json.decodeFromString<JsonObject>(it.readText(Charsets.UTF_8)) }
            ?.filter { Json.decodeFromJsonElement<String>(it["_type"]!!) != "playlist" }
            ?.let { if (direct_url) it else it.dropLast(1) }
            .also { workingDir.deleteRecursively() }  // cleaning before return
            ?: listOf()
    }
}