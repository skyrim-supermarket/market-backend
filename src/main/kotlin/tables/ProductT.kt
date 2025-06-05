package com.mac350.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object ProductT : IntIdTable("Products") {
    val productName = varchar("productName", 255)
    val image = varchar("image", 255)
    val priceGold = long("priceGold")
    val stock = long("stock")
    val createdAt = varchar("createdAt", 255)
    val updatedAt = varchar("updatedAt", 255)
    val standardDiscount = long("standardDiscount")
    val specialDiscount = long("specialDiscount")
    val hasDiscount = bool("hasDiscount")
}

class ProductDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductDAO>(ProductT)

    var productName by ProductT.productName
    var image by ProductT.image
    var priceGold by ProductT.priceGold
    var stock by ProductT.stock
    var createdAt by ProductT.createdAt
    var updatedAt by ProductT.updatedAt
    var standardDiscount by ProductT.standardDiscount
    var specialDiscount by ProductT.specialDiscount
    var hasDiscount by ProductT.hasDiscount
}