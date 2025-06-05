package com.mac350.tables

import com.mac350.models.Sale
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object SaleT : IntIdTable("Sales") {
    val idClient = reference("id", AccountT, onDelete = ReferenceOption.CASCADE).nullable()
    val idEmployee = reference("id", AccountT, onDelete = ReferenceOption.CASCADE).nullable()
    val totalPriceGold = long("totalPriceGold")
    val totalQuantity = long("totalQuantity")
    val finished = bool("finished")
    val status = varchar("status", 255)
    val createdAt = varchar("createdAt", 255)
    val updatedAt = varchar("updatedAt", 255)
}

class SaleDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SaleDAO>(SaleT)

    var idClient by AccountDAO.optionalReferencedOn(SaleT.idClient)
    var idEmployee by AccountDAO.optionalReferencedOn(SaleT.idEmployee)
    var totalPriceGold by SaleT.totalPriceGold
    var totalQuantity by SaleT.totalQuantity
    var finished by SaleT.finished
    var status by SaleT.status
    var createdAt by SaleT.createdAt
    var updatedAt by SaleT.updatedAt
}

fun daoToSale(dao: SaleDAO): Sale = Sale(
    id = dao.id.value.toLong(),
    idClient = dao.idClient?.id?.value?.toLong(),
    idEmployee = dao.idEmployee?.id?.value?.toLong(),
    totalPriceGold = dao.totalPriceGold,
    totalQuantity = dao.totalQuantity,
    finished = dao.finished,
    status = dao.status,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)