package cj.jukebox.plugins.statistics

import cj.jukebox.templates.MainTemplate
import cj.jukebox.utils.UserSession
import io.ktor.server.html.*
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.style

/**
 * Statistiques globales du jukebox.
 * @param[user] L'utilisateur de la session.
 * @author khatharsis
 */
class GlobalStatistics(user: UserSession) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        private val statsColumn = TemplatePlaceholder<StatsColumn>()
        override fun FlowContent.apply() {
            div("container") {
                div("statistics") {
                    div("col-xl-6 statcol") {
                        h2 {
                            style = "text-align:center"
                            text("Users with most play counts:")
                        }
                        insert(StatsColumn("All Time", giveTestArray()), statsColumn)
                        insert(StatsColumn("Last seven days", giveTestArray()), statsColumn)
                        insert(StatsColumn("Last 24 hours", giveTestArray()), statsColumn)
                    }
                    div("col-xl-6 statcol") {
                        h2 {
                            style = "text-align:center"
                            text("Tracks with most play counts:")
                        }
                        insert(StatsColumn("All Time", giveTestArray()), statsColumn)
                        insert(StatsColumn("Last seven days", giveTestArray()), statsColumn)
                        insert(StatsColumn("Last 24 hours", giveTestArray()), statsColumn)
                    }
                }
            }
        }
    }
)