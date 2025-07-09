package com.mac350.routes

import com.mac350.models.Filter
import com.mac350.models.QueryResults
import com.mac350.plugins.suspendTransaction
import com.mac350.repositories.ProductRepository
import com.mac350.repositories.UtilRepository
import com.mac350.tables.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*

fun Route.productRoutes() {

    post("/products") {
        val recv = call.receive<Filter>()
        var type = recv.type.lowercase()
        type = UtilRepository.capitalizeFirstLetter(type)
        val page = recv.page - 1
        val pageSize = recv.pageSize
        val productName = recv.filterName
        val minPriceGold = recv.minPriceGold
        val maxPriceGold = recv.maxPriceGold
        val orderBy = recv.orderBy

        val query = ProductRepository.getProducts(type, productName, minPriceGold, maxPriceGold, orderBy)

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
            "Ammunition" -> ProductRepository.newAmmunition(product, fields["magical"]!!, fields["craft"]!!, fields["speed"]!!.toDouble(), fields["gravity"]!!.toDouble(), fields["category"]!!)
            "Armor" -> ProductRepository.newArmor(product, fields["weight"]!!.toDouble(), fields["magical"]!!, fields["craft"]!!, fields["protection"]!!.toDouble(), fields["heavy"]!!, fields["category"]!!)
            "Books" -> ProductRepository.newBook(product, fields["skillTaught"]!!,  fields["magical"]!!,  fields["pages"]!!.toLong())
            "Clothing" -> ProductRepository.newClothing(product, fields["protection"]!!.toLong(),  fields["slot"]!!,  fields["enchantment"]!!,  fields["enchanted"]!!,  fields["weight"]!!.toDouble())
            "Food" -> ProductRepository.newFood(product, fields["weight"]!!.toDouble(),  fields["healthRestored"]!!.toLong(),  fields["staminaRestored"]!!.toLong(),  fields["magickaRestored"]!!.toLong(),  fields["duration"]!!.toLong())
            "Ingredients" -> ProductRepository.newIngredient(product, fields["weight"]!!.toDouble(),  fields["magical"]!!,  fields["effects"]!!)
            "Miscellaneous" -> ProductRepository.newMiscellany(product, fields["questItem"]!!,  fields["craftingUse"]!!,  fields["modelType"]!!)
            "Ores" -> ProductRepository.newOre(product, fields["weight"]!!.toDouble(),  fields["metalType"]!!,  fields["smeltedInto"]!!)
            "Potions" -> ProductRepository.newPotion(product, fields["effects"]!!,  fields["duration"]!!.toLong(),  fields["magnitude"]!!,  fields["poisoned"]!!)
            "Soul gems" -> ProductRepository.newSoulGem(product, fields["soulSize"]!!,  fields["isFilled"]!!,  fields["containedSoul"]!!,  fields["canCapture"]!!,  fields["reusable"]!!)
            "Weapons" -> ProductRepository.newWeapon(product, fields["weight"]!!.toDouble(), fields["magical"]!!, fields["craft"]!!, fields["damage"]!!.toLong(), fields["speed"]!!.toDouble(), fields["reach"]!!.toLong(), fields["stagger"]!!.toDouble(), fields["battleStyle"]!!, fields["category"]!!)
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
            is AmmunitionDAO -> ProductRepository.editAmmunition(typeDao, fields["magical"]!!, fields["craft"]!!, fields["speed"]!!.toDouble(), fields["gravity"]!!.toDouble(), fields["category"]!!)
            is ArmorDAO -> ProductRepository.editArmor(typeDao, fields["weight"]!!.toDouble(), fields["magical"]!!, fields["craft"]!!, fields["protection"]!!.toDouble(), fields["heavy"]!!, fields["category"]!!)
            is BookDAO -> ProductRepository.editBook(typeDao, fields["skillTaught"]!!,  fields["magical"]!!,  fields["pages"]!!.toLong())
            is ClothingDAO -> ProductRepository.editClothing(typeDao, fields["protection"]!!.toLong(),  fields["slot"]!!,  fields["enchantment"]!!,  fields["enchanted"]!!,  fields["weight"]!!.toDouble())
            is FoodDAO -> ProductRepository.editFood(typeDao, fields["weight"]!!.toDouble(),  fields["healthRestored"]!!.toLong(),  fields["staminaRestored"]!!.toLong(),  fields["magickaRestored"]!!.toLong(),  fields["duration"]!!.toLong())
            is IngredientDAO -> ProductRepository.editIngredient(typeDao, fields["weight"]!!.toDouble(),  fields["magical"]!!,  fields["effects"]!!)
            is MiscellanyDAO -> ProductRepository.editMiscellany(typeDao, fields["questItem"]!!,  fields["craftingUse"]!!,  fields["modelType"]!!)
            is OreDAO -> ProductRepository.editOre(typeDao, fields["weight"]!!.toDouble(),  fields["metalType"]!!,  fields["smeltedInto"]!!)
            is PotionDAO -> ProductRepository.editPotion(typeDao, fields["effects"]!!,  fields["duration"]!!.toLong(),  fields["magnitude"]!!,  fields["poisoned"]!!)
            is SoulGemDAO -> ProductRepository.editSoulGem(typeDao, fields["soulSize"]!!,  fields["isFilled"]!!,  fields["containedSoul"]!!,  fields["canCapture"]!!,  fields["reusable"]!!)
            is WeaponDAO -> ProductRepository.editWeapon(typeDao, fields["weight"]!!.toDouble(), fields["magical"]!!, fields["craft"]!!, fields["damage"]!!.toLong(), fields["speed"]!!.toDouble(), fields["reach"]!!.toLong(), fields["stagger"]!!.toDouble(), fields["battleStyle"]!!, fields["category"]!!)
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

}