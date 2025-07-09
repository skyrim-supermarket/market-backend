package com.mac350.routes

import com.mac350.models.EditAccount
import com.mac350.models.Login
import com.mac350.models.Register
import com.mac350.plugins.generateToken
import com.mac350.plugins.suspendTransaction
import com.mac350.repositories.AccountRepository
import com.mac350.repositories.SaleRepository
import com.mac350.repositories.UtilRepository
import com.mac350.tables.daoToAdmin
import com.mac350.tables.daoToCarrocaBoy
import com.mac350.tables.daoToCashier
import com.mac350.tables.daoToClient
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.accountRoutes() {
    get("/admins") {
        call.respond(AccountRepository.getAdmins())
        return@get
    }

    get("/carrocaboys") {
        call.respond(AccountRepository.getCarrocaBoys())
        return@get
    }

    get("/cashiers") {
        call.respond(AccountRepository.getCashiers())
        return@get
    }

    get("/clients") {
        call.respond(AccountRepository.getClients())
        return@get
    }

    get("/sales") {
        call.respond(SaleRepository.getSales())
        return@get
    }


    get("/adminByEmail/{email}") {
        val email = call.parameters["email"]

        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid email!")
            return@get
        }

        val admin = AccountRepository.getAdminByEmail(email)

        if (admin == null) {
            call.respond(HttpStatusCode.NotFound, "This admin doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToAdmin(admin) }
        call.respond(res)
        return@get
    }

    get("/adminById/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if(id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
            return@get
        }

        val admin = AccountRepository.getAdminById(id)

        if (admin == null) {
            call.respond(HttpStatusCode.NotFound, "This admin doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToAdmin(admin) }
        call.respond(res)
        return@get
    }


    get("/cashierByEmail/{email}") {
        val email = call.parameters["email"]

        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid email!")
            return@get
        }

        val cashier = AccountRepository.getCashierByEmail(email)

        if (cashier == null) {
            call.respond(HttpStatusCode.NotFound, "This cashier doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToCashier(cashier) }
        call.respond(res)
        return@get
    }

    get("/cashierById/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if(id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
            return@get
        }

        val cashier = AccountRepository.getCashierById(id)

        if (cashier == null) {
            call.respond(HttpStatusCode.NotFound, "This cashier doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToCashier(cashier) }
        call.respond(res)
        return@get
    }


    get("/carrocaBoyByEmail/{email}") {
        val email = call.parameters["email"]

        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid email!")
            return@get
        }

        val carrocaBoy = AccountRepository.getCarrocaBoyByEmail(email)

        if (carrocaBoy == null) {
            call.respond(HttpStatusCode.NotFound, "This CarroçaBoy doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToCarrocaBoy(carrocaBoy) }
        call.respond(res)
        return@get
    }

    get("/carrocaBoyById/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if(id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
            return@get
        }

        val carrocaBoy = AccountRepository.getCarrocaBoyById(id)

        if (carrocaBoy == null) {
            call.respond(HttpStatusCode.NotFound, "This CarroçaBoy doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToCarrocaBoy(carrocaBoy) }
        call.respond(res)
        return@get
    }

    get("/clientByEmail/{email}") {
        val email = call.parameters["email"]

        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid email!")
            return@get
        }

        val client = AccountRepository.getClientByEmail(email)

        if (client == null) {
            call.respond(HttpStatusCode.NotFound, "This client doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToClient(client) }
        call.respond(res)
        return@get
    }

    get("/clientById/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if(id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
            return@get
        }

        val client = AccountRepository.getClientById(id)

        if (client == null) {
            call.respond(HttpStatusCode.NotFound, "This client doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToClient(client) }
        call.respond(res)
        return@get
    }

    get("/employeeById/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if(id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID!")
            return@get
        }

        val account = AccountRepository.getAccountById(id)

        if (account == null || (account.type != "cashier" && account.type != "carrocaboy")) {
            call.respond(HttpStatusCode.NotFound, "This employee doesn't exist!")
            return@get
        }

        val res = if(account.type == "cashier") {
            val dao = AccountRepository.getCashierById(id)
            suspendTransaction { daoToCashier(dao!!) }
        } else {
            val dao = AccountRepository.getCarrocaBoyById(id)
            suspendTransaction { daoToCarrocaBoy(dao!!) }
        }

        call.respond(res)
        return@get
    }

    post("/newAdmins") {
        val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
        val required = AccountRepository.reqFields["Admins"] ?: emptyList()
        val missing = required.filter { it !in fields }
        if(missing.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        val account = AccountRepository.newAccount(
            fields["username"]!!,
            fields["email"]!!,
            fields["password"]!!,
            "admin",
            date
        )

        AccountRepository.newAdmin(account, fields["root"]!!)

        call.respond(HttpStatusCode.OK, "Admin successfully added!")
        return@post
    }

    post("/editAdmins/{adminId}/{adminEditingEmail}") {
        val adminId = call.parameters["adminId"]?.toIntOrNull()
        val adminEditingEmail = call.parameters["adminEditingEmail"]
        if(adminId == null || adminEditingEmail.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountById(adminId)
        val admin = AccountRepository.getAdminById(adminId)

        if(account == null || admin == null) {
            call.respond(HttpStatusCode.NotFound, "This admin doesn't exist!")
            return@post
        }

        val adminEditing = AccountRepository.getAdminByEmail(adminEditingEmail)
        if(adminEditing == null) {
            call.respond(HttpStatusCode.NotFound, "The admin editing doesn't exist!")
            return@post
        }

        if(adminEditing.root.lowercase() != "yes") {
            call.respond(HttpStatusCode.Unauthorized, "The admin can only edit others if it's a root!")
            return@post
        }

        val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
        val required = AccountRepository.reqEditFields["Admins"] ?: emptyList()
        val missing = required.filter { it !in fields }
        if(missing.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        AccountRepository.editAccount(
            account,
            fields["username"]!!,
            fields["email"]!!,
            date
        )
        AccountRepository.editAdmin(admin, fields["root"]!!)

        call.respond(HttpStatusCode.OK, "Admin successfully edited!")
        return@post
    }

    post("/newCarrocaboys") {
        val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
        val required = AccountRepository.reqFields["Carrocaboys"] ?: emptyList()
        val missing = required.filter { it !in fields }
        if(missing.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        val account = AccountRepository.newAccount(
            fields["username"]!!,
            fields["email"]!!,
            fields["password"]!!,
            "carrocaboy",
            date
        )

        AccountRepository.newCarrocaBoy(account)

        call.respond(HttpStatusCode.OK, "CarroçaBoy successfully added!")
        return@post
    }

    post("/editCarrocaboys/{carrocaBoyId}") {
        val carrocaBoyId = call.parameters["carrocaBoyId"]?.toIntOrNull()
        if(carrocaBoyId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountById(carrocaBoyId)

        if(account == null || account.type != "carrocaboy") {
            call.respond(HttpStatusCode.NotFound, "This CarroçaBoyId doesn't exist!")
            return@post
        }

        val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
        val required = AccountRepository.reqFields["Employee"] ?: emptyList()
        val missing = required.filter { it !in fields }
        if(missing.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        AccountRepository.editAccount(
            account,
            fields["username"]!!,
            fields["email"]!!,
            date
        )

        call.respond(HttpStatusCode.OK, "CarroçaBoy successfully edited!")
        return@post
    }

    post("/newCashiers") {
        val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
        val required = AccountRepository.reqFields["Cashiers"] ?: emptyList()
        val missing = required.filter { it !in fields }
        if(missing.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        val account = AccountRepository.newAccount(
            fields["username"]!!,
            fields["email"]!!,
            fields["password"]!!,
            "cashier",
            date
        )

        AccountRepository.newCashier(account, fields["section"]!!.toLong())

        call.respond(HttpStatusCode.OK, "Cashier successfully added!")
        return@post
    }

    post("/editCashiers/{cashierId}") {
        val cashierId = call.parameters["cashierId"]?.toIntOrNull()
        if(cashierId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountById(cashierId)
        val cashier = AccountRepository.getCashierById(cashierId)

        if(account == null || cashier == null) {
            call.respond(HttpStatusCode.NotFound, "This cashier doesn't exist!")
            return@post
        }

        val (fields, files) = UtilRepository.parseMultiPart(call.receiveMultipart())
        val required = AccountRepository.reqFields["Employee"] ?: emptyList()
        val missing = required.filter { it !in fields }
        if(missing.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing fields: $missing")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        AccountRepository.editAccount(
            account,
            fields["username"]!!,
            fields["email"]!!,
            date
        )

        call.respond(HttpStatusCode.OK, "Cashier successfully edited!")
        return@post
    }

    delete("/deleteAccount/{toDeleteId}/{deletingEmail}") {
        val toDeleteId = call.parameters["toDeleteId"]?.toIntOrNull()
        val deletingEmail = call.parameters["deletingEmail"]

        if(toDeleteId == null || deletingEmail.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@delete
        }

        val toDelete = AccountRepository.getAccountById(toDeleteId)
        val deleting = AccountRepository.getAccountByEmail(deletingEmail)

        if(toDelete == null || deleting == null) {
            call.respond(HttpStatusCode.NotFound, "Invalid parameters!")
            return@delete
        }

        if(toDelete.email == deletingEmail) {
            call.respond(HttpStatusCode.Unauthorized, "You can't delete yourself!")
            return@delete
        }

        if(toDelete.type == "admin") {
            val admin1 = AccountRepository.getAdminById(toDeleteId)
            val admin2 = AccountRepository.getAdminByEmail(deletingEmail)
            if(admin2!!.root.lowercase() != "yes" || admin1!!.root.lowercase() == "yes") {
                call.respond(HttpStatusCode.Unauthorized, "You can't delete a root admin or an admin of same level!")
                return@delete
            }
        }

        AccountRepository.deleteAccount(toDelete)
        call.respond(HttpStatusCode.OK, "Account successfully deleted!")
        return@delete
    }

    post("/registerClient") {
        val register = call.receive<Register>()

        val account = AccountRepository.getAccountByEmail(register.email)
        if(account != null) {
            call.respond(HttpStatusCode.Unauthorized, "This user already exists!")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        val newAccount = AccountRepository.newAccount(register.username, register.email, register.password, "client", date)
        AccountRepository.newClient(newAccount, register.address)
        SaleRepository.newCart(newAccount, date)

        val token = generateToken(register.email, "client")
        call.respond(mapOf("token" to token))
        return@post
    }

    post("/editClient/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val register = call.receive<EditAccount>()

        val account = AccountRepository.getAccountByEmail(email)
        val client = AccountRepository.getClientByEmail(email)
        if(account == null || client == null) {
            call.respond(HttpStatusCode.Unauthorized, "This user doesn't exist!")
            return@post
        }

        if(register.newPassword != null && register.newPassword != "") {
            if(!AccountRepository.checkPw(register.prevPassword, account.password)) {
                call.respond(HttpStatusCode.Unauthorized, "Wrong password!")
                return@post
            }

            AccountRepository.editPw(account, register.newPassword)
        }

        val date = Date(System.currentTimeMillis()).toString()
        AccountRepository.editAccount(account, register.username, register.email, date)
        AccountRepository.editClient(client, register.address)

        val token = generateToken(register.email, "client")
        call.respond(mapOf("token" to token))
        return@post
    }

    post("/login") {
        val login = call.receive<Login>()
        val account = AccountRepository.getAccountByEmail(login.email)
        if(account == null || !AccountRepository.checkPw(login.password, account.password)) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid user or invalid password!")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        AccountRepository.setLastRun(account, date)

        val token = generateToken(account.email, account.type)
        call.respond(mapOf("token" to token))
        return@post
    }

}