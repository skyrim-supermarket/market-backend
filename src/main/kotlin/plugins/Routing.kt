package com.mac350.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mac350.models.*
import com.mac350.repositories.ClientRepo
import com.mac350.tables.*
import com.mac350.repositories.*
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
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun Application.configureRouting() {
    routing {
        staticFiles("/uploads", File("uploads"))

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

            val date = Date(System.currentTimeMillis()).toString()
            val newProduct = ProductRepository.newProduct(productName, priceGold.toLong(), description, standardDiscount.toLong(), specialDiscount.toLong(), date)

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

        get("/labels/{table}") {
            val table = call.parameters["table"]

            if(table == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid table name!")
                return@get
            }

            val childTable = getTableName(table.lowercase())

            if(childTable == null) {
                call.respond(HttpStatusCode.NotFound, "$table does not exist!")
                return@get
            }

            val columns = suspendTransaction { getLabelsAndTypes(ProductT, childTable) }

            val response = columns.map{(name, type) -> mapOf("name" to name, "type" to type)}

            call.respond(response)
            return@get
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
            }

            val client = AccountRepository.getClientById(id)

            if (client == null) {
                call.respond(HttpStatusCode.NotFound, "This client doesn't exist!")
                return@get
            }

            call.respond(daoToClient(client))
            return@get
        }

        post("/registerAdmin") {
            val register = call.receive<RegisterAdminAndCarrocaBoy>()

            val account = AccountRepository.getAccountByEmail(register.email)
            if(account != null) {
                call.respond(HttpStatusCode.Unauthorized, "This user already exists!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val newAccount = AccountRepository.newAccount(register.username, register.email, register.password, "admin", date)
            AccountRepository.newAdmin(newAccount)

            call.respond(HttpStatusCode.OK, "Admin successfully registered")
            return@post
        }

        post("/registerCarrocaBoy") {
            val register = call.receive<RegisterAdminAndCarrocaBoy>()

            val account = AccountRepository.getAccountByEmail(register.email)
            if(account != null) {
                call.respond(HttpStatusCode.Unauthorized, "This user already exists!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val newAccount = AccountRepository.newAccount(register.username, register.email, register.password, "carrocaboy", date)
            AccountRepository.newCarrocaBoy(newAccount)

            call.respond(HttpStatusCode.OK, "CarroçaBoy successfully registered")
            return@post
        }

        post("/registerCashier") {
            val register = call.receive<RegisterCashier>()

            val account = AccountRepository.getAccountByEmail(register.email)
            if(account != null) {
                call.respond(HttpStatusCode.Unauthorized, "This user already exists!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val newAccount = AccountRepository.newAccount(register.username, register.email, register.password, "client", date)
            AccountRepository.newCashier(newAccount, register.section)

            call.respond(HttpStatusCode.OK, "Cashier successfully registered")
            return@post
        }

        post("/registerClient") {
            val register = call.receive<Register>()

            val account = AccountRepository.getAccountByEmail(register.email)
            if(account != null) {
                call.respond(HttpStatusCode.Unauthorized, "This user already exists!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val newAccount = AccountRepository.newAccount(register.username, register.email, register.password, "client", date)
            AccountRepository.newClient(newAccount, register.address)
            SaleRepository.newCart(newAccount, date)

            val token = generateToken(register.email, "client")
            call.respond(mapOf("token" to token))
            return@post
        }

        post("/login") {
            val login = call.receive<Login>()
            val account = AccountRepository.getAccountByEmail(login.email)
            if(account == null || !AccountRepository.checkPw(login.password, account.password)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid user or invalid password!")
                return@post
            }

            val token = generateToken(account.email, account.type)
            call.respond(mapOf("token" to token))
            return@post
        }

        get("/cartSize/{email}") {
            val email = call.parameters["email"]
            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@get
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null) {
                call.respond(HttpStatusCode.NotFound, "This account does not exist!")
                return@get
            }

            val cart = SaleRepository.getCartByAccount(account)
            val cartSize = SaleProductRepository.getCartSize(cart.id.value)
            call.respond(mapOf("size" to cartSize))
            return@get
        }

        post("/addToCart/{idProduct}/{email}") {
            val idProduct = call.parameters["idProduct"]?.toIntOrNull()
            val email = call.parameters["email"]

            if(idProduct == null || email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            val product = ProductRepository.getProductById(idProduct)

            if(account == null) {
                call.respond(HttpStatusCode.NotFound, "This account does not exist!")
                return@post
            }

            if(product == null) {
                call.respond(HttpStatusCode.NotFound, "This product does not exist!")
                return@post
            }

            if(product.stock <= 0) {
                call.respond(HttpStatusCode.Unauthorized, "This product is out of stock!")
                return@post
            }

            val cart = SaleRepository.getCartByAccount(account)

            SaleProductRepository.newSaleProduct(cart, product)

            call.respond(HttpStatusCode.OK, "Product successfully added!")
            return@post
        }

        delete("/deleteFromCart/{idProduct}/{email}") {
            val idProduct = call.parameters["idProduct"]?.toIntOrNull()
            val email = call.parameters["email"]

            if(idProduct == null || email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@delete
            }

            val account = AccountRepository.getAccountByEmail(email)
            val product = ProductRepository.getProductById(idProduct)

            if(account == null) {
                call.respond(HttpStatusCode.NotFound, "This account does not exist!")
                return@delete
            }

            if(product == null) {
                call.respond(HttpStatusCode.NotFound, "This product does not exist!")
                return@delete
            }

            val cart = SaleRepository.getCartByAccount(account)
            val saleProduct = SaleProductRepository.getSaleProduct(cart.id.value, account.id.value)

            if(saleProduct == null) {
                call.respond(HttpStatusCode.NotFound, "This product is not in the cart!")
                return@delete
            }

            val date = Date(System.currentTimeMillis()).toString()
            SaleRepository.alterTotalPrice(cart, product, saleProduct.quantity, 0, date)
            SaleProductRepository.deleteSaleProduct(saleProduct)
        }

        post("/alterQuantity/{idProduct}/{email}/{quantity}") {
            val idProduct = call.parameters["idProduct"]?.toIntOrNull()
            val email = call.parameters["email"]
            val quantity = call.parameters["quantity"]?.toLongOrNull()

            if(idProduct == null || email.isNullOrBlank() || quantity == null || quantity < 0) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val product = ProductRepository.getProductById(idProduct)
            if(product == null) {
                call.respond(HttpStatusCode.NotFound, "This product does not exist!")
                return@post
            }

            if(product.stock < quantity) {
                call.respond(HttpStatusCode.Unauthorized, "This quantity is over the product's stock!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null) {
                call.respond(HttpStatusCode.NotFound, "This account does not exist!")
                return@post
            }

            val sale = SaleRepository.getCartByAccount(account)
            val saleProduct = SaleProductRepository.getSaleProduct(sale.id.value, idProduct)

            if(saleProduct == null) {
                call.respond(HttpStatusCode.NotFound, "This product to sale assignment does not exist!")
                return@post
            }

            val previousQuantity = saleProduct.quantity

            val date = Date(System.currentTimeMillis()).toString()
            SaleProductRepository.alterQuantity(saleProduct, quantity)
            SaleRepository.alterTotalPrice(sale, product, previousQuantity, quantity, date)

            call.respond(HttpStatusCode.OK, "Quantity successfully altered!")
            return@post
        }

        post("/finishOnlineSale/{email}") {
            val email = call.parameters["email"]
            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null || account.type != "client") {
                call.respond(HttpStatusCode.BadRequest, "Invalid user!")
                return@post
            }

            val cart = SaleRepository.getCartByAccount(account)
            val saleProducts = SaleProductRepository.getSaleProductsBySale(cart.id.value)

            for (product in saleProducts) {
                val check = ProductRepository.getProductById(product.idProduct)
                if(check == null || product.quantity > check.stock) {
                    call.respond(HttpStatusCode.Unauthorized, "Couldn't finish your purchase!")
                    return@post
                }
            }

            val date = Date(System.currentTimeMillis()).toString()
            SaleRepository.finishOnlineSale(cart, date)
            SaleRepository.newCart(account, date)

            for (product in saleProducts) {
                val productDAO = ProductRepository.getProductById(product.idProduct)
                ProductRepository.alterStock(productDAO!!, product.quantity)
            }
        }

        post("/acceptOrder/{email}/{idSale}") {
            val email = call.parameters["email"]
            val idSale = call.parameters["idSale"]?.toIntOrNull()

            if(email.isNullOrBlank() || idSale == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null) {
                call.respond(HttpStatusCode.NotFound, "This account does not exist!")
                return@post
            }

            val sale = SaleRepository.getAvailableSaleById(idSale)
            if(sale == null) {
                call.respond(HttpStatusCode.NotFound, "This sale does not exist or has already been accepted!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()

            SaleRepository.assignCarrocaBoy(sale, account, date)

            call.respond(HttpStatusCode.OK, "You have accepted this order!")
            return@post
        }

        post("/deliverOrder/{email}/{idSale}") {
            val email = call.parameters["email"]
            val idSale = call.parameters["idSale"]?.toIntOrNull()

            if(email.isNullOrBlank() || idSale == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null) {
                call.respond(HttpStatusCode.NotFound, "This account does not exist!")
                return@post
            }

            val sale = SaleRepository.getAvailableSaleById(idSale)
            if(sale == null) {
                call.respond(HttpStatusCode.NotFound, "This sale does not exist or has already been accepted!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            SaleRepository.finishSale(sale, date)
            val carrocaBoy = AccountRepository.getCarrocaBoyByEmail(email)
            if(carrocaBoy == null) {
                call.respond(HttpStatusCode.NotFound, "This CarroçaBoy does not exist!")
                return@post
            }

            AccountRepository.addCommissionToEmployee(carrocaBoy, sale.totalPriceGold, date)

            call.respond(HttpStatusCode.OK, "You have delivered this order!")
            return@post
        }

        post("") {

        }
    }
}
