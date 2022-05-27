package cj.jukebox.plugins.playlist

import cj.jukebox.database.*
import cj.jukebox.utils.UserSession

typealias Playlist = MutableList<Pair<Int, Track>>

/**
 * Vérifie si la [track] fournie peut être jouée, puis l'ajoute à la [Playlist].
 * Garde aussi la trace de l'[user] l'ayant requêtée.
 * Créé aussi une nouvelle occurrence dans la table [Logs].
 * @author Ukabi
 */
fun Playlist.addIfPossible(track: Track, user: User): Boolean =
    !track.blacklisted && !track.obsolete && add(Pair(Log.createLog(track, user).time, track))

/**
 * Vérifie si la [Track] correspondant à la [trackId] fournie peut être jouée, puis l'ajoute à la [Playlist].
 * Garde aussi la trace de l'[user] l'ayant requêtée.
 * Créé aussi une nouvelle occurrence dans la table [Logs].
 * @author Ukabi
 */
fun Playlist.addIfPossible(trackId: Int, user: User): Boolean =
    Track.importFromId(trackId).let { (it != null) && addIfPossible(it, user) }

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
 * @author Ukabi
 */
fun Playlist.removeIfPossible(track: Track, delete: Boolean = false): Boolean =
    Log
        .getTrackLogs(track)
        .firstOrNull { Pair(it.time, it.trackId) in this }
        ?.let { if (delete) it.delete(); it }
        .let { (it != null) && remove(Pair(it.time, it.trackId)) }

/**
 * Vérifie si la [Track] correspondant à la [trackId] fournie fait partie de la [Playlist], puis la supprime.
 * Efface aussi l'occurrence de [Log] précédemment créée si [delete] est fourni.
 * @author Ukabi
 */
fun Playlist.removeIfPossible(trackId: Int, delete: Boolean = false): Boolean =
    Track.importFromId(trackId).let { (it != null) && removeIfPossible(it, delete) }

/**
 * Échange les [Track] étant aux emplacements [from] et [to].
 * @author Ukabi
 */
fun Playlist.move(from: Int, to: Int) {
    this[from] = this[to].also { this[to] = this[from] }
}

/**
 * Renvoie une [List] de [n] des précédents [Log] dont la [Track] peut être jouée.
 * TODO: probablement à refactor vers database.Logs pour en faire une fonction moins coûteuse en mémoire.
 * @author Ukabi
 */
fun Playlist.suggest(n: Int = 5): List<Log> =
    Log
        .getLogs()
        .filter { !it.trackId.blacklisted && !it.trackId.obsolete }
        .shuffled()
        .slice(0 until n)

/**
 * Renvoie la somme des durées (en secondes) de chacune des [Track] de la [Playlist].
 * @author Ukabi
 */
fun Playlist.duration(): Int = mapNotNull { it.second.duration }.reduce { acc, i -> acc + i }
