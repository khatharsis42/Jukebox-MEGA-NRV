package cj.jukebox.templates

import cj.jukebox.database.User

import io.ktor.server.html.*
import kotlinx.html.*

class Accueil(user: User) : MainTemplate(
    user,
    music = object : Template<FlowContent> {
        override fun FlowContent.apply() {
            div("form-group") {
                input(InputType.search) {
                    placeholder = "Recherchez un morceau ou collez une URL"
                    autoComplete = false
                    autoFocus = true
                    classes = setOf("form-control", "form-control-lg")
                    dir = Dir.ltr
                    id = "query"
                    name = "query"
                    spellCheck = false
                    style = "text-align: left; direction: ltr;"
                }
            }
            div("form-group form-inline") {
                div("col-0") {
                    img() {
                        classes = setOf("icon","btn-volume")
                        style = "margin: auto;"
                    }
                }
                div("col-10") {
                    input(InputType.range) {
                        classes = setOf("volume-slider", "form-control")
                        id = "volume-slider"
                        max = "100"
                        min = "0"
                    }
                }
            }
            ul("list-group") { id = "search_results" }
            ul("list-group") { id = "playlist" }
        }
    },
    content = object : Template<FlowContent> {
        override fun FlowContent.apply() {
            div("col-xl-4") {
                style = "padding-left: .5em;padding-right: .5em;"
                div { id = "YT" }
                div("container") {
                    div("row") {
                        div("col3 yt-btn") {
                            button {
                                classes = setOf("btn", "btn-outline-primary")
                                id = "pause"
                                type = ButtonType.submit
                                i("fa fa-play")
                                text("/")
                                i("fa fa-pause")
                            }
                        }
                        div("col2 yt-btn") {
                            button {
                                classes = setOf("btn", "btn-outline-primary")
                                id = "rewind"
                                type = ButtonType.submit
                                title = "-10s"
                                i("fa fa-fast-backward")
                            }
                        }
                        div("col2 yt-btn") {
                            button {
                                classes = setOf("btn", "btn-outline-primary")
                                id = "advance"
                                type = ButtonType.submit
                                title = "+10s"
                                i("fa fa-fast-forward")
                            }
                        }
                        div("col-4 yt-btn") {
                            input(InputType.search) {
                                // aria-expanded=false
                                style = "text-align: center; direction: ltr;"
                                spellCheck = false
                                autoFocus = true
                                autoComplete = false
                                placeholder = "00:00"
                                name = "jump"
                                id = "jump"
                                dir = Dir.ltr
                                classes = setOf("form-control", "form-control-lg")
                            }
                        }
                    }
                }
                div("clearfix") {
                    p("display-4 float-left") { text("Suggestions") }
                    p("float-right lead py-4") {
                        a("javascript:void(0)") { id = "refresh-suggestions"; text("Rafraîchir") }
                        text(" ")
                        a("javascript:void(0)") { id = "toggle-YT"; text("Afficher/Masquer la vidéo") }
                    }
                }
                ul("list-group") {
                    id = "suggestions"
                }
            }
        }
    }
)