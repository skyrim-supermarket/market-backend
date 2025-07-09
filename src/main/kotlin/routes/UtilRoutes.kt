package com.mac350.routes

import com.mac350.plugins.suspendTransaction
import com.mac350.repositories.UtilRepository
import com.mac350.tables.AccountT
import com.mac350.tables.ProductT
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.utilRoutes() {
    staticFiles("/uploads", File("uploads"))

    get("/labels/{table}") {
        var table = call.parameters["table"]

        if(table == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid table name!")
            return@get
        }

        table = table.lowercase()
        val childTable = UtilRepository.getTableName(table)

        if(childTable == null) {
            call.respond(HttpStatusCode.NotFound, "$table does not exist!")
            return@get
        }

        val columns = if(table=="admins" || table == "carrocaboys" || table == "cashiers") {
            suspendTransaction { UtilRepository.getLabelsAndTypes(AccountT, childTable) }
        } else {
            suspendTransaction { UtilRepository.getLabelsAndTypes(ProductT, childTable) }
        }

        val response = columns.map{(name, type) -> mapOf("name" to name, "type" to type)}

        call.respond(response)
        return@get
    }
}