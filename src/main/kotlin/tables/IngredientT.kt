package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object IngredientT : IntIdTable("Ingredients") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val weight = double("weight")
    val magical = varchar("magical", 255)
    val effects = varchar("effects", 255)
}

class IngredientDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<IngredientDAO>(IngredientT)

    var product by ProductDAO referencedOn IngredientT.product
    var weight by IngredientT.weight
    var magical by IngredientT.magical
    var effects by IngredientT.effects
}

fun daoToIngredient(dao: IngredientDAO): Ingredient = Ingredient(
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
    magical = dao.magical,
    effects = dao.effects
)
