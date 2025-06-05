package com.mac350.tables

import com.mac350.models.Cashier
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object CashierT : IntIdTable("Cashiers") {
    val account = reference("id", AccountT, onDelete = ReferenceOption.CASCADE)
    val totalComissions = long("totalComissions")
    val section = long("section")
}

class CashierDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CashierDAO>(CashierT)

    var account by AccountDAO referencedOn CashierT.account
    var totalComissions by CashierT.totalComissions
    var section by CashierT.section
}

fun daoToCashier(dao: CashierDAO): Cashier = Cashier(
    id = dao.account.id.value.toLong(),
    username = dao.account.username,
    email = dao.account.email,
    password = dao.account.password,
    type = dao.account.type,
    createdAt = dao.account.createdAt,
    updatedAt = dao.account.updatedAt,
    totalCommissions = dao.totalComissions,
    section = dao.section
)
