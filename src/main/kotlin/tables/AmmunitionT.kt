package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object AmmunitionT : IntIdTable("Ammunition") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val magical = varchar("magical", 255)
    val craft = varchar("craft", 255)
    val speed = double("speed")
    val gravity = double("gravity")
    val category = varchar("category", 255)
}

class AmmunitionDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AmmunitionDAO>(AmmunitionT)

    var product by ProductDAO referencedOn AmmunitionT.product
    var magical by AmmunitionT.magical
    var craft by AmmunitionT.craft
    var speed by AmmunitionT.speed
    var gravity by AmmunitionT.gravity
    var category by AmmunitionT.category
}

fun daoToAmmunition(dao: AmmunitionDAO): Ammunition = Ammunition(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    magical = dao.magical,
    craft = dao.craft,
    speed = dao.speed,
    gravity = dao.gravity,
    category = dao.category
)
