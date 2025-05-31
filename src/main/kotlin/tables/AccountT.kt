package com.mac350.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object AccountT : IntIdTable("Account") {
    val username = varchar("username", 255)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val type = varchar("type", 255)
    val createdAt = varchar("createdAt", 255)
    val updatedAt = varchar("updatedAt", 255)
}

class AccountDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AccountDAO>(AccountT)

    var username by AccountT.username
    var email by AccountT.email
    var password by AccountT.password
    var type by AccountT.type
    var createdAt by AccountT.createdAt
    var updatedAt by AccountT.updatedAt
}