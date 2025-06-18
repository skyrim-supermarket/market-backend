package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object MiscellanyT : IntIdTable("Miscellaneous") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
}

class MiscellanyDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MiscellanyDAO>(MiscellanyT)

    var product by ProductDAO referencedOn MiscellanyT.product
}

fun daoToMiscellany(dao: MiscellanyDAO): Miscellany = Miscellany(
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
