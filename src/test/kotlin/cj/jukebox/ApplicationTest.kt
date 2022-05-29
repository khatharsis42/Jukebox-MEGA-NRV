package cj.jukebox

import cj.jukebox.plugins.nav.nav
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            authentication {
                provider("auth-session") {
                    authenticate {  }
                }
            }
            nav()
        }
        client.get("/status").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("status", bodyAsText())
        }
    }
}