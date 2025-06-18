package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PotionT : IntIdTable("Potions") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
}

class PotionDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PotionDAO>(PotionT)

    var product by ProductDAO referencedOn PotionT.product
}

fun daoToPotion(dao: PotionDAO): Potion = Potion(
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
