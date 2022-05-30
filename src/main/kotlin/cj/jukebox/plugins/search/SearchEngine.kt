package cj.jukebox.plugins.search

import cj.jukebox.config
import cj.jukebox.database.TrackData
import cj.jukebox.utils.runCommand

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

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
    JAMENDO {
        override fun downloadSingle(url: String): List<TrackData> {
            throw NotImplementedError()
        }

        override fun jsonToTrack(metadata: JsonObject): TrackData {
            TODO("Not yet implemented")
        }

        override val urlRegex = urlRegexMaker("jamendo\\.com")
    },

    /**
     * Correspond à une URL directe vers une vidéo de Twitch.
     */
    TWITCH {
        override fun downloadSingle(url: String): List<TrackData> {
            throw NotImplementedError()
        }

        override fun jsonToTrack(metadata: JsonObject): TrackData {
            TODO("Not yet implemented")
        }

        override val urlRegex = urlRegexMaker("twitch\\.tv")
    },

    /**
     * Correspond à une URL directe vers une musique Bandcamp.
     */
    BANDCAMP {
        override fun downloadSingle(url: String): List<TrackData> {
            throw NotImplementedError()
        }

        override fun jsonToTrack(metadata: JsonObject): TrackData {
            TODO("Not yet implemented")
        }

        override val urlRegex = urlRegexMaker("bandcamp\\.com")
    },

    /**
     * Correspond à une URL directe vers une musique.
     */
    DIRECT_FILE {
        override fun downloadSingle(url: String): List<TrackData> {
            throw NotImplementedError()
        }

        override fun jsonToTrack(metadata: JsonObject): TrackData {
            TODO("Not yet implemented")
        }

        override val urlRegex = Regex("^(https?://)?\\.*\\.(mp3|mp4|ogg|flac|wav|webm)")
        override val queryRegex = Regex("^!direct .+\$")
    },

    /**
     * Correspond à une recherche Soundcloud.
     */
    SOUNDCLOUD {
        override fun downloadSingle(url: String): List<TrackData> {
            throw NotImplementedError()
        }

        override fun jsonToTrack(metadata: JsonObject): TrackData {
            TODO("Not yet implemented")
        }

        override val urlRegex = urlRegexMaker("soundcloud\\.com")
        override val queryRegex = Regex("^!sc .+\$")
    },

    /**
     * Correspond à la recherche avec YouTube.
     */
    YOUTUBE {
        override fun jsonToTrack(metadata: JsonObject) = TrackData(
            url = metadata["webpage_url"].toString().removeSurrounding("\""),
            source = name,
            track = (metadata["title"] ?: metadata["track"]).toString().removeSurrounding("\""),
            artist = (metadata["artist"] ?: metadata["uploader"]).toString().removeSurrounding("\""),
            album = (metadata["album"]).toString().removeSurrounding("\""),
            albumArtUrl = metadata["thumbnail"].toString().removeSurrounding("\""),
            duration = try {
                Integer.parseInt(metadata["duration"].toString().removeSurrounding("\""))
            } catch (e: Exception) {
                0
            },
            false,
            false
        )

        override fun downloadSingle(url: String): List<TrackData> {
            if ("list" in url) return searchYoutubeDL(url, true).map {jsonToTrack(it)}
            val newUrl = "https://www.youtube.com/watch?v=" +
                    if ("youtu.be" in url) {
                        url.substringAfter("youtu.be/").substringBefore("?")
                    } else {
                        url.substringAfter("youtube.com/watch?v=").substringBefore("&")
                    }
            return searchYoutubeDL(newUrl, true).map { jsonToTrack(it) }
        }

        override fun downloadMultiple(request: String) = searchYoutubeDL(
            "ytsearch5:\"${request.removePrefix("!yt ")}\""
        ).map { jsonToTrack(it) }

        override val youtubeArgs: Map<String, String> = mapOf("yes-playlist" to "")
        override val urlRegex = urlRegexMaker("youtube\\.com|youtu\\.be")
        override val queryRegex = "^!yt .+\$".toRegex()
    };
    /**
     * Permet de télécharger des metadatas depuis une URL. Utilise quasi exclusivement youtube-dl.
     * @param[url] Une URL vers une musique ou une playlist.
     * @return Une [List] de [TrackData], correspondant à l'URL.
     */
    abstract fun downloadSingle(url: String): List<TrackData>

    /**
     * Convertit un objet JSON en une TrackData.
     */
    abstract fun jsonToTrack(metadata: JsonObject): TrackData

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
        val wholeRequest = "yt-dlp --id --write-info-json --skip-download " +
                if (youtubeArgs.isNotEmpty()) {
                    youtubeArgs.entries
                        .joinToString("") { (key, value) ->
                            "--$key ${if (value.isEmpty()) "" else "$value "}"
                        }
                } else {
                    ""
                } + request
        println(wholeRequest)

        val randomValue = (0..Int.MAX_VALUE).random()

        val workingDir = tmpDir.resolve(randomValue.toString())
        workingDir.mkdirs()

        wholeRequest.runCommand(workingDir)
        val retour = workingDir
            .listFiles { _, s -> s.endsWith(".info.json") }
            ?.sortedBy { it.lastModified() }
            ?.map { Json.decodeFromString<JsonObject>(it.readText(Charsets.UTF_8)) }

        workingDir.deleteRecursively()

        return (if (direct_url) retour else retour?.dropLast(1)) ?: listOf()
    }
}
