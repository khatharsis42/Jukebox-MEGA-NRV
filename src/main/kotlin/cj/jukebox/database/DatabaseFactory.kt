package cj.jukebox.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

/**
 * Helper class permettant une meilleure interaction avec la base de données.
 * @param[path] l'emplacement du fichier SQLite.
 * @author Ukabi
 */
class DatabaseFactory(path: String) {
    private val database = connect(path)
    init { createSchema() }

    /**
     * Se connecte à la database SQLite selon le [path] fourni.
     */
    private fun connect(path: String): Database {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        return Database.connect("jdbc:sqlite:${path}", driver = "org.sqlite.JDBC")
    }

    /**
     * Créé les tables si elles n'ont pas encore été créées.
     */
    private fun createSchema() {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(Tracks)
            SchemaUtils.create(Logs)
        }
    }

    /**
     * Interagit avec [database] selon le [block] donné.
     * La dernière instruction du [block] sert aussi de renvoi.
     */
    fun <T> dbQuery(block: () -> T): T = transaction(database) { block() }
}