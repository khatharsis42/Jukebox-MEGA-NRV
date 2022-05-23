package cj.jukebox.templates

import io.ktor.server.html.*
import kotlinx.css.form
import kotlinx.css.h2
import kotlinx.html.*
import templates.MainTemplate
import java.io.File

class Settings(user:String) : MainTemplate(
    user,
    content = object: Template<FlowContent> {
        override fun FlowContent.apply() {
            div("container") {
                h2 {
                    style="text-align:center;"
                    text("Settings")
                }
                form {
                    action="/settings"
                    method=FormMethod.post
                    fieldSet {
                        div {
                            style="text-align:center;"
                            label { htmlFor = "style"; text("Styles") }
                            br
                            br
                            select {
                                id="style"
                                name="style"
                                File("src/main/resources/styles/custom")
                                    .listFiles()
                            //                                    .walk()
                                    ?.filter { it.name.endsWith(".css") }
                                    ?.forEach {
                                    option {
                                        value=it.name
                                        text(it.name)
                                    }
                                }
                            }
                            br
                            br
                            input {
                                id="submit"
                                name="submit"
                                value="Send"
                                type=InputType.submit
                            }
                        }
                    }
                }
            }
        }
    }
)