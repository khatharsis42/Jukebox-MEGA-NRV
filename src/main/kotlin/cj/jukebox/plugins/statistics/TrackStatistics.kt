package cj.jukebox.plugins.statistics

import cj.jukebox.database.Log
import cj.jukebox.database.Track
import cj.jukebox.templates.MainTemplate
import cj.jukebox.utils.UserSession
import io.ktor.server.html.*
import kotlinx.html.*
import java.time.Duration

/**
 * Pages de statistiques pour une musique.
 * @param[user] L'utilisateur de la session.
 * @param[track] La musique dont on veut voir les stats.
 * @author khatharsis
 */
class TrackStatistics(user: UserSession, track: Track) : MainTemplate(user, content = object : Template<FlowContent> {
    private val statsColumn = TemplatePlaceholder<StatsColumn>()
    override fun FlowContent.apply() {
        div("container") {
            div {
                style = "text-align:center"
                h1 { text("Statistiques de ${track.artist} - ${track.track}") }
                when {
                    track.obsolete -> h2 { style = "color:red"; text("Obsolete track") }
                    track.blacklisted -> h2 { style = "color:red"; text("Blacklisted track") }
                    else -> form {
                        action = "/add/${track.id}"
                        method = FormMethod.post
                        input {
                            type = InputType.submit
                            name = "Add"
                            value = "Ajouter à la file d'attente."
                        }
                    }
                }
            }
            div("statistics") {
                div("col statcol") {
                    insert(StatsColumn("All Time", prepareData(track)), statsColumn)
                }
            }
        }
    }
})

/**
 * Prépare les données nécessaires à l'affichage des statistiques.
 * @author Ukabi
 */
private fun prepareData(track: Track, timeDelta: Duration? = null): List<List<Any>> = listOf(listOf("User", "Count")) +
        Log.getMostActiveUsers(track, timeDelta).map { listOf(it.second.name, it.first) }