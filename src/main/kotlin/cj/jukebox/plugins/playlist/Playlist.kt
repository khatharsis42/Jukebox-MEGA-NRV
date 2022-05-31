package cj.jukebox.plugins.playlist

import cj.jukebox.database.Log
import cj.jukebox.database.Logs
import cj.jukebox.database.Track
import cj.jukebox.database.User
import cj.jukebox.player
import cj.jukebox.utils.SigName
import cj.jukebox.utils.UserSession
import cj.jukebox.utils.sendSignal

typealias Playlist = MutableList<Log>

enum class Direction(val direction: String) {
    TOP("top"),
    UP("up"),
    DOWN("down")
}

/**
 * Échange les [Track] étant aux emplacements [from] et [to].
 * @author Ukabi
 */
private fun Playlist.move(from: Int, to: Int) {
    this[from] = this[to].also { this[to] = this[from] }
        .also { player.sendSignal(SigName.SIGUSR2) }
}

/**
 * Échange l'élément [from] de la [Playlist] avec l'élément au-dessus ou en dessous,
 * selon la valeur de [direction].
 * Ne procède pas à l'échange si le bord de la [Playlist] est dépassé.
 * @author Ukabi
 */
fun Playlist.move(from: Int, direction: Direction) =
    when (direction) {
        Direction.TOP -> add(0, removeAt(from))
        Direction.UP -> (from - 1).takeIf { it >= 0 }?.let { move(from, it) }
        Direction.DOWN -> (from + 1).takeIf { it < size }?.let { move(from, it) }
    }

/**
 * Retrouve [log] dans la [Playlist], puis l'échange avec l'élément au-dessus ou en dessous,
 * selon la valeur de [direction]. Le cas échéant, ne fait rien.
 * Ne procède pas à l'échange si le bord de la [Playlist] est dépassé.
 * @author Ukabi
 */
fun Playlist.move(log: Log, direction: Direction) =
    indexOf(log).takeIf { it >= 0 }?.let { move(it, direction) }

/**
 * Vérifie si la [track] fournie peut être jouée, puis l'ajoute à la [Playlist].
 * Garde aussi la trace de l'[user] l'ayant requêtée.
 * Créé aussi une nouvelle occurrence dans la table [Logs].
 * @author Ukabi
 */
fun Playlist.addIfPossible(track: Track, user: User): Boolean =
    !track.blacklisted && !track.obsolete && add(Log.createLog(track, user))
        .also { if (it) player.sendSignal(SigName.SIGUSR2) }

/**
 * Vérifie si la [Track] correspondant à la [trackId] fournie peut être jouée, puis l'ajoute à la [Playlist].
 * Garde aussi la trace de l'[user] l'ayant requêtée.
 * Créé aussi une nouvelle occurrence dans la table [Logs].
 * @author Ukabi
 */
fun Playlist.addIfPossible(trackId: Int, user: User): Boolean =
    Track.getTrack(trackId)?.let { addIfPossible(it, user) } ?: false

/**
 * Vérifie si la [Track] correspondant à la [trackId] fournie peut être jouée, puis l'ajoute à la [Playlist].
 * Garde aussi la trace de l'[user] l'ayant requêtée.
 * Créé aussi une nouvelle occurrence dans la table [Logs].
 * @author Ukabi
 */
fun Playlist.addIfPossible(trackId: Int, user: UserSession): Boolean = addIfPossible(trackId, user.toUser())

/**
 * Vérifie si la [track] fournie peut être jouée, puis l'ajoute à la [Playlist].
 * Garde aussi la trace de l'[user] l'ayant requêtée.
 * Créé aussi une nouvelle occurrence dans la table [Logs].
 * @author Ukabi
 */
fun Playlist.addIfPossible(track: Track, user: UserSession): Boolean = addIfPossible(track, user.toUser())

/**
 * Vérifie si la [track] fournie fait partie de la [Playlist], puis la supprime.
 * Efface aussi l'occurrence de [Log] précédemment créée si [delete] est fourni.
 * @return L'index de la track supprimée, *null* le cas échéant.
 * @author Ukabi
 */
fun Playlist.removeIfPossible(track: Track, delete: Boolean = true): Int? =
    map { it.trackId }
        .indexOf(track).takeIf { it >= 0 }
        .also { index -> index?.let { removeAt(it).apply { if (delete) this.delete() } } }
        ?.also { player.sendSignal(if (it == 0) SigName.SIGUSR1 else SigName.SIGUSR2) }

/**
 * Vérifie si la [Track] correspondant à la [trackId] fournie fait partie de la [Playlist], puis la supprime.
 * Efface aussi l'occurrence de [Log] précédemment créée si [delete] est fourni.
 * @return L'index de la track supprimée, *null* le cas échéant.
 * @author Ukabi
 */
fun Playlist.removeIfPossible(trackId: Int, delete: Boolean = true): Int? =
    Track.getTrack(trackId)?.let { removeIfPossible(it, delete) }

/**
 * Renvoie la somme des durées (en secondes) de chacune des [Track] de la [Playlist].
 * @author Ukabi
 */
fun Playlist.duration(): Int = mapNotNull { it.trackId.duration }.reduce { acc, i -> acc + i }

/**
 * Renvoie une [List] de [n] des précédents [Log] dont la [Track] peut être jouée.
 * @author Ukabi
 */
fun suggest(n: Int = 5): List<Log> = Log.getRandom(n)
