package cj.jukebox.database

import cj.jukebox.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater

import java.time.Instant

object Logs : IntIdTable() {
    val trackId = reference("trackId", Tracks)
    val userId = reference("userId", Users)
    val time = integer("time")
}

/**
 * Représentation objet d'une ligne de la table [Logs]
 * @author Ukabi
 */
class Log(id: EntityID<Int>) : IntEntity(id) {
    var trackId by Track referencedOn Logs.trackId
    var userId by User referencedOn Logs.userId
    var time by Logs.time

    companion object : IntEntityClass<Log>(Logs) {
        /**
         * Créé une nouvelle occurrence dans la table [Logs].
         */
        fun createLog(track: Track, user: User) =
            database.dbQuery {
                Log.new {
                    trackId = track
                    userId = user
                    time = getNow()
                }
            }

        /**
         * Récupère les [n] derniers [Log].
         */
        fun getLogs(n: Int): List<Log> =
            database.dbQuery {
                Log
                    .all()
                    .orderBy(Logs.time to SortOrder.DESC)
                    .limit(n)
                    .toList()
            }

        /**
         * Récupère les [Log] ayant été créés dans les [timeDelta] dernières secondes.
         * Si [timeDelta] est [Nothing], renvoie tous les [Log].
         */
        fun getLogs(timeDelta: Int? = null): List<Log> =
            database.dbQuery {
                Log
                    .timeFilter(timeDelta)
                    .orderBy(Logs.time to SortOrder.DESC)
                    .toList()
            }

        /**
         * Pour un·e [user] et une [track] donné·e·s, renvoie le nombre
         * de fois que l'[user] a ajouté [track] à la playlist.
         * Possibilité de limiter la recherche aux [timeDelta] dernières secondes, si fourni.
         */
        fun getCount(user: User, track: Track, timeDelta: Int? = null): Int =
            database.dbQuery {
                Logs
                    .slice(Logs.id.count())
                    .select { (Logs.trackId eq track.id).and(Logs.userId eq user.id).timeFilter(timeDelta) }
                    .map { it[Logs.id.count()].toInt() }
                    .first()
            }

        /**
         * Récupère les [Log] correspondant à [user].
         * Filtre les [Log] des [timeDelta] dernières secondes si fourni.
         */
        fun getUserLogs(user: User, timeDelta: Int? = null): List<Log> =
            database.dbQuery {
                Log
                    .find { (Logs.userId eq user.id).timeFilter(timeDelta) }
                    .orderBy(Logs.time to SortOrder.DESC)
                    .toList()
            }

        /**
         * Récupère les [Log] correspondant à [track].
         * Filtre les [Log] des [timeDelta] dernières secondes si fourni.
         */
        fun getTrackLogs(track: Track, timeDelta: Int? = null): List<Log> =
            database.dbQuery {
                Log
                    .find { (Logs.trackId eq track.id).timeFilter(timeDelta) }
                    .orderBy(Logs.time to SortOrder.DESC)
                    .toList()
            }

        /**
         * Pour un·e [user] donné·e, renvoie le nombre de fois qu'[user] a
         * ajouté des [Track] à la playlist.
         * Possibilité de limiter la recherche aux [timeDelta] dernières secondes, si fourni.
         */
        fun getUserCount(user: User, timeDelta: Int? = null): Int =
            getUserLogs(user, timeDelta).size

        /**
         * Pour une [track] donnée, renvoie le nombre de fois que [track] a été ajoutée à la playlist.
         * Possibilité de limiter la recherche aux [timeDelta] dernières secondes, si fourni.
         */
        fun getTrackCount(track: Track, timeDelta: Int? = null): Int =
            getTrackLogs(track, timeDelta).size

        /**
         * Renvoie une [List] de [Pair<Int, User>] triée par ordre décroissant,
         * où l'entier correspond au nombre de fois qu'un·e [User] apparait dans [Log].
         * Filtre les [Log] des [timeDelta] dernières secondes si fourni.
         * Coupe la liste aux [n] [User] les plus actif·ve·s, si fourni.
         */
        fun getMostActiveUsers(timeDelta: Int? = null, n: Int? = null): List<Pair<Int, User>> =
            database.dbQuery {
                Logs
                    .slice(Logs.userId.count(), Logs.userId)
                    .timeFilter(timeDelta)
                    .groupBy(Logs.userId)
                    .orderBy(Logs.userId.count() to SortOrder.DESC)
                    .let { if (n != null) it.limit(n) else it }
                    .map { it[Logs.userId.count()].toInt() to User[it[Logs.userId]] }
            }

        /**
         * Renvoie une [List] de [Pair<Int, Track>] triée par ordre décroissant,
         * où l'entier correspond au nombre de fois qu'une [Track] apparait dans [Log].
         * Filtre les [Log] des [timeDelta] dernières secondes si fourni.
         * Coupe la liste aux [n] [Track] les plus écoutées, si fourni.
         */
        fun getMostPlayedTracks(timeDelta: Int? = null, n: Int? = null): List<Pair<Int, Track>> =
            database.dbQuery {
                Logs
                    .slice(Logs.userId.count(), Logs.userId)
                    .timeFilter(timeDelta)
                    .groupBy(Logs.userId)
                    .orderBy(Logs.trackId.count() to SortOrder.DESC)
                    .let { if (n != null) it.limit(n) else it }
                    .map { it[Logs.trackId.count()].toInt() to Track[it[Logs.trackId]] }
            }

        /**
         * Pour une [track] donnée, renvoie une [List] de [Pair<Int, User>] triée
         * par ordre décroissant, où l'entier correspond au nombre de fois qu'un·e [User]
         * apparait dans [Logs].
         * Filtre les [Log] des [timeDelta] dernières secondes si fourni.
         * Coupe la liste aux [n] [User] les plus actif·ve·s, si fourni.
         */
        fun getMostActiveUsers(track: Track, timeDelta: Int? = null, n: Int? = null): List<Pair<Int, User>> =
            database.dbQuery {
                Logs
                    .slice(Logs.userId.count(), Logs.userId)
                    .select { (Logs.trackId eq track.id).timeFilter(timeDelta) }
                    .groupBy(Logs.userId)
                    .orderBy(Logs.userId.count() to SortOrder.DESC)
                    .let { if (n != null) it.limit(n) else it }
                    .map { it[Logs.userId.count()].toInt() to User[it[Logs.userId]] }
            }

        /**
         * Pour un·e [user] donné·e, renvoie une [List] de [Pair<Int, Track>] triée
         * par ordre décroissant, où l'entier correspond au nombre de fois qu'une [Track]
         * apparait dans [Logs].
         * Filtre les [Log] des [timeDelta] dernières secondes si fourni.
         * Coupe la liste aux [n] [Track] les plus écoutées, si fourni.
         */
        fun getMostPlayedTracks(user: User, timeDelta: Int? = null, n: Int? = null): List<Pair<Int, Track>> =
            database.dbQuery {
                Logs
                    .slice(Logs.trackId.count(), Logs.trackId)
                    .select { (Logs.userId eq user.id).timeFilter(timeDelta) }
                    .groupBy(Logs.trackId)
                    .orderBy(Logs.trackId.count() to SortOrder.DESC)
                    .let { if (n != null) it.limit(n) else it }
                    .map { it[Logs.trackId.count()].toInt() to Track[it[Logs.trackId]] }
            }
    }
}

/**
 * Raccourci pour renvoyer le timestamp actuel.
 */
private fun getNow() = Instant.now().epochSecond.toInt()

/**
 * Raccourci pour filtrer [Logs] aux [timeDelta] dernières secondes.
 */
private fun Op<Boolean>.timeFilter(timeDelta: Int?): Op<Boolean> =
    let {
        if (timeDelta != null) {
            it.and(Logs.time greater (getNow() - timeDelta))
        } else it
    }

/**
 * Raccourci pour filtrer [Logs] aux [timeDelta] dernières secondes.
 */
private fun FieldSet.timeFilter(timeDelta: Int?): Query =
    let {
        if (timeDelta != null) {
            it.select(Logs.time greater (getNow() - timeDelta))
        } else it.selectAll()
    }

/**
 * Raccourci pour filtrer [Logs] aux [timeDelta] dernières secondes.
 */
private fun Log.Companion.timeFilter(timeDelta: Int?): SizedIterable<Log> =
    let {
        if (timeDelta != null) {
            it.find { Logs.time greater (getNow() - timeDelta) }
        } else it.all()
    }
