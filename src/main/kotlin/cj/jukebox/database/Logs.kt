package cj.jukebox.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.time.Instant

object Logs : IntIdTable() {
    val trackId = reference("trackId", Songs).nullable()
    val userId = reference("userId", Users).nullable()

    val time = integer("time")
        .default(Instant.now().epochSecond.toInt())
}

class Log(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Log>(Logs)

    var trackId by Song optionalReferencedOn Logs.trackId
    var userId  by User optionalReferencedOn Logs.userId
    var time    by Logs.time
}