package com.mac350.routes

import com.mac350.models.ProductCartInfo
import com.mac350.models.SaleInfo
import com.mac350.plugins.suspendTransaction
import com.mac350.repositories.AccountRepository
import com.mac350.repositories.ProductRepository
import com.mac350.repositories.SaleProductRepository
import com.mac350.repositories.SaleRepository
import com.mac350.tables.daoToSale
import com.mac350.tables.daoToSaleProduct
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.saleRoutes() {

    get("/getCart/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@get
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null || account.type != "client") {
            call.respond(HttpStatusCode.NotFound, "This client does not exist!")
            return@get
        }

        val cart = SaleRepository.getCartByAccount(account)
        val saleProducts = SaleProductRepository.getSaleProductsBySale(cart.id.value)
        val sale = suspendTransaction { daoToSale(cart) }

        if(saleProducts.isEmpty()) {
            call.respond(SaleInfo(sale, emptyList()))
            return@get
        }

        val productInfos = saleProducts.map { saleProduct ->
            val productDAO = ProductRepository.getProductById(saleProduct.idProduct)
            suspendTransaction {
                productDAO?.let {
                    ProductCartInfo(
                        it.id.value.toLong(),
                        it.productName,
                        it.image?.let { "http://localhost:8080$it" },
                        it.priceGold * saleProduct.quantity,
                        saleProduct.quantity,
                        it.type
                    )
                }
            }
        }

        call.respond(SaleInfo(sale, productInfos))
    }

    get("/getSale/{saleId}") {
        val saleId = call.parameters["saleId"]?.toIntOrNull()
        if(saleId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@get
        }

        val cart = SaleRepository.getSaleById(saleId)

        if(cart == null) {
            call.respond(HttpStatusCode.NotFound, "This sale does not exist!")
            return@get
        }

        val saleProducts = SaleProductRepository.getSaleProductsBySale(cart.id.value)
        val sale = suspendTransaction { daoToSale(cart) }

        if(saleProducts.isEmpty()) {
            call.respond(SaleInfo(sale, emptyList()))
            return@get
        }

        val productInfos = saleProducts.map { saleProduct ->
            val productDAO = ProductRepository.getProductById(saleProduct.idProduct)
            suspendTransaction {
                productDAO?.let {
                    ProductCartInfo(
                        it.id.value.toLong(),
                        it.productName,
                        it.image?.let { "http://localhost:8080${it}" },
                        it.priceGold * saleProduct.quantity,
                        saleProduct.quantity,
                        it.type
                    )
                }
            }
        }

        call.respond(SaleInfo(sale, productInfos))
    }

    get("/previousOrders/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@get
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@get
        }

        call.respond(SaleRepository.getFinishedSalesByClient(account.id.value))
    }

    get("/previousSales/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@get
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@get
        }

        call.respond(SaleRepository.getFinishedSalesByEmployee(account.id.value))
    }

    post("/newIrlPurchase/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null || account.type != "cashier") {
            call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        SaleRepository.newIrlPurchase(account, date)

        call.respond(HttpStatusCode.OK, "New IRL purchase created!")
        return@post
    }

    delete("/cancelIrlPurchase/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@delete
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null || account.type != "cashier") {
            call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
            return@delete
        }

        val purchase = SaleRepository.getIrlPurchaseByAccount(account)
        if(purchase == null) {
            call.respond(HttpStatusCode.NotFound, "This IRL purchase does not exist!")
            return@delete
        }

        SaleRepository.deleteIrlPurchase(purchase)
        call.respond(HttpStatusCode.OK, "Purchase cancelled successfully!")
        return@delete
    }

    post("/addToIrlPurchase/{idProduct}/{email}") {
        val idProduct = call.parameters["idProduct"]?.toIntOrNull()
        val email = call.parameters["email"]

        if(idProduct == null || email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        val product = ProductRepository.getProductById(idProduct)

        if(account == null || account.type != "cashier") {
            call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
            return@post
        }

        if(product == null) {
            call.respond(HttpStatusCode.NotFound, "This product does not exist!")
            return@post
        }

        if(product.stock <= 0) {
            call.respond(HttpStatusCode.Unauthorized, "This product is out of stock!")
            return@post
        }

        val purchase = SaleRepository.getIrlPurchaseByAccount(account)

        if(purchase == null) {
            call.respond(HttpStatusCode.NotFound, "This IRL purchase does not exist!")
            return@post
        }

        val check = SaleProductRepository.getSaleProduct(purchase.id.value, product.id.value)
        if(check != null) {
            call.respond(HttpStatusCode.Unauthorized, "This product is already in this purchase!")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        SaleProductRepository.newSaleProduct(purchase, product)
        SaleRepository.alterTotalQuantity(purchase, 1, date)
        SaleRepository.alterTotalPrice(purchase, product, 0, 1, date)

        call.respond(HttpStatusCode.OK, "Product successfully added!")
        return@post
    }

    delete("/deleteFromIrlPurchase/{idProduct}/{email}") {
        val idProduct = call.parameters["idProduct"]?.toIntOrNull()
        val email = call.parameters["email"]

        if(idProduct == null || email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@delete
        }

        val account = AccountRepository.getAccountByEmail(email)
        val product = ProductRepository.getProductById(idProduct)

        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@delete
        }

        if(product == null) {
            call.respond(HttpStatusCode.NotFound, "This product does not exist!")
            return@delete
        }

        val purchase = SaleRepository.getIrlPurchaseByAccount(account)

        if(purchase == null) {
            call.respond(HttpStatusCode.NotFound, "This IRL purchase doesn't exist!")
            return@delete
        }

        val saleProduct = SaleProductRepository.getSaleProduct(purchase.id.value, idProduct)

        if(saleProduct == null) {
            call.respond(HttpStatusCode.NotFound, "This product is not in the cart!")
            return@delete
        }

        val date = Date(System.currentTimeMillis()).toString()
        val delta = saleProduct.quantity
        SaleRepository.alterTotalPrice(purchase, product, saleProduct.quantity, 0, date)
        SaleRepository.alterTotalQuantity(purchase, -delta, date)
        SaleProductRepository.deleteSaleProduct(saleProduct)

        call.respond(HttpStatusCode.OK, "Product successfully deleted!")
        return@delete
    }

    post("/alterQuantityIrlPurchase/{idProduct}/{email}/{quantity}") {
        val idProduct = call.parameters["idProduct"]?.toIntOrNull()
        val email = call.parameters["email"]
        val quantity = call.parameters["quantity"]?.toLongOrNull()

        if(idProduct == null || email.isNullOrBlank() || quantity == null || quantity < 0) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val product = ProductRepository.getProductById(idProduct)
        if(product == null) {
            call.respond(HttpStatusCode.NotFound, "This product does not exist!")
            return@post
        }

        if(product.stock < quantity) {
            call.respond(HttpStatusCode.Unauthorized, "This quantity is over the product's stock!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@post
        }

        val sale = SaleRepository.getIrlPurchaseByAccount(account)
        if(sale == null) {
            call.respond(HttpStatusCode.NotFound, "This IRL sale does not exist!")
            return@post
        }

        val saleProduct = SaleProductRepository.getSaleProduct(sale.id.value, idProduct)

        if(saleProduct == null) {
            call.respond(HttpStatusCode.NotFound, "This product to sale assignment does not exist!")
            return@post
        }

        val previousQuantity = saleProduct.quantity

        val date = Date(System.currentTimeMillis()).toString()
        SaleProductRepository.alterQuantity(saleProduct, quantity)
        SaleRepository.alterTotalQuantity(sale, quantity-previousQuantity, date)
        SaleRepository.alterTotalPrice(sale, product, previousQuantity, quantity, date)

        call.respond(HttpStatusCode.OK, "Quantity successfully altered!")
        return@post
    }

    post("/addClientToIrlPurchase/{emailCashier}/{emailClient}") {
        val emailCashier = call.parameters["emailCashier"]
        val emailClient = call.parameters["emailClient"]
        if(emailCashier.isNullOrBlank() || emailClient.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid Parameters!")
            return@post
        }

        val client = AccountRepository.getAccountByEmail(emailClient)
        if(client == null || client.type != "client") {
            call.respond(HttpStatusCode.BadRequest, "Invalid user!")
            return@post
        }

        val cashier = AccountRepository.getAccountByEmail(emailCashier)
        if(cashier == null || cashier.type != "cashier") {
            call.respond(HttpStatusCode.NotFound, "This cashier does not exist!")
            return@post
        }

        val sale = SaleRepository.getIrlPurchaseByAccount(cashier)
        if(sale == null) {
            call.respond(HttpStatusCode.NotFound, "This IRL sale does not exist!")
            return@post
        }

        SaleRepository.assignClient(client, sale)
        call.respond(HttpStatusCode.OK, "Client successfully added!")
        return@post
    }

    post("/finishIrlSale/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid Parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null || account.type != "cashier") {
            call.respond(HttpStatusCode.BadRequest, "Invalid user!")
            return@post
        }

        val purchase = SaleRepository.getIrlPurchaseByAccount(account)
        if(purchase == null) {
            call.respond(HttpStatusCode.NotFound, "This purchase doesn't exist!")
            return@post
        }

        val saleProducts = SaleProductRepository.getSaleProductsBySale(purchase.id.value)

        for (product in saleProducts) {
            val check = ProductRepository.getProductById(product.idProduct)
            if(check == null || product.quantity > check.stock) {
                call.respond(HttpStatusCode.Unauthorized, "Couldn't finish this purchase!")
                return@post
            }
        }

        for (product in saleProducts) {
            val productDAO = ProductRepository.getProductById(product.idProduct)
            ProductRepository.alterStock(productDAO!!, product.quantity)
        }

        val date = Date(System.currentTimeMillis()).toString()
        SaleRepository.finishSale(purchase, "Delivered!", date)
        call.respond(HttpStatusCode.OK, "Sale finished successfully!")
        return@post
    }

    post("/addToCart/{idProduct}/{email}") {
        val idProduct = call.parameters["idProduct"]?.toIntOrNull()
        val email = call.parameters["email"]

        if(idProduct == null || email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        val product = ProductRepository.getProductById(idProduct)

        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@post
        }

        if(product == null) {
            call.respond(HttpStatusCode.NotFound, "This product does not exist!")
            return@post
        }

        if(product.stock <= 0) {
            call.respond(HttpStatusCode.Unauthorized, "This product is out of stock!")
            return@post
        }

        val cart = SaleRepository.getCartByAccount(account)

        val check = SaleProductRepository.getSaleProduct(cart.id.value, product.id.value)
        if(check != null) {
            call.respond(HttpStatusCode.Unauthorized, "This product is already in this cart!")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()

        SaleProductRepository.newSaleProduct(cart, product)
        SaleRepository.alterTotalQuantity(cart, 1, date)
        SaleRepository.alterTotalPrice(cart, product, 0, 1, date)

        call.respond(HttpStatusCode.OK, "Product successfully added!")
        return@post
    }

    delete("/deleteFromCart/{idProduct}/{email}") {
        val idProduct = call.parameters["idProduct"]?.toIntOrNull()
        val email = call.parameters["email"]

        if(idProduct == null || email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@delete
        }

        val account = AccountRepository.getAccountByEmail(email)
        val product = ProductRepository.getProductById(idProduct)

        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@delete
        }

        if(product == null) {
            call.respond(HttpStatusCode.NotFound, "This product does not exist!")
            return@delete
        }

        val cart = SaleRepository.getCartByAccount(account)
        val saleProduct = SaleProductRepository.getSaleProduct(cart.id.value, idProduct)

        if(saleProduct == null) {
            call.respond(HttpStatusCode.NotFound, "This product is not in the cart!")
            return@delete
        }

        val date = Date(System.currentTimeMillis()).toString()
        val delta = saleProduct.quantity
        SaleRepository.alterTotalPrice(cart, product, delta, 0, date)
        SaleRepository.alterTotalQuantity(cart, -delta, date)
        SaleProductRepository.deleteSaleProduct(saleProduct)

        call.respond(HttpStatusCode.OK, "Product successfully deleted!")
        return@delete
    }

    post("/alterQuantityCart/{idProduct}/{email}/{quantity}") {
        val idProduct = call.parameters["idProduct"]?.toIntOrNull()
        val email = call.parameters["email"]
        val quantity = call.parameters["quantity"]?.toLongOrNull()

        if(idProduct == null || email.isNullOrBlank() || quantity == null || quantity < 0) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val product = ProductRepository.getProductById(idProduct)
        if(product == null) {
            call.respond(HttpStatusCode.NotFound, "This product does not exist!")
            return@post
        }

        if(product.stock < quantity) {
            call.respond(HttpStatusCode.Unauthorized, "This quantity is over the product's stock!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@post
        }

        val sale = SaleRepository.getCartByAccount(account)
        val saleProduct = SaleProductRepository.getSaleProduct(sale.id.value, idProduct)

        if(saleProduct == null) {
            call.respond(HttpStatusCode.NotFound, "This product to sale assignment does not exist!")
            return@post
        }

        val previousQuantity = saleProduct.quantity

        val date = Date(System.currentTimeMillis()).toString()
        SaleProductRepository.alterQuantity(saleProduct, quantity)
        SaleRepository.alterTotalQuantity(sale, quantity-previousQuantity, date)
        SaleRepository.alterTotalPrice(sale, product, previousQuantity, quantity, date)

        call.respond(HttpStatusCode.OK, "Quantity successfully altered!")
        return@post
    }

    post("/alterCartAddress/{email}/{address}") {
        val email = call.parameters["email"]
        val address = call.parameters["address"]
        if(email.isNullOrBlank() || address.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid Parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null || account.type != "client") {
            call.respond(HttpStatusCode.BadRequest, "Invalid user!")
            return@post
        }

        val cart = SaleRepository.getCartByAccount(account)
        val date = Date(System.currentTimeMillis()).toString()

        SaleRepository.alterAddress(cart, address, date)
        call.respond(HttpStatusCode.OK)
        return@post
    }

    post("/finishOnlineSale/{email}") {
        val email = call.parameters["email"]
        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid Parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null || account.type != "client") {
            call.respond(HttpStatusCode.BadRequest, "Invalid user!")
            return@post
        }

        val cart = SaleRepository.getCartByAccount(account)
        val saleProducts = SaleProductRepository.getSaleProductsBySale(cart.id.value)

        for (product in saleProducts) {
            val check = ProductRepository.getProductById(product.idProduct)
            if(check == null || product.quantity > check.stock) {
                call.respond(HttpStatusCode.Unauthorized, "Couldn't finish your purchase!")
                return@post
            }
        }

        val date = Date(System.currentTimeMillis()).toString()
        SaleRepository.finishSale(cart, "Waiting for CarroçaBoy!", date)
        SaleRepository.newCart(account, date)

        for (product in saleProducts) {
            val productDAO = ProductRepository.getProductById(product.idProduct)
            ProductRepository.alterStock(productDAO!!, product.quantity)
        }

        call.respond(HttpStatusCode.OK, "You finished your purchase!")
        return@post
    }

    post("/acceptOrder/{email}/{idSale}") {
        val email = call.parameters["email"]
        val idSale = call.parameters["idSale"]?.toIntOrNull()

        if(email.isNullOrBlank() || idSale == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@post
        }

        val sale = SaleRepository.getAvailableSaleById(idSale)
        if(sale == null) {
            call.respond(HttpStatusCode.NotFound, "This sale does not exist or has already been accepted!")
            return@post
        }

        val saleToDeliver = SaleRepository.getSaleToBeDelivered(account)

        if(saleToDeliver != null) {
            call.respond(HttpStatusCode.Unauthorized, "You are already delivering an order!")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()

        SaleRepository.assignCarrocaBoy(sale, account, date)

        call.respond(HttpStatusCode.OK, "You have accepted this order!")
        return@post
    }

    post("/deliverOrder/{email}/{idSale}") {
        val email = call.parameters["email"]
        val idSale = call.parameters["idSale"]?.toIntOrNull()

        if(email.isNullOrBlank() || idSale == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@post
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null) {
            call.respond(HttpStatusCode.NotFound, "This account does not exist!")
            return@post
        }

        val sale = SaleRepository.getSaleToBeDeliveredById(idSale)
        if(sale == null) {
            call.respond(HttpStatusCode.NotFound, "This sale does not exist or is not being delivered!")
            return@post
        }

        val carrocaBoy = AccountRepository.getCarrocaBoyByEmail(email)
        if(carrocaBoy == null) {
            call.respond(HttpStatusCode.NotFound, "This CarroçaBoy does not exist!")
            return@post
        }

        val date = Date(System.currentTimeMillis()).toString()
        SaleRepository.finishSale(sale, "Delivered!", date)

        AccountRepository.addCommissionToEmployee(carrocaBoy, sale.totalPriceGold, date)

        call.respond(HttpStatusCode.OK, "You have delivered this order!")
        return@post
    }

    get("/availableSales") {
        call.respond(SaleRepository.getAvailableSales())
        return@get
    }

    get("/saleToBeDelivered/{email}") {
        val email = call.parameters["email"]

        if(email.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@get
        }

        val account = AccountRepository.getAccountByEmail(email)
        if(account == null || account.type != "carrocaboy") {
            call.respond(HttpStatusCode.NotFound, "This CarroçaBoy does not exist!")
            return@get
        }

        val saleToDeliver = SaleRepository.getSaleToBeDelivered(account)

        if(saleToDeliver == null) {
            call.respond(mapOf("res" to "You aren't delivering an order right now!"))
        } else {
            val res = suspendTransaction { daoToSale(saleToDeliver) }
            call.respond(res)
        }

        return@get
    }

    get("/saleProduct/{idSale}/{idProduct}") {
        val idSale = call.parameters["idSale"]?.toIntOrNull()
        val idProduct = call.parameters["idProduct"]?.toIntOrNull()

        if(idSale==null || idProduct == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid parameters!")
            return@get
        }

        val saleProduct = SaleProductRepository.getSaleProduct(idSale, idProduct)
        if(saleProduct == null) {
            call.respond(HttpStatusCode.NotFound, "This sale-product doesn't exist!")
            return@get
        }

        val res = suspendTransaction { daoToSaleProduct(saleProduct) }

        call.respond(res)
        return@get
    }
}