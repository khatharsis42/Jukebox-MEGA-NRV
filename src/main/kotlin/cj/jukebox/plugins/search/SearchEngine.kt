package cj.jukebox.plugins.search

import cj.jukebox.config
import cj.jukebox.database.TrackData
import cj.jukebox.utils.Loggers
import cj.jukebox.utils.runCommand

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Enumération de toutes les sources prises en compte.
 *
 * @param[urlRegex] Un regex permettant de reconnaître une URL.
 * @author khatharsis
 */
enum class SearchEngine(val urlRegex: Regex) {
    /**
     * Correspond à une URL directe vers une musique de Jamendo.
     */
    JAMENDO("jamendo\\.com"),

    /**
     * Correspond à une URL directe vers une vidéo de Twitch.
     */
    TWITCH("twitch\\.tv"),

    /**
     * Correspond à une URL directe vers une musique Bandcamp.
     */
    BANDCAMP("(.+\\.)?bandcamp\\.com"),

    /**
     * Correspond à une URL directe vers une musique.
     */
    DIRECT_FILE(Regex("^(https?://)?.*\\.(mp3|mp4|ogg|flac|wav|webm)")),

    /**
     * Correspond à une recherche Soundcloud.
     */
    SOUNDCLOUD("soundcloud\\.com") {
        override fun downloadMultiple(request: String) =
            searchYoutubeDL("scsearch5:\"${request.removePrefix("!sc ")}\"").map { super.jsonToTrack(it) }

        override val queryRegex = Regex("^!sc .+\$")
    },

    /**
     * Correspond à la recherche avec YouTube.
     */
    YOUTUBE("youtube\\.com|youtu\\.be") {
        override fun downloadSingle(url: String): List<TrackData> {
            if ("list" in url)
                return searchYoutubeApi(url, true)

            val urlInfo =
                if ("youtu.be" in url)
                    url.substringAfter("youtu.be/").substringBefore("?")
                else
                    url.substringAfter("youtube.com/watch?v=").substringBefore("&")

            val newUrl = "https://www.youtube.com/watch?v=$urlInfo"

            return super.downloadSingle(newUrl)
        }

        override fun downloadMultiple(request: String) = searchYoutubeApi(request, false)

        private fun getRequest(url: String, params: Map<String, String>): Pair<Boolean, JsonObject> {
            val connection =
                URL("$url?${params.map { (key, value) -> "$key=$value&" }.reduce(String::plus)}")
                    .openConnection() as HttpURLConnection

            if (connection.responseCode != 200 && connection.responseCode != 403)
                throw Exception("Error ${connection.responseCode}: ${connection.responseMessage}")

            val responseString =
                connection.responseCode.takeIf { it == 200 }
                    ?.let { connection.inputStream.bufferedReader().lines().reduce(String::plus).get() }
                    ?: "{}"

            return (connection.responseCode == 200) to Json.decodeFromString(responseString)
        }

        private fun searchYoutubeApi(query: String, searchPlaylist: Boolean): List<TrackData> {
            Loggers.DL.info("Searching request using Youtube API")

            for (key in config.data.YT_KEYS) {
                val (validResponse, response) =
                    if (searchPlaylist)
                        getRequest(
                            "https://www.googleapis.com/youtube/v3/playlistItems",
                            mapOf(
                                "key" to key,
                                "part" to "snippet",
                                "maxResults" to "50",
                                "playlistId" to query.substringAfter("list=").substringBefore("&")
                            )
                        )
                    else getRequest(
                        "https://www.googleapis.com/youtube/v3/search",
                        mapOf(
                            "key" to key,
                            "q" to URLEncoder.encode(query, StandardCharsets.UTF_8.toString()),
                            "part" to "snippet",
                            "type" to "video",
                            "maxResults" to "5"
                        )
                    )

                if (!validResponse)
                    continue  // On essaye les autres clefs

                if (response.isEmpty() || (response["items"] as JsonArray).isEmpty())
                    Loggers.DL.warn("Nothing found on Youtube for query $query")

                val videoIds = (response["items"] as JsonArray)
                    .map { it as JsonObject }.mapNotNull { it["id"] }
                    .let { ids ->
                        if (searchPlaylist)
                            ids.map { it as JsonObject }.mapNotNull { it["ressourceId"] }
                        else
                            ids
                    }
                    .map { it as JsonObject }.mapNotNull { it["videoId"] }
                    .map { Json.decodeFromJsonElement<String>(it) }

                val (secondValidResponse, secondResponse) = getRequest(
                    "https://www.googleapis.com/youtube/v3/videos",
                    mapOf(
                        "part" to "snippet,contentDetails",
                        "key" to key,
                        "id" to videoIds.reduce { acc, s -> "$acc,$s" }
                    )
                )

                if (!secondValidResponse)
                    continue  // On essaye les autres clefs

                return (secondResponse["items"] as JsonArray)
                    .map { it as JsonObject }
                    .map { TrackData.createFromYTApi(it, name) }
            }

            // On a plus de clefs et aucune n'a fonctionné
            return searchYoutubeDL("ytsearch5:${query.removePrefix("!yt ")}").map { super.jsonToTrack(it) }
        }

        override val youtubeArgs: Map<String, String> = mapOf("yes-playlist" to "")
        override val queryRegex = "^!yt .+\$".toRegex()
    };

    /**
     * Constructeur avec un nom de domaine.
     * @param[domain] Nom de domaine de l'URL.
     */
    constructor(domain: String) : this(Regex("^(https?://)?((www\\.)?$domain)/.+$"))

    /**
     * Permet de télécharger des metadatas depuis une URL. Utilise quasi exclusivement youtube-dl.
     * @param[url] Une URL vers une musique ou une playlist.
     * @return Une [List] de [TrackData], correspondant à l'URL.
     */
    open fun downloadSingle(url: String): List<TrackData> = searchYoutubeDL(url).map { jsonToTrack(it) }

    /**
     * Convertit un objet JSON en une TrackData.
     */
    protected fun jsonToTrack(metadata: JsonObject): TrackData = TrackData.createFromYoutubeDL(metadata, name)

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
     * Un regex permettant de reconnaître une query.
     */
    open val queryRegex: Regex? = null

    private val tmpDir = File(config.data.TMP_PATH)

    /**
     * Exécute une recherche via youtube-dl. Une liste des métadonnées en Json.
     * @param[request] Une [List]<[JsonObject]> correspondant aux métadonnées de la requête.
     */
    protected fun searchYoutubeDL(request: String): List<JsonObject> {
        Loggers.DL.info("Searching for request using youtube-dlp")

        val wholeRequest = listOf("yt-dlp", "--id", "--write-info-json", "--skip-download") +
                youtubeArgs.map { (k, v) -> "--$k${if (v.isNotBlank()) " $v" else ""}" } + listOf(request)

        Loggers.DL.info(wholeRequest.reduce { a, b -> "$a $b" })

        val randomValue = (0..Int.MAX_VALUE).random()
        val workingDir = tmpDir.resolve(randomValue.toString())
        workingDir.mkdirs()

        wholeRequest.runCommand(workingDir, logger = Loggers.DL)
        return workingDir
            .listFiles { _, s -> s.endsWith(".info.json") }
            ?.sortedBy { it.lastModified() }
            ?.map { Json.decodeFromString<JsonObject>(it.readText(Charsets.UTF_8)) }
            ?.filter { Json.decodeFromJsonElement<String>(it["_type"]!!) != "playlist" }
            .also { workingDir.deleteRecursively() }  // cleaning before return
            ?: listOf()
    }
}