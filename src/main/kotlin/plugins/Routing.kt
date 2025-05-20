package com.mac350.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mac350.models.Client
import com.mac350.repositories.ClientRepo
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.configureRouting(repository: ClientRepo) {
    routing {
        route("/clients") {
            get {
                val clients = repository.allClients()
                call.respond(clients)
            }

            get("/specialClients/{vip}") {
                val param = call.parameters["vip"]
                if (param == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                var vip = false
                if(param=="false") vip = true
                try {
                    val specialClients = repository.specialClients(vip)

                    if (specialClients.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(specialClients)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }

            }

            get("/byEmail/{email}") {
                val email = call.parameters["email"]
                if (email == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val client = repository.clientByEmail(email)
                if (client == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(email)
            }

            post {
                try {
                    val client = call.receive<Client>()
                    repository.addClient(client)
                    call.respond(HttpStatusCode.NoContent)
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{email}") {
                val email = call.parameters["email"]
                if (email == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                if (repository.removeClient(email)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
