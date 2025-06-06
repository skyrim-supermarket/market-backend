package com.mac350.tables

import com.mac350.models.CarrocaBoy
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object CarrocaBoyT : IntIdTable("CarrocaBoys") {
    val account = reference("account_id", AccountT, onDelete = ReferenceOption.CASCADE)
    val totalComissions = long("totalComissions")
}

class CarrocaBoyDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CarrocaBoyDAO>(CarrocaBoyT)

    var account by AccountDAO referencedOn CarrocaBoyT.account
    var totalComissions by CarrocaBoyT.totalComissions
}

fun daoToCarrocaBoy(dao: CarrocaBoyDAO): CarrocaBoy = CarrocaBoy(
    id = dao.account.id.value.toLong(),
    username = dao.account.username,
    email = dao.account.email,
    password = dao.account.password,
    type = dao.account.type,
    createdAt = dao.account.createdAt,
    updatedAt = dao.account.updatedAt,
    lastRun = dao.account.lastRun,
    totalCommissions = dao.totalComissions
)
