package cj.jukebox.templates

import cj.jukebox.utils.UserSession

import io.ktor.server.html.*
import kotlinx.html.*

class Logout(val user: UserSession) : Template<HTML> {
    private val header = TemplatePlaceholder<Header>()
    override fun HTML.apply() {
        insert(Header(), header)
        body("text-center") {
            div("container") {
                h1 { text("Attention !") }
                h2 { text("Se déconnecter désassociera ce PC du compte ${user.name}.")}
                form {
                    action="/logout"
                    method=FormMethod.post
                    classes=setOf("form-signin")
                    button {
                        classes="btn btn-lg btn-primary btn-block".split(" ").toSet()
                        type=ButtonType.submit
                        text("Oui, allez !")
                    }
                }
                a("/", "Ah non merde !", "btn btn-lg")
            }
        }
    }
}