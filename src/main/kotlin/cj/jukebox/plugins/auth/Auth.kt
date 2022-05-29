package cj.jukebox.plugins.auth

import cj.jukebox.templates.Header
import io.ktor.server.html.*
import kotlinx.html.*

/**
 * Classe de template pour l'authentification.
 *
 * @param correct Indique si l'authentification précédente était correcte.
 * Vaut true de base pour des raisons évidentes.
 * Permet de faire spawner un texte en rouge indiquant à l'utilisateur que le login n'a pas réussi.
 * TODO: remplacer ça par une enum dans le futur, pour traiter tous les problèmes possibles.
 * @author khatharsis
 */
class Auth(private val correct: Boolean = true) : Template<HTML> {
    /**
     * Le header.
     */
    private val header = TemplatePlaceholder<Header>()

    /**
     * Fonction utilisée pour générer du JavaScript utilisé par les boutons.
     * Créé une copie des inputs pour les envoyer vers l'URL donnée en paramètre.
     * @param action L'URL vers laquelle faire la requête POST.
     */
    private fun createJS(action: String) =
        """username = document.getElementById("inputUser").cloneNode();
           password = document.getElementById("inputPassword").cloneNode();
           var form = document.createElement("form");
           form.setAttribute('method', "post");
           form.appendChild(username);
           form.appendChild(password);
           form.setAttribute('action', "$action");
           form.style.display = "none";
           document.body.appendChild(form);
           form.submit();
           """.trimIndent()

    override fun HTML.apply() {
        insert(Header(), header)
        body("text-center") {
            div("container") {
                h1("h3 mb-3 font-weight-normal") { text("C'est koi ton p'tit nom ?") }
                if (!correct) h2 { style = "color:red"; text("Mot de passe ou nom d'utilisateur incorrect.") }
                label("sr-only") {
                    htmlFor = "inputUser"
                    text("Pseudo")
                }
                input(InputType.text) {
                    id = "inputUser"
                    name = "user"
                    classes = setOf("form-control")
                    placeholder = "Pseudo"
                    required = true
                    autoFocus = true
                }
                label("sr-only") {
                    htmlFor = "inputPassword"
                    text("Mot de passe")
                }
                input(InputType.text) {
                    id = "inputPassword"
                    name = "pass"
                    classes = setOf("form-control")
                    placeholder = "Mot de passe"
                    required = true
                    autoFocus = true
                }
                br
                button {
                    classes = "btn btn-lg btn-primary btn-block".split(" ").toSet()
                    name = "action"
                    type = ButtonType.submit
                    value = "login"
                    id = "login"
                    onClick = createJS("/login")
                    text("Connexion")
                }
                p("mt-1 mb-1 text-muted") { text("- ou -") }
                button {
                    classes = "btn btn-lg btn-primary btn-block".split(" ").toSet()
                    name = "action"
                    type = ButtonType.submit
                    value = "signup"
                    id = "signup"
                    onClick = createJS("/signup")
                    text("S'enregistrer")
                }
            }
        }
    }
}