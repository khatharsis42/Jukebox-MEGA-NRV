package cj.jukebox.search

import cj.jukebox.database.Song
import kotlinx.html.Dir
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * La classe de base pour faire une recherche.
 */
interface SearchEngine {
    /**
     * Permet de télécharger des metadatas depuis une URL.
     * @param[url] Une URL vers une musique ou une playlist.
     * @return Une [Array] de [Song], correspondant à l'URL.
     */
    fun downloadSingle(url:String): Array<Song> {
        throw NotImplementedError()
    }
    /**
     * Permet de télécharger des metadatas depuis une requête textuelle.
     * @param[url] Une URL vers une musique ou une playlist.
     * @return Une [Array] de [Song], correspondant à la requête.
     */
    fun downloadMultiple(request:String): Array<Song> {
        throw NotImplementedError()
    }

    /**
     * Les arguments donnés à youtube-dl.
     */
    val youtubeArgs : Map<String, String>

    private val tempDir: File
        get() = File("./src/main/resources/temp")

    /**
     * Exécute une recherche via youtube-dl. Une liste des métadonnées en Json.
     * @param[request] Une [List]<[JsonObject]> correspondant aux métadonnées de la requête.
      */
    fun searchYoutubeDL(request:String) : List<JsonObject> {
        println(request)
        val wholeRequest = "yt-dlp --id --write-info-json --skip-download " +
                if (youtubeArgs.isNotEmpty()) {
                    youtubeArgs
                        .map { (key, value) -> "--$key ${if (value.isEmpty()) "" else "$value "}" }
                        .reduce { acc, s -> acc + s }
                } else {""} + request
        val randomValue = (0..Int.MAX_VALUE).random()
        "mkdir $randomValue".runCommand(tempDir)
        wholeRequest.runCommand(tempDir.resolve(randomValue.toString()))
        val retour = tempDir.resolve(randomValue.toString())
            .listFiles { file, s -> s.endsWith(".json") }
            ?.map {Json.decodeFromString<JsonObject>(it.readText(Charsets.UTF_8))}
        "rm -rf $randomValue".runCommand(tempDir)
        return retour ?: listOf()
    }
    fun String.runCommand(workingDir: File) {
        ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
    }
}