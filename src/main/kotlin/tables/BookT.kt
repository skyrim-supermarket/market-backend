package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BookT : IntIdTable("Books") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val skillTaught = varchar("skillTaught", 255)
    val magical = varchar("magical", 255)
    val pages = long("pages")
}

class BookDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BookDAO>(BookT)

    var product by ProductDAO referencedOn BookT.product
    var skillTaught by BookT.skillTaught
    var magical by BookT.magical
    var pages by BookT.pages
}

fun daoToBook(dao: BookDAO): Book = Book(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    skillTaught = dao.skillTaught,
    magical = dao.magical,
    pages = dao.pages
)
