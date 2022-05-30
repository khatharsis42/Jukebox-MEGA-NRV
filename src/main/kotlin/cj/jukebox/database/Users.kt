package cj.jukebox.database

import cj.jukebox.database
import cj.jukebox.utils.encrypt

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

object Users : IntIdTable() {
    val name = varchar("name", 50).uniqueIndex()
    val pass = varchar("pass", 50)

    val theme = varchar("theme", 50).nullable()
}

/**
 * Représentation objet d'une ligne de la table [Users].
 * @author Ukabi
 */
class User(id: EntityID<Int>) : IntEntity(id) {
    var name by Users.name
    var pass by Users.pass
    var theme by Users.theme

    /**
     * Met à jour le [theme] de l'[User].
     */
    @JvmName("setTheme1")
    fun setTheme(style: String?) = database.dbQuery { theme = style }

    companion object : IntEntityClass<User>(Users) {
        /**
         * Crée un·e [User] à partir des informations fournies.
         */
        fun createUser(userName: String, password: String): User =
            database.dbQuery {
                User.new {
                    name = userName
                    pass = password.encrypt()
                }
            }

        /**
         * Cherche un·e [User] étant donné son [id].
         */
        fun findUser(id: Int): User? = database.dbQuery { User.findById(id) }

        /**
         * Cherche un·e [User] correspondant au [name] donné.
         * Filtre aussi par [pass], si fourni.
         */
        fun findUser(name: String, pass: String? = null): User? =
            database.dbQuery {
                User
                    .find { (Users.name eq name).passFilter(pass) }
                    .limit(1)
                    .firstOrNull()
            }
    }
}

/**
 * Raccourci pour filtrer [Users] selon le [pass] fourni.
 */
private fun Op<Boolean>.passFilter(pass: String?): Op<Boolean> {
    val encryptedPass = pass?.encrypt()
    return if (encryptedPass != null) this.and(Users.pass eq encryptedPass) else this
}
