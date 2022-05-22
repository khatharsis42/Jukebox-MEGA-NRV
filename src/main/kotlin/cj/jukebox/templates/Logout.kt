package cj.jukebox.templates

import cj.jukebox.database.User

import io.ktor.server.html.*
import kotlinx.html.*

/**
 * La page pour se déconnecter de la session.
 * @param[user] L'utilisateur de la session.
 * @author khatharsis
 */
class Logout(val user: User) : Template<HTML> {
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