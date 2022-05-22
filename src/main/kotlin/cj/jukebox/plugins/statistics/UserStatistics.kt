package cj.jukebox.plugins.statistics

import cj.jukebox.database.User
import cj.jukebox.templates.MainTemplate
import cj.jukebox.utils.UserSession
import io.ktor.server.html.*
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.style

/**
 * Statistiques d'un seul utilisateur.
 * @param[user] L'utilisateur de la session.
 * @param[lookedUpUser] L'utilisateur dont on veut voir les stats.
 * @author khatharsis
 */
class UserStatistics(user: UserSession, lookedUpUser: User) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        private val statsColumn = TemplatePlaceholder<StatsColumn>()
        override fun FlowContent.apply() {
            div("container") {
                div {
                    style = "text-align:center"
                    h1 { text("Statistiques de ${lookedUpUser.name}") }
                }
                div("statistics") {
                    div("col-xl-6 statcol") {
                        insert(StatsColumn("All Time", giveTestArray()), statsColumn)
                    }
                    div("col-xl-6 statcol") {
                        insert(StatsColumn("Last month", giveTestArray()), statsColumn)
                    }
                }
            }
        }
    }
)