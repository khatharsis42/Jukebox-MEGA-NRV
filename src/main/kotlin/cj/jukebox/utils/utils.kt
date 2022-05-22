package cj.jukebox.utils

import cj.jukebox.database.User
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

data class UserSession(val user: User) : Principal

@JvmName("getUserSession1")
fun ApplicationCall.getUserSession(): UserSession? = sessions.get()

@JvmName("clearUserSession1")
fun ApplicationCall.clearUserSession() = sessions.clear<UserSession>()

fun ApplicationCall.getParam(name: String) = parameters.getOrFail(name)

fun CurrentSession.setUserSession(user: User) = set(UserSession(user))
