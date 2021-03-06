package cj.jukebox.utils

import cj.jukebox.database.User
import cj.jukebox.database.Users

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

import org.jetbrains.exposed.dao.id.EntityID

/**
 * Représente une session utilisateur·ice.
 * @param[id] [name] [theme] les données utiles à la session.
 * @author Ukabi
 */
data class UserSession(val id: Int, val name: String, var theme: String?) : Principal {
    /**
     * Convertit la [UserSession] en l'occurrence de la table [Users] correspondante.
     */
    fun toUser(): User = User.findUser(id)!!  // can assert because [User] exists if [UserSession] exists too
}

/**
 * Raccourci pour récupérer la session de l'utilisateur·ice.
 * @author Ukabi
 */
@JvmName("getUserSession1")
fun ApplicationCall.getUserSession(): UserSession? = sessions.get()

/**
 * Raccourci pour mettre fin à la session de l'utilisateur·ice.
 * @author Ukabi
 */
@JvmName("clearUserSession1")
fun ApplicationCall.clearUserSession() = sessions.clear<UserSession>()

/**
 * Raccourci pour accéder au paramètre [name] d'un formulaire.
 * @author Ukabi
 */
fun ApplicationCall.getParam(name: String): String = parameters.getOrFail(name)

/**
 * Raccourci pour créer une [UserSession] étant données les informations de l'utilisateur·ice.
 * @author Ukabi
 */
fun CurrentSession.setUserSession(id: EntityID<Int>, name: String, theme: String?) =
    set(UserSession(id.value, name, theme))