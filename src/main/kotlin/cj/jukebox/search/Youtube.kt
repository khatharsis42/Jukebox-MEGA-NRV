package cj.jukebox.search

import cj.jukebox.database.Song

/**
 * Le search engine de YouTube.
 */
class Youtube : SearchEngine {
    override fun downloadSingle(url: String): Array<Song> {
        val json = searchYoutubeDL(url)
        return super.downloadSingle(url)
    }

    override fun downloadMultiple(request: String): Array<Song> {
        return super.downloadMultiple(request)
    }

    override val youtubeArgs : Map<String, String> = mapOf("yes-playlist" to "")
}