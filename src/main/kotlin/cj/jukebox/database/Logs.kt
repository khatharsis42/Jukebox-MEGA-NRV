package cj.jukebox.database

import cj.jukebox.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and

import java.time.Instant

private fun getNow() = Instant.now().epochSecond.toInt()

object Logs : IntIdTable() {
    val trackId = reference("trackId", Tracks).nullable()
    val userId = reference("userId", Users).nullable()

    val time = integer("time").default(getNow())
}

class Log(id: EntityID<Int>) : IntEntity(id) {
    var trackId by Track optionalReferencedOn Logs.trackId
    var userId  by User optionalReferencedOn Logs.userId
    var time    by Logs.time

    companion object : IntEntityClass<Log>(Logs) {
        fun createLog(track: Track, user: User) =
            database.dbQuery {
                Log.new {
                    trackId = track
                    userId = user
                }
            }

        fun getUserLog(user: User, timeDelta: Int? = null): List<Log> =
            database.dbQuery {
                Log.find {
                    if (timeDelta == null) {
                        Logs.userId eq user.id
                    } else {
                        (Logs.userId eq user.id) and (Logs.time greater (getNow() - timeDelta) )
                    }
                }.orderBy(Logs.time to SortOrder.DESC).toList()
            }

        fun getLastLogs(n: Int): List<Log> =
            database.dbQuery {
                Log.all().orderBy(Logs.time to SortOrder.DESC).limit(n).toList()
            }
    }
}