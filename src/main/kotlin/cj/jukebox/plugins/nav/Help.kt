package cj.jukebox.plugins.nav

import cj.jukebox.config
import cj.jukebox.templates.MainTemplate
import cj.jukebox.utils.UserSession

import io.ktor.server.html.*
import kotlinx.html.*

/**
 * La page d'aide, qui affiche des informations.
 * @param[user] L'utilisateur de la session.
 * @author khatharsis
 */
class Help(user: UserSession) : MainTemplate(
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
                    text("Pour rechercher des chansons sur Youtube ou Soundcloud, vous pouvez utiliser les bangs !yt ou !sc.")
                    text("Pour lire des titres en provenance de Bandcamp, Soundcloud, Jamendo, Youtube, ou bien des fichiers en lien direct, vous pouvez directement mettre l'URL de la musique.")
                }
            }
        }
    }
)