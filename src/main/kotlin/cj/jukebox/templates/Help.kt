package cj.jukebox.templates

import cj.jukebox.config
import io.ktor.server.html.*
import kotlinx.css.div
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.p
import templates.MainTemplate

class Help(user: String) : MainTemplate(
    user,
    content = object : Template<FlowContent> {
        override fun FlowContent.apply() {
            div("container") {
                p {
                    text("Le code source est disponible.")
                    a("https://github.com/khatharsis42/Jukebox-MEGA-NRV", "ici")
                    text("La version utilisée est ${config.data.APP_NAME}.")
                    //TODO: mettre la version là
                }
                //TODO: mettre les modules
                p {
                    text("Pour utiliser la recherche Youtube, entrez simplement les termes de votre recherche, puis appuyez sur la touche \"Entrée\"")
                    text("Pour rechercher des chansons sur Youtube ou Soundcloud, vous pouvez utiliser les bangs <code>!yt</code>, ou <code>!sc</code>.")
                    text("Pour lire des titres en provenance de Bandcamp, Soundcloud, Jamendo, Youtube, ou bien des fichiers en lien direct, vous pouvez directement mettre l'URL de la musique.")
                }
            }
        }
    }
)