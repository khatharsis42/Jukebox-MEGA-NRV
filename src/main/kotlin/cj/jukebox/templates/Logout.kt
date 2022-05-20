package templates

import io.ktor.server.html.*
import kotlinx.html.*

class Logout : Template<FlowContent> {
    private val user = Placeholder<FlowContent>()
    override fun FlowContent.apply() {
        header {
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1, shrink-to-fit=no"
                charset = "utf-8"
            }
            link {
                rel = "stylesheet"
                href = ""
                // TODO: I hate frontend, kill me plz thx
            }
        }
    }
}