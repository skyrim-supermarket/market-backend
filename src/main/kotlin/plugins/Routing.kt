package com.mac350.plugins

import com.mac350.models.*
import com.mac350.tables.*
import com.mac350.repositories.*
import com.mac350.routes.accountRoutes
import com.mac350.routes.productRoutes
import com.mac350.routes.saleRoutes
import com.mac350.routes.utilRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import java.util.Date
import java.io.File
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun Application.configureRouting() {
    routing {
        accountRoutes()
        productRoutes()
        saleRoutes()
        utilRoutes()
    }
}
