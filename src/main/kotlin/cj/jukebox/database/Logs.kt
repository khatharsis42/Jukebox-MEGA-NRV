package cj.jukebox.database

import cj.jukebox.database
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.time.Instant

object Logs : IntIdTable() {
    val trackId = reference("trackId", Tracks).nullable()
    val userId = reference("userId", Users).nullable()

    val time = integer("time")
        .default(Instant.now().epochSecond.toInt())
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

        fun getUserLog(user: User, timeDelta: Int? = null) =
            database.dbQuery {

            }
    }
}