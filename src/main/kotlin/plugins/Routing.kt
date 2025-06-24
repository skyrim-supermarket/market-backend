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
import java.io.File
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun Application.configureRouting() {
    routing {
        static("/uploads") {
            files("uploads")
        }
        post("/products") {
            val recv = call.receive<ProductFilterTest>()
            val type = recv.type.lowercase()
            val page = recv.page - 1
            val pageSize = recv.pageSize

            val query = suspendTransaction {
                if(type=="all products") {
                    ProductDAO.all().map(::daoToCard)
                } else if(
                    type=="ammunition" ||
                    type=="armor" ||
                    type=="books" ||
                    type=="clothing" ||
                    type=="food" ||
                    type=="ingredients" ||
                    type=="miscellaneous" ||
                    type=="ores" ||
                    type=="potions" ||
                    type=="soul gems" ||
                    type=="weapons"
                    ) {
                    ProductDAO.find { ProductT.type eq type }.map(::daoToCard)
                } else null
            }

            if (query == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameter!")
                return@post
            } else {
                val totalCount = query.size
                val pagedQuery = query.drop(page*pageSize).take(pageSize)
                call.respond(HttpStatusCode.OK, mapOf("results" to QueryResults(pagedQuery, totalCount)))
                return@post
            }
        }

        post("/newAmmunition") {
            val (fields, files) = parseMultiPart(call.receiveMultipart())
            val productName = fields["productName"]
            val priceGold = fields["priceGold"]
            val description = fields["description"]
            val standardDiscount = fields["standardDiscount"]
            val specialDiscount = fields["specialDiscount"]
            val magical = fields["magical"]
            val craft = fields["craft"]
            val speed = fields["speed"]
            val gravity = fields["gravity"]
            val category = fields["category"]

            val imageBytes = files["image"]


            if(productName == null || priceGold == null || description == null || standardDiscount == null || specialDiscount == null
                || magical == null || craft == null || speed == null || gravity == null || category == null) {
                call.respond(HttpStatusCode.BadRequest, "Every field must be filled!")
                return@post
            }

            val newProduct = createNewProduct(productName, priceGold.toLong(), description, standardDiscount.toLong(), specialDiscount.toLong())

            suspendTransaction {
                AmmunitionDAO.new {
                    this.product = newProduct
                    this.magical = magical
                    this.craft = craft
                    this.speed = speed.toDouble()
                    this.gravity = gravity.toDouble()
                    this.category = category
                }
            }

            val uploadDir = File("uploads")
            if(!uploadDir.exists()) uploadDir.mkdirs()

            val imageName = "${newProduct.id}.png"
            if(imageBytes!=null) {
                File(uploadDir, imageName).writeBytes(imageBytes!!)

                suspendTransaction {
                    val findProduct = ProductDAO.findById(newProduct.id.value)
                    findProduct?.image = "/uploads/$imageName"
                }
            }

            call.respond(HttpStatusCode.OK, "Ammunition successfully added!")
            return@post
        }

        post("/newArmor") {

        }

        post("/newBook") {

        }

        post("/newClothing") {

        }

        post("/newFood") {

        }

        post("/newIngredient") {

        }

        post("/newMiscellany") {

        }

        post("/newOre") {

        }

        post("/newPotion") {

        }

        post("/newSoulGem") {

        }

        post("/newWeapon") {

        }

        get("/clients") {
            val clients = suspendTransaction {
                ClientDAO.all().map(::daoToClient)
            }

            if(clients.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No client was found!")
                return@get
            } else {
                call.respond(clients)
                return@get
            }
        }

        get("/clientByEmail/{email}") {
            val email = call.parameters["email"]

            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid email!")
                return@get
            } else {
                val client = suspendTransaction {
                    val account = AccountDAO.find { AccountT.email eq email }.firstOrNull()
                    account?.let { acc ->
                        ClientDAO.find { ClientT.account eq acc.id }.firstOrNull()
                    }
                }

                if (client == null) {
                    call.respond(HttpStatusCode.NotFound, "This client doesn't exist!")
                    return@get
                } else {
                    call.respond(daoToClient(client))
                    return@get
                }
            }
        }

        get("/clientById/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if(id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
                return@get
            } else {
                val client = suspendTransaction {
                    val account = AccountDAO.find { AccountT.id eq id }.firstOrNull()
                    account?.let { acc ->
                        ClientDAO.find { ClientT.account eq acc.id }.firstOrNull()
                    }
                }

                if (client == null) {
                    call.respond(HttpStatusCode.NotFound, "This client doesn't exist!")
                    return@get
                } else {
                    call.respond(daoToClient(client))
                    return@get
                }
            }
        }

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

                    val newClient = ClientDAO.new {
                        this.account = newAccount
                        this.isSpecialClient = false
                        this.address = register.address
                    }

                    SaleDAO.new {
                        this.idClient = newAccount
                        this.totalPriceGold = 0
                        this.totalQuantity = 0
                        this.finished = false
                        this.status = "Carrinho"
                        this.createdAt = date
                        this.updatedAt = date
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
