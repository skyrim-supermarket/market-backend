package com.mac350.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mac350.models.*
import com.mac350.repositories.ClientRepo
import com.mac350.tables.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import java.util.Date
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun Application.configureRouting() {
    routing {
        /*route("/clients") {
            get {

            }
        }*/

        post("/registerClient") {
            val register = call.receive<Register>()
            val check = suspendTransaction {
                val account = AccountDAO.find {AccountT.email eq register.email }.firstOrNull()
                if(account == null) {
                    val date = Date(System.currentTimeMillis()).toString()
                    val newAccount = AccountDAO.new {
                        this.username = register.username
                        this.email = register.email
                        this.password = register.password
                        this.type = "client"
                        this.createdAt = date
                        this.updatedAt = date
                        this.lastRun = date
                    }

                    ClientDAO.new {
                        this.account = newAccount
                        this.isSpecialClient = false
                        this.address = register.address
                    }
                } else null
            }

            if(check != null) {
                val token = generateToken(register.email, "client")
                call.respond(mapOf("token" to token))
                return@post
            } else {
                call.respond(HttpStatusCode.Unauthorized, "This user already exists!")
                return@post
            }
        }

        post("/login") {
            val login = call.receive<Login>()
            val check = suspendTransaction {
                val account = AccountDAO.find {AccountT.email eq login.email }.firstOrNull()
                if(account != null && account.password == login.password) {
                    account
                } else null
            }

            if(check != null) {
                val email = check.email
                val type = check.type
                val token = generateToken(email, type)
                call.respond(mapOf("token" to token))
                return@post
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid user or invalid password!")
                return@post
            }
        }
    }
}
