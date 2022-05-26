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

class Log(id: EntityID<Int>) : IntEntity(id) {
    var trackId by Track referencedOn Logs.trackId
    var userId  by User referencedOn Logs.userId
    var time    by Logs.time

    companion object : IntEntityClass<Log>(Logs) {
        fun createLog(track: Track, user: User) =
            database.dbQuery {
                Log.new {
                    trackId = track
                    userId = user
                    time = getNow()
                }
            }

        fun getLogs(n: Int): List<Log> =
            database.dbQuery { Log
                .all()
                .orderBy(Logs.time to SortOrder.DESC)
                .limit(n)
                .toList()
            }

        fun getLogs(timeDelta: Int? = null): List<Log> =
            database.dbQuery { Log
                .timeFilter(timeDelta)
                .orderBy(Logs.time to SortOrder.DESC)
                .toList()
            }

        fun getUserLogs(user: User, timeDelta: Int? = null): List<Log> =
            database.dbQuery { Log
                .find { (Logs.userId eq user.id).timeFilter(timeDelta) }
                .orderBy(Logs.time to SortOrder.DESC)
                .toList()
            }

        fun getTrackLogs(track: Track, timeDelta: Int? = null): List<Log> =
            database.dbQuery { Log
                .find { (Logs.trackId eq track.id).timeFilter(timeDelta) }
                .orderBy(Logs.time to SortOrder.DESC)
                .toList()
            }

        fun getMostActiveUsers(timeDelta: Int? = null, n: Int? = null): List<Pair<Int, User>> =
            database.dbQuery { Logs
                .slice(Logs.userId.count(), Logs.userId)
                .timeFilter(timeDelta)
                .groupBy(Logs.userId)
                .orderBy(Logs.userId.count() to SortOrder.DESC)
                .let { if (n != null) it.limit(n) else it }
                .map { it[Logs.userId.count()].toInt() to User[it[Logs.userId]] }
            }

        fun getMostPlayedTracks(timeDelta: Int? = null, n: Int? = null): List<Pair<Int, Track>> =
            database.dbQuery { Logs
                .slice(Logs.userId.count(), Logs.userId)
                .timeFilter(timeDelta)
                .groupBy(Logs.userId)
                .orderBy(Logs.trackId.count() to SortOrder.DESC)
                .let { if (n != null) it.limit(n) else it }
                .map { it[Logs.trackId.count()].toInt() to Track[it[Logs.trackId]] }
            }

        fun getMostActiveUsers(track: Track, timeDelta: Int? = null, n: Int? = null): List<Pair<Int, User>> =
            database.dbQuery { Logs
                .slice(Logs.userId.count(), Logs.userId)
                .select { (Logs.trackId eq track.id).timeFilter(timeDelta) }
                .groupBy(Logs.userId)
                .orderBy(Logs.userId.count() to SortOrder.DESC)
                .let { if (n != null) it.limit(n) else it }
                .map { it[Logs.userId.count()].toInt() to User[it[Logs.userId]] }
            }

        fun getMostPlayedTracks(user: User, timeDelta: Int? = null, n: Int? = null): List<Pair<Int, Track>> =
            database.dbQuery { Logs
                .slice(Logs.trackId.count(), Logs.trackId)
                .select { (Logs.userId eq user.id).timeFilter(timeDelta) }
                .groupBy(Logs.trackId)
                .orderBy(Logs.trackId.count() to SortOrder.DESC)
                .let { if (n != null) it.limit(n) else it }
                .map { it[Logs.trackId.count()].toInt() to Track[it[Logs.trackId]] }
            }
    }
}

private fun getNow() = Instant.now().epochSecond.toInt()

private fun Op<Boolean>.timeFilter(timeDelta: Int?): Op<Boolean> =
    this.let {
        if (timeDelta != null) {
            it.and(Logs.time greater (getNow() - timeDelta))
        } else it
    }

private fun FieldSet.timeFilter(timeDelta: Int?): Query =
    this.let {
        if (timeDelta != null) {
            it.select(Logs.time greater (getNow() - timeDelta))
        } else it.selectAll()
    }

private fun Log.Companion.timeFilter(timeDelta: Int?): SizedIterable<Log> =
    this.let {
        if (timeDelta != null) {
            it.find { Logs.time greater (getNow() - timeDelta) }
        } else it.all()
    }
