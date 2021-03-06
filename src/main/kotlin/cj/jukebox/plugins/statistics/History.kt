package cj.jukebox.plugins.statistics

import cj.jukebox.database
import cj.jukebox.database.Log
import cj.jukebox.database.Track
import cj.jukebox.templates.MainTemplate
import cj.jukebox.utils.UserSession
import io.ktor.server.html.*
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.style

/**
 * Historique des musiques passées sur le jukebox.
 * @param[user] L'utilisateur de la session.
 * @param[n] Nombre de musiques que l'on souhaite voir. (On devrait vraiment plutôt faire des pages).
 * @author khatharsis
 */
class History(user: UserSession, n: Int = 50) : MainTemplate(user, content = object : Template<FlowContent> {
    private val statsColumn = TemplatePlaceholder<StatsColumn>()
    private val data = prepareData(n)

    override fun FlowContent.apply() {
        div("container") {
            div {
                style = "text-align:center;"
                h1 { text("Historique des $n dernières musiques.") }
            }
            div("statistics") {
                style = "padding:30px;"
                div("col statcol") {
                    style = "margin: auto;"
                    insert(StatsColumn(if (data.isNotEmpty()) "" else "Y'a r :/", data), statsColumn)
                }
            }
        }
    }
})

/**
 * Prépare les données nécessaires à l'affichage de l'historique.
 * @param[n] le nombre de [Track] à afficher.
 * @author Ukabi
 */
private fun prepareData(n: Int): List<List<Any>> = listOf(listOf("Name", "Track", "Track Count")) +
        database.dbQuery {
            Log.getLogs(n).map { listOf(it.user.name, it.track.track.toString(), Log.getTrackCount(it.track)) }
        }