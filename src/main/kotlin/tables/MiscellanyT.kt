package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object MiscellanyT : IntIdTable("Miscellaneous") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val questItem = varchar("questItem", 255)
    val craftingUse = varchar("craftingUse", 255)
    val modelType = varchar("modelType", 255)
}

class MiscellanyDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MiscellanyDAO>(MiscellanyT)

    var product by ProductDAO referencedOn MiscellanyT.product
    var questItem by MiscellanyT.questItem
    var craftingUse by MiscellanyT.craftingUse
    var modelType by MiscellanyT.modelType
}

fun daoToMiscellany(dao: MiscellanyDAO): Miscellany = Miscellany(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    questItem = dao.questItem,
    craftingUse = dao.craftingUse,
    modelType = dao.modelType
)
