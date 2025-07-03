package com.mac350.plugins

import com.mac350.models.*
import com.mac350.tables.*
import com.mac350.repositories.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import java.util.Date
import java.io.File
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
        staticFiles("/uploads", File("uploads"))

        post("/products") {
            val recv = call.receive<Filter>()
            var type = recv.type.lowercase()
            type = UtilRepository.capitalizeFirstLetter(type)
            val page = recv.page - 1
            val pageSize = recv.pageSize

            val query = ProductRepository.getProducts(type)

            if (query == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameter!")
                return@post
            }

            val totalCount = query.size
            val pagedQuery = query.drop(page*pageSize).take(pageSize)
            call.respond(HttpStatusCode.OK, mapOf("results" to QueryResults(pagedQuery, totalCount)))
            return@post

        }

        get("/product/{idProduct}") {
            val idProduct = call.parameters["idProduct"]?.toIntOrNull()
            if(idProduct == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameter!")
                return@get
            }

            val product = ProductRepository.getProductById(idProduct)
            if(product == null) {
                call.respond(HttpStatusCode.NotFound, "This product does not exist!")
                return@get
            }

            val productDao = when(product.type) {
                "Ammunition" -> ProductRepository.getAmmunition(product.id.value)
                "Armor" -> ProductRepository.getArmor(product.id.value)
                "Books" -> ProductRepository.getBook(product.id.value)
                "Clothing" -> ProductRepository.getClothing(product.id.value)
                "Food" -> ProductRepository.getFood(product.id.value)
                "Ingredients" -> ProductRepository.getIngredient(product.id.value)
                "Miscellaneous" -> ProductRepository.getMiscellany(product.id.value)
                "Ores" -> ProductRepository.getOre(product.id.value)
                "Potions" -> ProductRepository.getPotion(product.id.value)
                "Soul gems" -> ProductRepository.getSoulGem(product.id.value)
                "Weapons" -> ProductRepository.getWeapon(product.id.value)
                else -> null
            }

            if(productDao == null) {
                call.respond(HttpStatusCode.NotFound, "This product does not exist!")
                return@get
            }

            val res = ProductRepository.convertDaoToProduct(productDao)

            if(res == null) {
                call.respond(HttpStatusCode.NotFound, "Invalid DAO type!")
                return@get
            }

            call.respond(res)
            return@get
        }

        post("/newProduct/{type}") {
            val type = call.parameters["type"]
            if(type.isNullOrBlank() || type !in ProductRepository.validTypes) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product type!")
                return@post
            }

            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = ProductRepository.reqFields[type] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val product = ProductRepository.newProduct(
                fields["productName"]!!,
                fields["priceGold"]!!.toLong(),
                fields["stock"]!!.toLong(),
                fields["description"]!!,
                type,
                date
            )

            when(type) {
                "Ammunition" -> ProductRepository.newAmmunition(product, fields["magical"]!!.toBoolean(), fields["craft"]!!, fields["speed"]!!.toDouble(), fields["gravity"]!!.toDouble(), fields["category"]!!)
                "Armor" -> ProductRepository.newArmor(product, fields["weight"]!!.toDouble(), fields["magical"]!!.toBoolean(), fields["craft"]!!, fields["protection"]!!.toDouble(), fields["heavy"]!!.toBoolean(), fields["category"]!!)
                "Books" -> ProductRepository.newBook(product)
                "Clothing" -> ProductRepository.newClothing(product)
                "Food" -> ProductRepository.newFood(product)
                "Ingredients" -> ProductRepository.newIngredient(product)
                "Miscellaneous" -> ProductRepository.newMiscellany(product)
                "Ores" -> ProductRepository.newOre(product)
                "Potions" -> ProductRepository.newPotion(product)
                "Soul gems" -> ProductRepository.newSoulGem(product)
                "Weapons" -> ProductRepository.newWeapon(product, fields["weight"]!!.toDouble(), fields["magical"]!!.toBoolean(), fields["craft"]!!, fields["damage"]!!.toLong(), fields["speed"]!!.toDouble(), fields["reach"]!!.toLong(), fields["stagger"]!!.toDouble(), fields["battleStyle"]!!, fields["category"]!!)
            }

            val uploadDir = File("uploads")
            if(!uploadDir.exists()) uploadDir.mkdirs()

            val imageBytes = files["image"]
            val imageName = "${product.id}.png"
            if(imageBytes!=null) {
                File(uploadDir, imageName).writeBytes(imageBytes)

                suspendTransaction {
                    val findProduct = ProductDAO.findById(product.id.value)
                    findProduct?.image = "/uploads/$imageName"
                }
            }

            call.respond(HttpStatusCode.OK, "$type successfully added!")
            return@post
        }

        post("/editProduct/{productId}") {
            val productId = call.parameters["productId"]?.toIntOrNull()

            if(productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val product = ProductRepository.getProductById(productId)

            if(product == null) {
                call.respond(HttpStatusCode.NotFound, "This product doesn't exist!")
                return@post
            }

            if(product.type !in ProductRepository.validTypes) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product type!")
                return@post
            }

            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = ProductRepository.reqFields[product.type] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            ProductRepository.editProduct(
                product,
                fields["productName"]!!,
                fields["priceGold"]!!.toLong(),
                fields["stock"]!!.toLong(),
                fields["description"]!!,
                date
            )

            val typeDao = when(product.type) {
                "Ammunition" -> ProductRepository.getAmmunition(productId)
                "Armor" -> ProductRepository.getArmor(productId)
                "Books" -> ProductRepository.getBook(productId)
                "Clothing" -> ProductRepository.getClothing(productId)
                "Food" -> ProductRepository.getFood(productId)
                "Ingredients" -> ProductRepository.getIngredient(productId)
                "Miscellaneous" -> ProductRepository.getMiscellany(productId)
                "Ores" -> ProductRepository.getOre(productId)
                "Potions" -> ProductRepository.getPotion(productId)
                "Soul gems" -> ProductRepository.getSoulGem(productId)
                "Weapons" -> ProductRepository.getWeapon(productId)
                else -> null
            }

            if(typeDao == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product type!")
                return@post
            }

            when(typeDao) {
                is AmmunitionDAO -> ProductRepository.editAmmunition(typeDao, fields["magical"]!!.toBoolean(), fields["craft"]!!, fields["speed"]!!.toDouble(), fields["gravity"]!!.toDouble(), fields["category"]!!)
                is ArmorDAO -> ProductRepository.editArmor(typeDao, fields["weight"]!!.toDouble(), fields["magical"]!!.toBoolean(), fields["craft"]!!, fields["protection"]!!.toDouble(), fields["heavy"]!!.toBoolean(), fields["category"]!!)
                is BookDAO -> ProductRepository.editBook(typeDao)
                is ClothingDAO -> ProductRepository.editClothing(typeDao)
                is FoodDAO -> ProductRepository.editFood(typeDao)
                is IngredientDAO -> ProductRepository.editIngredient(typeDao)
                is MiscellanyDAO -> ProductRepository.editMiscellany(typeDao)
                is OreDAO -> ProductRepository.editOre(typeDao)
                is PotionDAO -> ProductRepository.editPotion(typeDao)
                is SoulGemDAO -> ProductRepository.editSoulGem(typeDao)
                is WeaponDAO -> ProductRepository.editWeapon(typeDao, fields["weight"]!!.toDouble(), fields["magical"]!!.toBoolean(), fields["craft"]!!, fields["damage"]!!.toLong(), fields["speed"]!!.toDouble(), fields["reach"]!!.toLong(), fields["stagger"]!!.toDouble(), fields["battleStyle"]!!, fields["category"]!!)
            }

            val uploadDir = File("uploads")
            if(!uploadDir.exists()) uploadDir.mkdirs()

            val imageBytes = files["image"]
            val imageName = "${product.id}.png"
            if(imageBytes!=null) {
                File(uploadDir, imageName).writeBytes(imageBytes)

                suspendTransaction {
                    val findProduct = ProductDAO.findById(product.id.value)
                    findProduct?.image = "/uploads/$imageName"
                }
            }

            call.respond(HttpStatusCode.OK, "Product successfully edited!")
            return@post
        }

        get("/admins") {
            call.respond(AccountRepository.getAdmins())
            return@get
        }

        get("/carrocaboys") {
            call.respond(AccountRepository.getCarrocaBoys())
            return@get
        }

        get("/cashiers") {
            call.respond(AccountRepository.getCashiers())
            return@get
        }

        get("/clients") {
            call.respond(AccountRepository.getClients())
            return@get
        }

        get("/sales") {
            call.respond(SaleRepository.getSales())
            return@get
        }

        get("/labels/{table}") {
            var table = call.parameters["table"]

            if(table == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid table name!")
                return@get
            }

            table = table.lowercase()
            val childTable = UtilRepository.getTableName(table)

            if(childTable == null) {
                call.respond(HttpStatusCode.NotFound, "$table does not exist!")
                return@get
            }

            val columns = if(table=="admins" || table == "carrocaboys" || table == "cashiers") {
                suspendTransaction { UtilRepository.getLabelsAndTypes(AccountT, childTable) }
            } else {
                suspendTransaction { UtilRepository.getLabelsAndTypes(ProductT, childTable) }
            }

            val response = columns.map{(name, type) -> mapOf("name" to name, "type" to type)}

            call.respond(response)
            return@get
        }

        get("/adminByEmail/{email}") {
            val email = call.parameters["email"]

            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid email!")
                return@get
            }

            val admin = AccountRepository.getAdminByEmail(email)

            if (admin == null) {
                call.respond(HttpStatusCode.NotFound, "This admin doesn't exist!")
                return@get
            }

            val res = suspendTransaction { daoToAdmin(admin) }
            call.respond(res)
            return@get
        }

        get("/adminById/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if(id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
                return@get
            }

            val admin = AccountRepository.getAdminById(id)

            if (admin == null) {
                call.respond(HttpStatusCode.NotFound, "This admin doesn't exist!")
                return@get
            }

            val res = suspendTransaction { daoToAdmin(admin) }
            call.respond(res)
            return@get
        }


        get("/cashierByEmail/{email}") {
            val email = call.parameters["email"]

            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid email!")
                return@get
            }

            val cashier = AccountRepository.getCashierByEmail(email)

            if (cashier == null) {
                call.respond(HttpStatusCode.NotFound, "This cashier doesn't exist!")
                return@get
            }

            val res = suspendTransaction { daoToCashier(cashier) }
            call.respond(res)
            return@get
        }

        get("/cashierById/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if(id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
                return@get
            }

            val cashier = AccountRepository.getCashierById(id)

            if (cashier == null) {
                call.respond(HttpStatusCode.NotFound, "This cashier doesn't exist!")
                return@get
            }

            val res = suspendTransaction { daoToCashier(cashier) }
            call.respond(res)
            return@get
        }


        get("/carrocaBoyByEmail/{email}") {
            val email = call.parameters["email"]

            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid email!")
                return@get
            }

            val carrocaBoy = AccountRepository.getCarrocaBoyByEmail(email)

            if (carrocaBoy == null) {
                call.respond(HttpStatusCode.NotFound, "This CarroçaBoy doesn't exist!")
                return@get
            }

            val res = suspendTransaction { daoToCarrocaBoy(carrocaBoy) }
            call.respond(res)
            return@get
        }

        get("/carrocaBoyById/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if(id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
                return@get
            }

            val carrocaBoy = AccountRepository.getCarrocaBoyById(id)

            if (carrocaBoy == null) {
                call.respond(HttpStatusCode.NotFound, "This CarroçaBoy doesn't exist!")
                return@get
            }

            val res = suspendTransaction { daoToCarrocaBoy(carrocaBoy) }
            call.respond(res)
            return@get
        }

        get("/clientByEmail/{email}") {
            val email = call.parameters["email"]

            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid email!")
                return@get
            }

            val client = AccountRepository.getClientByEmail(email)

            if (client == null) {
                call.respond(HttpStatusCode.NotFound, "This client doesn't exist!")
                return@get
            }

            val res = suspendTransaction { daoToClient(client) }
            call.respond(res)
            return@get
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

            val res = suspendTransaction { daoToClient(client) }
            call.respond(res)
            return@get
        }

        post("/newAdmins") {
            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = AccountRepository.reqFields["Admins"] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val account = AccountRepository.newAccount(
                fields["username"]!!,
                fields["email"]!!,
                fields["password"]!!,
                "admin",
                date
            )

            AccountRepository.newAdmin(account, fields["root"]!!.toBoolean())

            call.respond(HttpStatusCode.OK, "Admin successfully added!")
            return@post
        }

        post("/editAdmins/{adminId}/{adminEditingEmail}") {
            val adminId = call.parameters["adminId"]?.toIntOrNull()
            val adminEditingEmail = call.parameters["adminEditingEmail"]
            if(adminId == null || adminEditingEmail.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountById(adminId)
            val admin = AccountRepository.getAdminById(adminId)

            if(account == null || admin == null) {
                call.respond(HttpStatusCode.NotFound, "This admin doesn't exist!")
                return@post
            }

            val adminEditing = AccountRepository.getAdminByEmail(adminEditingEmail)
            if(adminEditing == null) {
                call.respond(HttpStatusCode.NotFound, "The admin editing doesn't exist!")
                return@post
            }

            if(!adminEditing.root) {
                call.respond(HttpStatusCode.Unauthorized, "The admin can only edit others if it's a root!")
                return@post
            }

            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = AccountRepository.reqEditFields["Admins"] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            AccountRepository.editAccount(
                account,
                fields["username"]!!,
                fields["email"]!!,
                date
            )
            AccountRepository.editAdmin(admin, fields["root"]!!.toBoolean())

            call.respond(HttpStatusCode.OK, "Admin successfully edited!")
            return@post
        }

        post("/newCarrocaboys") {
            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = AccountRepository.reqFields["Carrocaboys"] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val account = AccountRepository.newAccount(
                fields["username"]!!,
                fields["email"]!!,
                fields["password"]!!,
                "carrocaboy",
                date
            )

            AccountRepository.newCarrocaBoy(account)

            call.respond(HttpStatusCode.OK, "CarroçaBoy successfully added!")
            return@post
        }

        post("/editCarrocaboys/{carrocaBoyId}") {
            val carrocaBoyId = call.parameters["carrocaBoyId"]?.toIntOrNull()
            if(carrocaBoyId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountById(carrocaBoyId)

            if(account == null || account.type != "carrocaboy") {
                call.respond(HttpStatusCode.NotFound, "This CarroçaBoyId doesn't exist!")
                return@post
            }

            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = AccountRepository.reqFields["Employee"] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            AccountRepository.editAccount(
                account,
                fields["username"]!!,
                fields["email"]!!,
                date
            )

            call.respond(HttpStatusCode.OK, "CarroçaBoy successfully edited!")
            return@post
        }

        post("/newCashiers") {
            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = AccountRepository.reqFields["Cashiers"] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            val account = AccountRepository.newAccount(
                fields["username"]!!,
                fields["email"]!!,
                fields["password"]!!,
                "cashier",
                date
            )

            AccountRepository.newCashier(account, fields["section"]!!.toLong())

            call.respond(HttpStatusCode.OK, "Cashier successfully added!")
            return@post
        }

        post("/editCashiers/{cashierId}") {
            val cashierId = call.parameters["cashierId"]?.toIntOrNull()
            if(cashierId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountById(cashierId)
            val cashier = AccountRepository.getCashierById(cashierId)

            if(account == null || cashier == null) {
                call.respond(HttpStatusCode.NotFound, "This cashier doesn't exist!")
                return@post
            }

            val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
            val required = AccountRepository.reqFields["Employee"] ?: emptyList()
            val missing = required.filter { it !in fields }
            if(missing.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            AccountRepository.editAccount(
                account,
                fields["username"]!!,
                fields["email"]!!,
                date
            )

            call.respond(HttpStatusCode.OK, "Cashier successfully edited!")
            return@post
        }

        delete("/deleteAccount/{toDeleteId}/{deletingEmail}") {
            val toDeleteId = call.parameters["toDeleteId"]?.toIntOrNull()
            val deletingEmail = call.parameters["deletingEmail"]

            if(toDeleteId == null || deletingEmail.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@delete
            }

            val toDelete = AccountRepository.getAccountById(toDeleteId)
            val deleting = AccountRepository.getAccountByEmail(deletingEmail)

            if(toDelete == null || deleting == null) {
                call.respond(HttpStatusCode.NotFound, "Invalid parameters!")
                return@delete
            }

            if(toDelete.email == deletingEmail) {
                call.respond(HttpStatusCode.Unauthorized, "You can't delete yourself!")
                return@delete
            }

            if(toDelete.type == "admin") {
                val admin1 = AccountRepository.getAdminById(toDeleteId)
                val admin2 = AccountRepository.getAdminByEmail(deletingEmail)
                if(admin1!!.root || !admin2!!.root) {
                    call.respond(HttpStatusCode.Unauthorized, "You can't delete a root admin or an admin of same level!")
                    return@delete
                }
            }

            AccountRepository.deleteAccount(toDelete)
            call.respond(HttpStatusCode.OK, "Account successfully deleted!")
            return@delete
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

        post("/editClient/{email}") {
            val email = call.parameters["email"]
            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val register = call.receive<EditAccount>()

            val account = AccountRepository.getAccountByEmail(email)
            val client = AccountRepository.getClientByEmail(email)
            if(account == null || client == null) {
                call.respond(HttpStatusCode.Unauthorized, "This user doesn't exist!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            AccountRepository.editAccount(account, register.username, register.email, date)
            AccountRepository.editClient(client, register.address)

            call.respond(HttpStatusCode.OK, "Account successfully edited!")
            return@post
        }

        post("/editAddress/{email}/{newAddress}") {
            val email = call.parameters["email"]
            val newAddress = call.parameters["newAddress"]
            if(email.isNullOrBlank() || newAddress.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            val client = AccountRepository.getClientByEmail(email)
            if(account == null || client == null) {
                call.respond(HttpStatusCode.Unauthorized, "This user doesn't exist!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            AccountRepository.editAccount(account, account.username, account.email, date)
            AccountRepository.editClient(client, newAddress)

            call.respond(HttpStatusCode.OK, "Address successfully edited!")
            return@post
        }

        post("/login") {
            val login = call.receive<Login>()
            val account = AccountRepository.getAccountByEmail(login.email)
            if(account == null || !AccountRepository.checkPw(login.password, account.password)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid user or invalid password!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            AccountRepository.setLastRun(account, date)

            val token = generateToken(account.email, account.type)
            call.respond(mapOf("token" to token))
            return@post
        }

        get("/getCart/{email}") {
            val email = call.parameters["email"]
            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@get
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null || account.type != "client") {
                call.respond(HttpStatusCode.NotFound, "This client does not exist!")
                return@get
            }

            val cart = SaleRepository.getCartByAccount(account)
            val saleProducts = SaleProductRepository.getSaleProductsBySale(cart.id.value)
            val sale = suspendTransaction { daoToSale(cart) }

            if(saleProducts.isEmpty()) {
                call.respond(SaleInfo(sale, emptyList()))
                return@get
            }

            val productInfos = saleProducts.map { saleProduct ->
                val productDAO = ProductRepository.getProductById(saleProduct.idProduct)
                suspendTransaction {
                    productDAO?.let {
                        ProductCartInfo(
                            it.id.value.toLong(),
                            it.productName,
                            it.image,
                            it.priceGold * saleProduct.quantity,
                            saleProduct.quantity,
                            it.type
                        )
                    }
                }
            }

            call.respond(SaleInfo(sale, productInfos))
        }

        get("/getSale/{saleId}") {
            val saleId = call.parameters["saleId"]?.toIntOrNull()
            if(saleId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@get
            }

            val cart = SaleRepository.getSaleById(saleId)

            if(cart == null) {
                call.respond(HttpStatusCode.NotFound, "This sale does not exist!")
                return@get
            }

            val saleProducts = SaleProductRepository.getSaleProductsBySale(cart.id.value)
            val sale = suspendTransaction { daoToSale(cart) }

            if(saleProducts.isEmpty()) {
                call.respond(SaleInfo(sale, emptyList()))
                return@get
            }

            val productInfos = saleProducts.map { saleProduct ->
                val productDAO = ProductRepository.getProductById(saleProduct.idProduct)
                suspendTransaction {
                    productDAO?.let {
                        ProductCartInfo(
                            it.id.value.toLong(),
                            it.productName,
                            it.image,
                            it.priceGold * saleProduct.quantity,
                            saleProduct.quantity,
                            it.type
                        )
                    }
                }
            }

            call.respond(SaleInfo(sale, productInfos))
        }

        get("/previousOrders/{email}") {
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

            call.respond(SaleRepository.getFinishedSalesByClient(account.id.value))
        }

        get("/previousSales/{email}") {
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

            call.respond(SaleRepository.getFinishedSalesByEmployee(account.id.value))
        }

        post("/newIrlPurchase/{email}") {
            val email = call.parameters["email"]
            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null || account.type != "cashier") {
                call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            SaleRepository.newIrlPurchase(account, date)

            call.respond(HttpStatusCode.OK, "New IRL purchase created!")
            return@post
        }

        delete("/cancelIrlPurchase/{email}") {
            val email = call.parameters["email"]
            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@delete
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null || account.type != "cashier") {
                call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
                return@delete
            }

            val purchase = SaleRepository.getIrlPurchaseByAccount(account)
            if(purchase == null) {
                call.respond(HttpStatusCode.NotFound, "This IRL purchase does not exist!")
                return@delete
            }

            SaleRepository.deleteIrlPurchase(purchase)
            call.respond(HttpStatusCode.OK, "Purchase cancelled successfully!")
            return@delete
        }

        post("/addToIrlPurchase/{idProduct}/{email}") {
            val idProduct = call.parameters["idProduct"]?.toIntOrNull()
            val email = call.parameters["email"]

            if(idProduct == null || email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            val product = ProductRepository.getProductById(idProduct)

            if(account == null || account.type != "cashier") {
                call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
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

            val purchase = SaleRepository.getIrlPurchaseByAccount(account)

            if(purchase == null) {
                call.respond(HttpStatusCode.NotFound, "This IRL purchase does not exist!")
                return@post
            }

            val check = SaleProductRepository.getSaleProduct(purchase.id.value, product.id.value)
            if(check != null) {
                call.respond(HttpStatusCode.Unauthorized, "This product is already in this purchase!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()
            SaleProductRepository.newSaleProduct(purchase, product)
            SaleRepository.alterTotalQuantity(purchase, 1, date)
            SaleRepository.alterTotalPrice(purchase, product, 0, 1, date)

            call.respond(HttpStatusCode.OK, "Product successfully added!")
            return@post
        }

        delete("/deleteFromIrlPurchase/{idProduct}/{email}") {
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
            val saleProduct = SaleProductRepository.getSaleProduct(cart.id.value, idProduct)

            if(saleProduct == null) {
                call.respond(HttpStatusCode.NotFound, "This product is not in the cart!")
                return@delete
            }

            val date = Date(System.currentTimeMillis()).toString()
            val delta = saleProduct.quantity
            SaleRepository.alterTotalPrice(cart, product, saleProduct.quantity, 0, date)
            SaleRepository.alterTotalQuantity(cart, -delta, date)
            SaleProductRepository.deleteSaleProduct(saleProduct)
        }

        post("/alterQuantityIrlPurchase/{idProduct}/{email}/{quantity}") {
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

            val sale = SaleRepository.getIrlPurchaseByAccount(account)
            if(sale == null) {
                call.respond(HttpStatusCode.NotFound, "This IRL sale does not exist!")
                return@post
            }

            val saleProduct = SaleProductRepository.getSaleProduct(sale.id.value, idProduct)

            if(saleProduct == null) {
                call.respond(HttpStatusCode.NotFound, "This product to sale assignment does not exist!")
                return@post
            }

            val previousQuantity = saleProduct.quantity

            val date = Date(System.currentTimeMillis()).toString()
            SaleProductRepository.alterQuantity(saleProduct, quantity)
            SaleRepository.alterTotalQuantity(sale, quantity-previousQuantity, date)
            SaleRepository.alterTotalPrice(sale, product, previousQuantity, quantity, date)

            call.respond(HttpStatusCode.OK, "Quantity successfully altered!")
            return@post
        }

        post("/addClientToIrlPurhcase/{emailCashier}/{emailClient}") {
            val emailCashier = call.parameters["emailCashier"]
            val emailClient = call.parameters["emailClient"]
            if(emailCashier.isNullOrBlank() || emailClient.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Parameters!")
                return@post
            }

            val client = AccountRepository.getAccountByEmail(emailClient)
            if(client == null || client.type != "client") {
                call.respond(HttpStatusCode.BadRequest, "Invalid user!")
                return@post
            }

            val cashier = AccountRepository.getAccountByEmail(emailCashier)
            if(cashier == null || cashier.type != "cashier") {
                call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
                return@post
            }

            val sale = SaleRepository.getIrlPurchaseByAccount(cashier)
            if(sale == null) {
                call.respond(HttpStatusCode.NotFound, "This IRL sale does not exist!")
                return@post
            }

            SaleRepository.assignClient(client, sale)
            call.respond(HttpStatusCode.OK, "Client successfully added!")
            return@post
        }

        post("/finishIrlSale/{email}") {
            val email = call.parameters["email"]
            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Parameters!")
                return@post
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null || account.type != "cashier") {
                call.respond(HttpStatusCode.BadRequest, "Invalid user!")
                return@post
            }

            val purchase = SaleRepository.getIrlPurchaseByAccount(account)
            if(purchase == null) {
                call.respond(HttpStatusCode.NotFound, "This purchase doesn't exist!")
                return@post
            }

            val saleProducts = SaleProductRepository.getSaleProductsBySale(purchase.id.value)

            for (product in saleProducts) {
                val check = ProductRepository.getProductById(product.idProduct)
                if(check == null || product.quantity > check.stock) {
                    call.respond(HttpStatusCode.Unauthorized, "Couldn't finish this purchase!")
                    return@post
                }
            }

            val date = Date(System.currentTimeMillis()).toString()
            SaleRepository.finishSale(purchase, "Delivered!", date)

            for (product in saleProducts) {
                val productDAO = ProductRepository.getProductById(product.idProduct)
                ProductRepository.alterStock(productDAO!!, product.quantity)
            }
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

            val check = SaleProductRepository.getSaleProduct(cart.id.value, product.id.value)
            if(check != null) {
                call.respond(HttpStatusCode.Unauthorized, "This product is already in this cart!")
                return@post
            }

            val date = Date(System.currentTimeMillis()).toString()

            SaleProductRepository.newSaleProduct(cart, product)
            SaleRepository.alterTotalQuantity(cart, 1, date)

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
            val saleProduct = SaleProductRepository.getSaleProduct(cart.id.value, idProduct)

            if(saleProduct == null) {
                call.respond(HttpStatusCode.NotFound, "This product is not in the cart!")
                return@delete
            }

            val date = Date(System.currentTimeMillis()).toString()
            val delta = saleProduct.quantity
            SaleRepository.alterTotalPrice(cart, product, saleProduct.quantity, 0, date)
            SaleProductRepository.deleteSaleProduct(saleProduct)
            SaleRepository.alterTotalQuantity(cart, -delta, date)
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
            SaleRepository.alterTotalQuantity(sale, quantity-previousQuantity, date)
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
            SaleRepository.finishSale(cart, "Waiting for CarroçaBoy!", date)
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
            SaleRepository.finishSale(sale, "Delivered", date)
            val carrocaBoy = AccountRepository.getCarrocaBoyByEmail(email)
            if(carrocaBoy == null) {
                call.respond(HttpStatusCode.NotFound, "This CarroçaBoy does not exist!")
                return@post
            }

            AccountRepository.addCommissionToEmployee(carrocaBoy, sale.totalPriceGold, date)

            call.respond(HttpStatusCode.OK, "You have delivered this order!")
            return@post
        }

        get("/availableSales") {
            call.respond(SaleRepository.getAvailableSales())
            return@get
        }

        get("/salesToBeDelivered/{email}") {
            val email = call.parameters["email"]

            if(email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
                return@get
            }

            val account = AccountRepository.getAccountByEmail(email)
            if(account == null) {
                call.respond(HttpStatusCode.NotFound, "This CarroçaBoy does not exist!")
                return@get
            }
            call.respond(SaleRepository.getSalesToBeDelivered(account))
            return@get
        }
    }
}
