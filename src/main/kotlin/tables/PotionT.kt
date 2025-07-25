package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PotionT : IntIdTable("Potions") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val effects = varchar("effects", 255)
    val duration = long("duration")
    val magnitude = varchar("magnitude", 255)
    val poisoned = varchar("poisoned", 255)
}

class PotionDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PotionDAO>(PotionT)

    var product by ProductDAO referencedOn PotionT.product
    var effects by PotionT.effects
    var duration by PotionT.duration
    var magnitude by PotionT.magnitude
    var poisoned by PotionT.poisoned
}

fun daoToPotion(dao: PotionDAO): Potion = Potion(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    effects = dao.effects,
    duration = dao.duration,
    magnitude = dao.magnitude,
    poisoned = dao.poisoned
)
