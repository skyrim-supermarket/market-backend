package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object OreT : IntIdTable("Ores") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val weight = double("weight")
    val metalType = varchar("metalType", 255)
    val smeltedInto = varchar("smeltedInto", 255)
}

class OreDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OreDAO>(OreT)

    var product by ProductDAO referencedOn OreT.product
    var weight by OreT.weight
    var metalType by OreT.metalType
    var smeltedInto by OreT.smeltedInto
}

fun daoToOre(dao: OreDAO): Ore = Ore(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    weight = dao.weight,
    metalType = dao.metalType,
    smeltedInto = dao.smeltedInto
)
