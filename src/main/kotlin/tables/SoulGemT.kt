package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object SoulGemT : IntIdTable("SoulGems") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val soulSize = varchar("soulSize", 255)
    val isFilled = varchar("isFilled", 255)
    val containedSoul = varchar("containedSoul", 255)
    val canCapture = varchar("canCapture", 255)
    val reusable = varchar("reusable", 255)
}

class SoulGemDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SoulGemDAO>(SoulGemT)

    var product by ProductDAO referencedOn SoulGemT.product
    var soulSize by SoulGemT.soulSize
    var isFilled by SoulGemT.isFilled
    var containedSoul by SoulGemT.containedSoul
    var canCapture by SoulGemT.canCapture
    var reusable by SoulGemT.reusable
}

fun daoToSoulGem(dao: SoulGemDAO): SoulGem = SoulGem(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    soulSize = dao.soulSize,
    isFilled = dao.isFilled,
    containedSoul = dao.containedSoul,
    canCapture = dao.canCapture,
    reusable = dao.reusable
)
