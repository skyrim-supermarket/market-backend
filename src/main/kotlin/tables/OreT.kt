package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object OreT : IntIdTable("Ores") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
}

class OreDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OreDAO>(OreT)

    var product by ProductDAO referencedOn OreT.product
}

fun daoToOre(dao: OreDAO): Ore = Ore(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image,
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    standardDiscount = dao.product.standardDiscount,
    specialDiscount = dao.product.specialDiscount,
    hasDiscount = dao.product.hasDiscount
)
