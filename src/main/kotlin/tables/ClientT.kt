package com.mac350.tables

import com.mac350.models.Client
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ClientT : IntIdTable("Clients") {
    val account = reference("account_id", AccountT, onDelete = ReferenceOption.CASCADE)
    val isSpecialClient = bool("isSpecialClient")
    val address = varchar("address", 255).nullable()

}

class ClientDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ClientDAO>(ClientT)

    var account by AccountDAO referencedOn ClientT.account
    var isSpecialClient by ClientT.isSpecialClient
    var address by ClientT.address
}

fun daoToClient(dao: ClientDAO): Client = Client(
    id = dao.account.id.value.toLong(),
    username = dao.account.username,
    email = dao.account.email,
    password = dao.account.password,
    type = dao.account.type,
    createdAt = dao.account.createdAt,
    updatedAt = dao.account.updatedAt,
    lastRun = dao.account.lastRun,
    isSpecialClient = dao.isSpecialClient,
    address = dao.address
)
