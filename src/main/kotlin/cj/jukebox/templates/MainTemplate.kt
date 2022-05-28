package cj.jukebox.templates

import cj.jukebox.config
import cj.jukebox.utils.UserSession

import io.ktor.server.html.*
import kotlinx.html.*

/**
 * Template principal, contient non seulement les headers mais également les scripts.
 *
 * @param[user] L'utilisateur de la session.
 * @param[content] Le contenu principal de cette page.
 * @param[music] Valeur existant uniquement pour la page d'accueil, permet d'avoir un endroit reservé pour les musiques.
 * @author khatharsis
 */
open class MainTemplate(
    private val user: UserSession,
    private val content: Template<FlowContent>,
    private val music: Template<FlowContent>? = null,
) : Template<HTML> {
    private val flowTemplate = TemplatePlaceholder<Template<FlowContent>>()
    private val header = TemplatePlaceholder<Header>()
    override fun HTML.apply() {
        insert(Header(user.theme), header)
        body {
            onMouseOver = "pageStatus=true;"
            onMouseOut = "pageStatus=false;"
            div("container-fluid") {
                div("row") {
                    div("col-xl-8") {
                        style = "padding-left: .5em;padding-right: .5em;"
                        h1("display-3") { text(config.data.APP_NAME) }
                        // Barre de menu
                        div("clearfix") {
                            ul("nav") {
                                li("nav-item") {
                                    p("nav-link") {
                                        a("/app") { text("Accueil") }
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
                                        a("/logout") { text("Logout from ${user.name}") }
                                    }
                                }
                            }
                        }
                        if (music != null) this.insert(music, flowTemplate)
                    }
                    this.insert(content, flowTemplate)
                }
            }
            script(ScriptType.textJScript, "/assets/scripts/jquery-3.3.1.min.js") {}
            script(ScriptType.textJScript, "/assets/scripts/jquery.loadTemplate.min.js") {}
            script(ScriptType.textJScript, "/assets/scripts/main.js") {}
            script(ScriptType.textJScript, "/assets/scripts/popper.min.js") {}
            script(ScriptType.textJScript, "/assets/scripts/bootstrap.min.js") {}
        }
    }
}