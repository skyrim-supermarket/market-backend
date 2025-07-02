package com.mac350.tables

import com.mac350.models.Ammunition
import com.mac350.models.ProductCardInfo
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.dao.id.IntIdTable

object ProductT : IntIdTable("Products") {
    val productName = varchar("productName", 255)
    val image = varchar("image", 255).nullable()
    val priceGold = long("priceGold")
    val stock = long("stock")
    val description = varchar("description", 255)
    val type = varchar("type", 255)
    val createdAt = varchar("createdAt", 255)
    val updatedAt = varchar("updatedAt", 255)
}

class ProductDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductDAO>(ProductT)

    var productName by ProductT.productName
    var image by ProductT.image
    var priceGold by ProductT.priceGold
    var stock by ProductT.stock
    var description by ProductT.description
    var type by ProductT.type
    var createdAt by ProductT.createdAt
    var updatedAt by ProductT.updatedAt
}

fun daoToCard(dao: ProductDAO): ProductCardInfo = ProductCardInfo(
    id = dao.id.value.toLong(),
    productName = dao.productName,
    image = dao.image?.let { "http://localhost:8080$it" },
    priceGold = dao.priceGold,
    stock = dao.stock,
    type = dao.type
)

