package cj.jukebox.templates

import cj.jukebox.database.Song
import cj.jukebox.database.User
import io.ktor.server.html.*
import kotlinx.css.*
import kotlinx.html.*
import templates.MainTemplate

/**
 * Une classe permettant de représenter rapidement le tableau fourni.
 * La première ligne de cet Array doit être le nom des colonnes.
 */
private class StatsColumn(val name: String, content: Array<Array<String>>) :
    Template<FlowContent> {
    val colNames = content.first()
    val columns = content.copyOfRange(1, content.size)
    override fun FlowContent.apply() {
        div("statdisplay") {
            h3 {
                style = "text-align:center"
                text(name)
            }
            table {
                thead {
                    tr {
                        colNames.forEach { th { text(it) } }
                    }
                }
                tbody {
                    columns.forEach { tr { it.forEach { th { text(it) } } } }
                }
            }
        }
    }
}

class GlobalStatistics(user: String) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        private val statsColumn = TemplatePlaceholder<StatsColumn>()
        override fun FlowContent.apply() {
            div("container") {
                div("statistics") {
                    div("col-xl-6 statcol") {
                        h2 {
                            style="text-align:center"
                            text("Users with most play counts:")
                        }
                        insert(StatsColumn("All Time", giveTestArray()), statsColumn)
                        insert(StatsColumn("Last seven days", giveTestArray()), statsColumn)
                        insert(StatsColumn("Last 24 hours", giveTestArray()), statsColumn)
                    }
                    div("col-xl-6 statcol") {
                        h2 {
                            style="text-align:center"
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

class UserStatistics(user: String, userStats: User) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        private val statsColumn = TemplatePlaceholder<StatsColumn>()
        override fun FlowContent.apply() {
            div("container") {
                div {
                    style = "text-align:center"
                    h1 {text("Statistiques de ${userStats.name}")}
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

class TrackStatistics(user: String, track: Song) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        private val statsColumn = TemplatePlaceholder<StatsColumn>()
        override fun FlowContent.apply() {
            div("container") {
                div {
                    style="text-align:center"
                    h1 { text("Statistiques de ${track.song}") }
                    when {
                        track.obsolete -> h2 { style="color:red"; text("Track obsolete")}
                        track.blacklisted -> h2 { style="color:red"; text("Track blacklisted")}
                        else -> form {
                            action="/add/${track.id}"
                            method = FormMethod.post
                            input {type=InputType.submit; name="Add"; value="Ajouter à la file d'attente."}
                        }
                    }
                }
                div("statistics") {
                    div("col statcol") {
                        insert(StatsColumn("All Time", giveTestArray()), statsColumn)
                    }
                }
            }
        }
    }
)

class History(user:String, n: Int = 50) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        private val statsColumn = TemplatePlaceholder<StatsColumn>()
        override fun FlowContent.apply() {
            div("container") {
                div {
                    style="text-align:center;"
                    h1 {text("Historique des $n dernières musiques.")}
                }
                div("statistics") {
                    style="padding:30px;"
                    div("col statcol") {
                        style="margin: auto;"
                        insert(StatsColumn("", giveTestArray()), statsColumn)
                    }
                }
            }
        }
    }
)

private fun giveTestArray() = arrayOf(
    arrayOf("Name 1", "Name 2"),
    arrayOf("Test1", "1"),
    arrayOf("Test2", "6"),
    arrayOf("Test3", "9")
)