package cj.jukebox.plugins.playlist

//import cj.jukebox.player
import cj.jukebox.database.Log
import cj.jukebox.database.Track
import cj.jukebox.database.TrackData
import cj.jukebox.database.User
import cj.jukebox.utils.Loggers
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

enum class Direction { TOP, UP, DOWN }

/**
 * Une classe représentant la playlist.
 * @author Khâtharsis
 * @author Ukabi
 */
@Serializable(with = PlaylistSerializer::class)
class Playlist : MutableList<TrackData> by mutableListOf() {
    /**
     * Échange les [TrackData] étant aux emplacements [from] et [to].
     * @author Ukabi
     */
    private fun move(from: Int, to: Int) {
        this[from] = this[to].also { this[to] = this[from] }
//            .also { player.sendSignal(SigName.SIGUSR2) }
    }

    /**
     * Échange l'élément [from] de la [Playlist] avec l'élément au-dessus ou en dessous,
     * selon la valeur de [direction].
     * Ne procède pas à l'échange si le bord de la [Playlist] est dépassé ou si on essaye de bouger la track à 0.
     * @author Ukabi
     */
    fun move(from: Int, direction: Direction) =
        if (from != 0) {
            when (direction) {
                Direction.TOP -> add(1, removeAt(from))
                Direction.UP -> (from - 1).takeIf { it >= 1 }?.let { move(from, it) }
                Direction.DOWN -> (from + 1).takeIf { it < size }?.let { move(from, it) }
            }
        } else Unit

    /**
     * Vérifie si la [track] fournie peut être jouée, puis l'ajoute à la [Playlist].
     * Garde aussi la trace de l'[User] l'ayant requêtée.
     * @author Khâtharsis
     * @author Ukabi
     */
    fun addIfPossible(track: TrackData): Boolean =
        !track.blacklisted && !track.obsolete && (add(track)
            .also { Loggers.GEN.info("Adding a track: $track") }
            .also { Log.createLog(track) })
//            .also { if (it) player.sendSignal(SigName.SIGUSR2) }

    /**
     * Vérifie si l'id de la [TrackData] fournie fait partie de la [Playlist], puis la supprime.
     * @return L'index de la track supprimée, *null* le cas échéant.
     * @author Khâtharsis
     * @author Ukabi
     */
    fun removeIfPossible(randomid: Int): Int? =
        map { it.randomid }
            .indexOf(randomid).takeIf { it >= 0 }
            ?.also { Loggers.GEN.info("Removing a track: ${get(it)}") }
            ?.also { removeAt(it) }
//            ?.also { player.sendSignal(if (it == 0) SigName.SIGUSR1 else SigName.SIGUSR2) }

    /**
     * Renvoie la somme des durées (en secondes) de chacune des [TrackData] de la [Playlist].
     * @author Ukabi
     */
    fun duration(): Int = mapNotNull { it.duration }.reduceOrNull { acc, i -> acc + i } ?: 0
}

class PlaylistSerializer : KSerializer<Playlist> by ListSerializer(TrackData.serializer()) as KSerializer<Playlist>

/**
 * Renvoie une [List] de [n] des précédents [Log] dont la [Track] peut être jouée.
 * @author Ukabi
 */
fun suggest(n: Int = 5): List<TrackData> = Log.getRandom(n).map { it.toTrackData() }