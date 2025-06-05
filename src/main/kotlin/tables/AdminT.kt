package com.mac350.tables

import com.mac350.models.Admin
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object AdminT : IntIdTable("Admins") {
    val account = reference("id", AccountT, onDelete = ReferenceOption.CASCADE)
}

class AdminDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AdminDAO>(AdminT)

    var account by AccountDAO referencedOn AdminT.account
}

fun daoToAdmin(dao: AdminDAO): Admin = Admin(
    id = dao.account.id.value.toLong(),
    username = dao.account.username,
    email = dao.account.email,
    password = dao.account.password,
    type = dao.account.type,
    createdAt = dao.account.createdAt,
    updatedAt = dao.account.updatedAt
)
