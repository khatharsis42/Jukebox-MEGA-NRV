package templates

import cj.jukebox.config
import cj.jukebox.templates.Header
import io.ktor.server.html.*
import kotlinx.html.*

class Auth : Template<HTML>{
    private val header = TemplatePlaceholder<Header>()
    override fun HTML.apply() {
        insert(Header(), header)
        body("text-center") {
            div("container") {
                form("/auth", classes="form-signin", method=FormMethod.post) {
                    //TODO: trouver le moyen de faire des flashs.
                    h1("h3 mb-3 font-weight-normal") { text("C'est koi ton p'tit nom ?") }
                    label("sr-only") {
                        htmlFor ="inputUser"
                        text("Pseudo")
                    }
                    input(InputType.text) {
                        id="inputUser"
                        name="user"
                        classes=setOf("form-control")
                        placeholder="Pseudo"
                        required=true
                        autoFocus=true
                    }
                    label("sr-only") {
                        htmlFor ="inputPassword"
                        text("Mot de passe")
                    }
                    input(InputType.text) {
                        id="inputPassword"
                        name="pass"
                        classes=setOf("form-control")
                        placeholder="Mot de passe"
                        required=true
                        autoFocus=true
                    }
                    button {
                        classes = "btn btn-lg btn-primary btn-block".split(" ").toSet()
                        name="action"
                        type=ButtonType.submit
                        value="login"
                        text("Connexion")
                    }
                    p("mt-1 mb-1 text-muted") {text("- ou -")}
                    button {
                        classes = "btn btn-lg btn-primary btn-block".split(" ").toSet()
                        name="action"
                        type=ButtonType.submit
                        value="new"
                        text("S'enregistrer")
                    }
                }
            }
        }
    }
}