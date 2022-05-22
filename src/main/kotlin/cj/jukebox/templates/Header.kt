package cj.jukebox.templates

import cj.jukebox.config
import io.ktor.server.html.*
import kotlinx.html.*

class Header : Template<HTML> {
    override fun HTML.apply() {
        head {
            meta {
                charset = "utf-8"
                content = "width=device-width, initial-scale=1, shrink-to-fit=no"
                name = "viewport"
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