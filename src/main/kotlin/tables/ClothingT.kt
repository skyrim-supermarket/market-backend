package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ClothingT : IntIdTable("Clothes") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val protection = long("protection")
    val slot = varchar("slot", 255)
    val enchantment = varchar("enchantment", 255)
    val enchanted = varchar("enchanted", 255)
    val weight = double("weight")
}

class ClothingDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ClothingDAO>(ClothingT)

    var product by ProductDAO referencedOn ClothingT.product
    var protection by ClothingT.protection
    var slot by ClothingT.slot
    var enchantment by ClothingT.enchantment
    var enchanted by ClothingT.enchanted
    var weight by ClothingT.weight
}

fun daoToClothing(dao: ClothingDAO): Clothing = Clothing(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    protection = dao.protection,
    slot = dao.slot,
    enchantment = dao.enchantment,
    enchanted = dao.enchanted,
    weight = dao.weight
)
