package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ArmorT : IntIdTable("Armors") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val weight = double("weight")
    val magical = bool("magical")
    val craft = varchar("craft", 255)
    val protection = double("protection")
    val heavy = bool("heavy")
    val category = varchar("category", 255)
}

class ArmorDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ArmorDAO>(ArmorT)

    var product by ProductDAO referencedOn ArmorT.product
    var weight by ArmorT.weight
    var magical by ArmorT.magical
    var craft by ArmorT.craft
    var protection by ArmorT.protection
    var heavy by ArmorT.heavy
    var category by ArmorT.category
}

fun daoToArmor(dao: ArmorDAO): Armor = Armor(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    standardDiscount = dao.product.standardDiscount,
    specialDiscount = dao.product.specialDiscount,
    hasDiscount = dao.product.hasDiscount,
    weight = dao.weight,
    magical = dao.magical,
    craft = dao.craft,
    protection = dao.protection,
    heavy = dao.heavy,
    category = dao.category
)
