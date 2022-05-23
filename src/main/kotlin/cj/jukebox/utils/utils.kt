package cj.jukebox.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import org.jetbrains.exposed.dao.id.EntityID

//data class UserSession(val user: User) : Principal
data class UserSession(val id: Int, val name: String, var theme: String?) : Principal


@JvmName("getUserSession1")
fun ApplicationCall.getUserSession(): UserSession? = sessions.get()

@JvmName("clearUserSession1")
fun ApplicationCall.clearUserSession() = sessions.clear<UserSession>()

fun ApplicationCall.getParam(name: String) = parameters.getOrFail(name)

fun CurrentSession.setUserSession(id: EntityID<Int>, name: String, theme: String?) = set(UserSession(id.value, name, theme))
