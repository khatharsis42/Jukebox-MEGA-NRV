package cj.jukebox.plugins.statistics

import io.ktor.server.html.*
import kotlinx.html.*

/**
 * Une classe permettant de représenter rapidement le tableau fourni.
 * La première ligne de cet Array doit être le nom des colonnes.
 * @param[name] Le nom que l'on veut donner à ce tableau.
 * @param[content] Tableau que l'on veut transformer en tableau HTML. Sa première ligne doit contenir les titres.
 * @author khatharsis
 */
class StatsColumn(val name: String, content: List<List<Any>>) : Template<FlowContent> {
    private val colNames = content.firstOrNull()?.map { it.toString() }
    private val columns = content.slice(1..content.size).map { list -> list.map { it.toString() } }

    override fun FlowContent.apply() {
        div("statdisplay") {
            h3 {
                style = "text-align:center"
                text(name)
            }
            table {
                if (colNames != null) {
                    thead {
                        tr {
                            colNames.forEach { th { text(it) } }
                        }
                    }
                }
                tbody {
                    columns.forEach { tr { it.forEach { th { text(it) } } } }
                }
            }
        }
    }
}