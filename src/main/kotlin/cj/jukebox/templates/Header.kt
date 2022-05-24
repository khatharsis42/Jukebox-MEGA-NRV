package cj.jukebox.templates

import cj.jukebox.config
import io.ktor.server.html.*
import kotlinx.html.*

/**
 * Un template HTML pour le Header.
 * @param styleSheet Le nom d'une des stylesheet disponible. Peut valoir null, auquel cas, elle n'est pas prise en compte.
 * @author khatharsis
 */
class Header(private val styleSheet: String? = null) : Template<HTML> {
    override fun HTML.apply() {
        head {
            meta {
                charset = "utf-8"
                content = "width=device-width, initial-scale=1, shrink-to-fit=no"
                name = "viewport"
                link("/assets/styles/custom/default.css", rel = "stylesheet", type = "text/css")
                if (styleSheet != null)
                    link("/assets/styles/custom/$styleSheet", rel = "stylesheet", type = "text/css")
                link("/assets/styles/custom/default.css", rel = "stylesheet", type = "text/css")
                link("/assets/favicon.ico", rel = "shortcut icon")
                link("/assets/styles/bootstrap.min.css", rel = "stylesheet")
                link(
                    "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css",
                    rel = "stylesheet"
                )
            }
            title(config.data.APP_NAME)
        }
    }

}