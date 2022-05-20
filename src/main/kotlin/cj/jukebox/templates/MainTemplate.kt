package templates

import cj.jukebox.config
import io.ktor.server.html.*
import kotlinx.css.Position
import kotlinx.css.script
import kotlinx.html.*

open class MainTemplate(
    private val user: String,
    private val content: Template<FlowContent>,
    private val music: Template<FlowContent>? = null
) : Template<HTML> {
    private val jukeboxName = config.data.APP_NAME
    private val temp = TemplatePlaceholder<Template<FlowContent>>()
    override fun HTML.apply() {
        lang = "fr"
        head {
            meta {
                charset = "utf-8"
                content = "width=device-width, initial-scale=1, shrink-to-fit=no"
                name = "viewport"
                link("assets/styles/custom/default.css", rel = "stylesheet", type="text/css")
                link("assets/favicon.ico", rel = "shortcut icon")
                link("assets/styles/bootstrap.min.css", rel = "stylesheet")
                link(
                    "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css",
                    rel = "stylesheet"
                )
            }
            title(jukeboxName)
        }
        body {
            onMouseOver = "pageStatus=true;"
            onMouseOut = "pageStatus=false;"
            div("container-fluid") {
                div("row") {
                    div("col-xl-8") {
                        style = "padding-left: .5em;padding-right: .5em;"
                        h1("display-3") { text(jukeboxName) }
                        // Barre de menu
                        div("clearfix") {
                            ul("nav") {
                                li("nav-item") {
                                    p("nav-link") {
                                        a("/app") { text("Acceuil") }
                                    }
                                }
                                li("nav-item") {
                                    p("nav-link") {
                                        a("/statistics") { text("Statistics") }
                                    }
                                }
                                li("nav-item") {
                                    p("nav-link") {
                                        a("/history") { text("History") }
                                    }
                                }
                                li("nav-item") {
                                    p("nav-link") {
                                        a("/settings") { text("Settings") }
                                    }
                                }
                                li("nav-item") {
                                    p("nav-link") {
                                        a("/help") { text("Help") }
                                    }
                                }
                                li("nav-item") {
                                    p("nav-link") {
                                        a("/logout") { text("Logout from $user") }
                                    }
                                }
                            }
                        }
                        if (music != null) this.insert(music, temp)
                    }
                    this.insert(content, temp)
                }
            }
            script(ScriptType.textJScript, "assets/scripts/jquery-3.3.1.min.js") {}
            script(ScriptType.textJScript, "assets/scripts/jquery.loadTemplate.min.js") {}
            script(ScriptType.textJScript, "assets/scripts/main.js") {}
            script(ScriptType.textJScript, "assets/scripts/popper.min.js") {}
            script(ScriptType.textJScript, "assets/scripts/bootstrap.min.js") {}
        }
    }
}