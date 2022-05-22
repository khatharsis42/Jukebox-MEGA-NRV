package cj.jukebox.plugins.statistics

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
class History(user: UserSession, n: Int = 50) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        private val statsColumn = TemplatePlaceholder<StatsColumn>()
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
                        insert(StatsColumn("", giveTestArray()), statsColumn)
                    }
                }
            }
        }
    }
)