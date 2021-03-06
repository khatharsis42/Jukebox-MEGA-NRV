package cj.jukebox.plugins.statistics

import cj.jukebox.database
import cj.jukebox.database.Log
import cj.jukebox.database.Track
import cj.jukebox.database.User
import cj.jukebox.templates.MainTemplate
import cj.jukebox.utils.UserSession
import cj.jukebox.utils.day
import cj.jukebox.utils.week
import io.ktor.server.html.*
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.style
import java.time.Duration

/**
 * Statistiques globales du jukebox.
 * @param[user] L'utilisateur de la session.
 * @author khatharsis
 */
class GlobalStatistics(user: UserSession) : MainTemplate(user, content = object : Template<FlowContent> {
    private val statsColumn = TemplatePlaceholder<StatsColumn>()
    override fun FlowContent.apply() {
        div("container") {
            div("statistics") {
                div("col-xl-6 statcol") {
                    h2 {
                        style = "text-align:center"
                        text("Users with most play counts:")
                    }
                    insert(StatsColumn("All Time", prepareUserData(10)), statsColumn)
                    insert(StatsColumn("Last 24 hours", prepareUserData(10, day)), statsColumn)
                    insert(StatsColumn("Last seven days", prepareUserData(10, week)), statsColumn)
                }
                div("col-xl-6 statcol") {
                    h2 {
                        style = "text-align:center"
                        text("Tracks with most play counts:")
                    }
                    insert(StatsColumn("All Time", prepareTrackData(10)), statsColumn)
                    insert(StatsColumn("Last 24 hours", prepareTrackData(10, day)), statsColumn)
                    insert(StatsColumn("Last seven days", prepareTrackData(10, week)), statsColumn)
                }
            }
        }
    }
})

/**
 * Prépare les données nécessaires à l'affichage des statistiques des [User].
 * @author Ukabi
 */
private fun prepareUserData(n: Int, timeDelta: Duration? = null): List<List<Any>> = listOf(listOf("User", "Count")) +
        database.dbQuery {
            Log.getMostActiveUsers(timeDelta, n).map { listOf(it.second.name, it.first) }
        }

/**
 * Prépare les données nécessaires à l'affichage des statistiques des [Track].
 * @author Ukabi
 */
private fun prepareTrackData(n: Int, timeDelta: Duration? = null): List<List<Any>> = listOf(listOf("Track", "Count")) +
        database.dbQuery {
            Log.getMostPlayedTracks(timeDelta, n).map { listOf(it.second.track.toString(), it.first) }
        }