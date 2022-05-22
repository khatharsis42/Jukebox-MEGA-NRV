package cj.jukebox.database

import cj.jukebox.config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class DatabaseFactory(path: String) {
    private val database = connect(path)
    init { createSchema() }

    private fun connect(path: String): Database {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        return Database.connect("jdbc:sqlite:${path}", driver = "org.sqlite.JDBC")
    }

    private fun createSchema() {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(Songs)
            SchemaUtils.create(Logs)
        }
    }

    fun <T> dbQuery(block: () -> T): T = transaction(database) { block() }
}