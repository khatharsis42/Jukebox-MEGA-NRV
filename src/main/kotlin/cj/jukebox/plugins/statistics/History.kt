package cj.jukebox.plugins.statistics

import cj.jukebox.database.Log
import cj.jukebox.templates.MainTemplate
import cj.jukebox.utils.UserSession

import io.ktor.server.html.*
import kotlinx.html.*

/**
 * Historique des musiques passées sur le jukebox.
 * @param[user] L'utilisateur de la session.
 * @param[n] Nombre de musiques que l'on souhaite voir. (On devrait vraiment plutôt faire des pages).
 * @author khatharsis
 */
class History(user: UserSession, n: Int = 50) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
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
                        if(data.isNotEmpty()) {
                            insert(StatsColumn("", data), statsColumn)
                        } else {
                            div("statdisplay") {
                                h3 {
                                    style = "text-align:center"
                                    text("Y'a r :/")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
)

private fun prepareData(n: Int): Array<Array<String>> =
    Log.getLastLogs(n).map { log ->
        arrayOf(log.userId?.name, log.trackId?.track, log.trackId?.id?.value)
            .map { it.toString() }.toTypedArray()
    }.toTypedArray()
