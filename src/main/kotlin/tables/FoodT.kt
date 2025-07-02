package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FoodT : IntIdTable("Foods") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
}

class FoodDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FoodDAO>(FoodT)

    var product by ProductDAO referencedOn FoodT.product
}

fun daoToFood(dao: FoodDAO): Food = Food(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt
)
