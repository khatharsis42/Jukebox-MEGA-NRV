package cj.jukebox.plugins.playlist

import cj.jukebox.database.TrackData
import cj.jukebox.database.Log
import cj.jukebox.utils.Loggers

//import cj.jukebox.player

/**
 * Une classe représentant la playlist.
 * @author Khâtharsis
 */
@kotlinx.serialization.Serializable
class Playlist : MutableList<TrackData> by mutableListOf() {
    /**
     * Échange les [Track] étant aux emplacements [from] et [to].
     * @author Ukabi
     */
    private fun move(from: Int, to: Int) {
        this[from] = this[to].also { this[to] = this[from] }
//        .also { player.sendSignal(SigName.SIGUSR2) }
    }


    /**
     * Échange l'élément [from] de la [Playlist] avec l'élément au-dessus ou en dessous,
     * selon la valeur de [direction].
     * Ne procède pas à l'échange si le bord de la [Playlist] est dépassé.
     * @author Ukabi
     */
    fun move(from: Int, direction: Direction) =
        when (direction) {
            Direction.TOP -> add(0, removeAt(from))
            Direction.UP -> (from - 1).takeIf { it >= 0 }?.let { move(from, it) }
            Direction.DOWN -> (from + 1).takeIf { it < size }?.let { move(from, it) }
        }

    /**
     * Vérifie si la [track] fournie peut être jouée, puis l'ajoute à la [Playlist].
     * Garde aussi la trace de l'[user] l'ayant requêtée.
     * Créé aussi une nouvelle occurrence dans la table [Logs].
     * @author Khâtharsis Ukabi
     */
    fun addIfPossible(track: TrackData): Boolean =
        !track.blacklisted && !track.obsolete && add(track)
            .also { Loggers.GEN.info("Adding a track: $track") }
//        .also { if (it) player.sendSignal(SigName.SIGUSR2) }

    /**
     * Vérifie si la [track] fournie fait partie de la [Playlist], puis la supprime.
     * @return L'index de la track supprimée, *null* le cas échéant.
     * @author Khâtharsis Ukabi
     */
    fun removeIfPossible(randomid: Int): Int? =
        map {it.randomid}
            .indexOf(randomid).takeIf { it >= 0 }
            ?.also { Loggers.GEN.info("Removing a track: ${get(it)}") }
            ?.also { removeAt(it) }

//        ?.also { player.sendSignal(if (it == 0) SigName.SIGUSR1 else SigName.SIGUSR2) }

    /**
     * Renvoie la somme des durées (en secondes) de chacune des [Track] de la [Playlist].
     * @author Ukabi
     */
    fun duration(): Int = mapNotNull { it.duration }.reduce { acc, i -> acc + i }
}

enum class Direction(val direction: String) {
    TOP("top"),
    UP("up"),
    DOWN("down")
}

/**
 * Renvoie une [List] de [n] des précédents [Log] dont la [Track] peut être jouée.
 * @author Ukabi
 */
fun suggest(n: Int = 5): List<Log> = Log.getRandom(n)
