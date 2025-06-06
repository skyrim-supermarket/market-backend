package com.mac350.tables

import com.mac350.models.Product
import com.mac350.models.SaleProduct
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object SaleProductT : IntIdTable("SaleProducts") {
    val idProduct = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val idSale = reference("sale_id", SaleT, onDelete = ReferenceOption.CASCADE)
    val quantity = long("quantity")
}

class SaleProductDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SaleProductDAO>(SaleProductT)

    var idProduct by ProductDAO referencedOn SaleProductT.idProduct
    var idSale by SaleDAO referencedOn SaleProductT.idSale
    var quantity by SaleProductT.quantity
}

fun daoToSaleProduct(dao: SaleProductDAO): SaleProduct = SaleProduct(
    id = dao.id.value.toLong(),
    idProduct = dao.idProduct.id.value.toLong(),
    idSale = dao.idSale.id.value.toLong(),
    quantity = dao.quantity
)