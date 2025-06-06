package com.mac350

import com.mac350.plugins.configureSecurity
import com.mac350.plugins.generateToken
import io.ktor.client.request.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.*
import java.util.Date

class ApplicationTest {

    @Test
    fun testSecurity() = testApplication {
        application {
            configureSecurity()
        }

        val token = generateToken("seila@email.com")
        val date = Date(System.currentTimeMillis() + 3600000)
        val decoded = JWT.decode(token)
        val expires = decoded.expiresAt
        val email = decoded.getClaim("email").asString()
        assertEquals(email, "seila@email.com")
        assertTrue(expires.time - date.time <= 1000)
    }

}
