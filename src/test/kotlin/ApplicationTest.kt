package com.mac350

import com.mac350.plugins.configureSecurity
import com.mac350.plugins.generateToken
import io.ktor.client.request.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import com.mac350.repositories.AccountRepository
import com.mac350.repositories.SaleProductRepository
import com.mac350.repositories.SaleRepository
import com.mac350.repositories.UtilRepository
import com.mac350.tables.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.util.Date
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class ApplicationTest {

    @BeforeTest
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        UtilRepository.createTables()
    }

    @AfterTest
    fun clean() {
        transaction {
            AccountT.deleteAll()
            AdminT.deleteAll()
            CarrocaBoyT.deleteAll()
            CashierT.deleteAll()
            ClientT.deleteAll()
            ProductT.deleteAll()
            SaleProductT.deleteAll()
            SaleT.deleteAll()
            AmmunitionT.deleteAll()
            ArmorT.deleteAll()
            BookT.deleteAll()
            ClothingT.deleteAll()
            FoodT.deleteAll()
            IngredientT.deleteAll()
            MiscellanyT.deleteAll()
            OreT.deleteAll()
            PotionT.deleteAll()
            SoulGemT.deleteAll()
            WeaponT.deleteAll()
        }
    }

    @Test
    fun testSecurity() = testApplication {
        application {
            configureSecurity()
        }

        val token = generateToken("seila@email.com", "admin")
        val date = Date(System.currentTimeMillis() + 3600000)
        val decoded = JWT.decode(token)
        val expires = decoded.expiresAt
        val email = decoded.getClaim("email").asString()
        val type = decoded.getClaim("type").asString()
        assertEquals(email, "seila@email.com")
        assertEquals(type, "admin")
        assertTrue(expires.time - date.time <= 1000)
    }

    @Test
    fun `should create new sale product`() = runBlocking {
        val product = transaction {
            ProductDAO.new {
                this.productName = "test product"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product"
                this.type = "Ammunition"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 100
                this.totalQuantity = 1
                this.finished = false
                this.status = "test"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }

        val saleProduct = SaleProductRepository.newSaleProduct(sale, product)

        transaction {
            assertEquals(sale.id.value, saleProduct.idSale.id.value)
            assertEquals(product.id.value, saleProduct.idProduct.id.value)
            assertEquals(1, saleProduct.quantity)
        }
    }

    @Test
    fun `should return sale product by sale and product ids`() = runBlocking {
        val product = transaction {
            ProductDAO.new {
                this.productName = "test product"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product"
                this.type = "Ammunition"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 100
                this.totalQuantity = 1
                this.finished = false
                this.status = "test"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val created = SaleProductRepository.newSaleProduct(sale, product)

        val found = SaleProductRepository.getSaleProduct(sale.id.value, product.id.value)

        transaction {
            assertNotNull(found)
            assertEquals(created.id.value, found!!.id.value)
        }
    }

    @Test
    fun `should return all sale products by sale`() = runBlocking {
        val product1 = transaction {
            ProductDAO.new {
                this.productName = "test product 1"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product 1"
                this.type = "Ammunition"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val product2 = transaction {
            ProductDAO.new {
                this.productName = "test product 2"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product 2"
                this.type = "Miscellaneous"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 200
                this.totalQuantity = 2
                this.finished = false
                this.status = "test"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }

        SaleProductRepository.newSaleProduct(sale, product1)
        SaleProductRepository.newSaleProduct(sale, product2)

        val results = SaleProductRepository.getSaleProductsBySale(sale.id.value)

        assertEquals(2, results.size)
    }

    @Test
    fun `should return cart size for given sale`() = runBlocking {
        val product1 = transaction {
            ProductDAO.new {
                this.productName = "test product 1"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product 1"
                this.type = "Ammunition"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val product2 = transaction {
            ProductDAO.new {
                this.productName = "test product 2"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product 2"
                this.type = "Miscellaneous"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 200
                this.totalQuantity = 2
                this.finished = false
                this.status = "test"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }

        SaleProductRepository.newSaleProduct(sale, product1)
        SaleProductRepository.newSaleProduct(sale, product2)

        val size = SaleProductRepository.getCartSize(sale.id.value)

        assertEquals(2, size)
    }

    @Test
    fun `should update quantity of sale product`() = runBlocking {
        val product = transaction {
            ProductDAO.new {
                this.productName = "test product"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product"
                this.type = "Ammunition"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 100
                this.totalQuantity = 1
                this.finished = false
                this.status = "test"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }

        val sp = SaleProductRepository.newSaleProduct(sale, product)
        SaleProductRepository.alterQuantity(sp, 5)

        val updated = SaleProductRepository.getSaleProduct(sale.id.value, product.id.value)

        assertEquals(5, updated?.quantity)
    }

    @Test
    fun `should delete sale product`() = runBlocking {
        val product = transaction {
            ProductDAO.new {
                this.productName = "test product"
                this.image = null
                this.priceGold = 100
                this.stock = 100
                this.description = "test product"
                this.type = "Ammunition"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 100
                this.totalQuantity = 1
                this.finished = false
                this.status = "test"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }

        val sp = SaleProductRepository.newSaleProduct(sale, product)
        SaleProductRepository.deleteSaleProduct(sp)

        val result = SaleProductRepository.getSaleProduct(sale.id.value, product.id.value)

        assertNull(result)
    }

    @Test
    fun `should create a cart with correct fields`() = runBlocking {
        val account = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val date = "today"
        val cart = SaleRepository.newCart(account, date)

        transaction {
            assertEquals(account.id.value, cart.idClient?.id?.value)
            assertEquals("Cart", cart.status)
            assertEquals(false, cart.finished)
        }
    }

    @Test
    fun `should return existing cart`() = runBlocking {
        val account = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val date = "today"
        val created = SaleRepository.newCart(account, date)
        val fetched = SaleRepository.getCartByAccount(account)
        transaction { assertEquals(created.id.value, fetched.id.value) }
    }

    @Test
    fun `should create a new cart if none exists`() = runBlocking {
        val account = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val fetched = SaleRepository.getCartByAccount(account)
        transaction {
            assertEquals(account.id.value, fetched.idClient?.id?.value)
            assertEquals("Cart", fetched.status)
        }
    }

    @Test
    fun `should create sale with employee and status`() = runBlocking {
        val employee = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "cashier"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val date = "today"
        val sale = SaleRepository.newIrlPurchase(employee, date)
        transaction {
            assertEquals(employee.id.value, sale.idEmployee?.id?.value)
            assertEquals("Ongoing IRL purchase", sale.status)
            assertEquals(false, sale.finished)
        }
    }

    @Test
    fun `should return correct sale`() = runBlocking {
        val account = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val date = "today"
        val sale = SaleRepository.newCart(account, date)
        val result = SaleRepository.getSaleById(sale.id.value)
        assertNotNull(result)
        transaction { assertEquals(sale.id.value, result!!.id.value) }
    }

    @Test
    fun `should set idClient of sale`() = runBlocking {
        val cashier = transaction {
            AccountDAO.new {
                this.username = "username1"
                this.email = "email1"
                this.password = "hashed"
                this.type = "cashier"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val client = transaction {
            AccountDAO.new {
                this.username = "username1"
                this.email = "email2"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val date = "today"
        val sale = SaleRepository.newIrlPurchase(cashier, date)
        SaleRepository.assignClient(client, sale)
        transaction { assertEquals(client.id.value, sale.idClient?.id?.value) }
    }

    @Test
    fun `should mark sale as finished with custom message`() = runBlocking {
        val client = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val date = "yesterday"
        val sale = SaleRepository.newCart(client, date)
        SaleRepository.finishSale(sale, "Delivered!", "today")
        transaction {
            assertTrue(sale.finished)
            assertEquals("Delivered!", sale.status)
            assertEquals("today", sale.updatedAt)
        }
    }

    @Test
    fun `should update quantity and timestamp`() = runBlocking {
        val client = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val sale = SaleRepository.newCart(client, "yesterday")
        SaleRepository.alterTotalQuantity(sale, 3, "today")
        transaction {
            assertEquals(3, sale.totalQuantity)
            assertEquals("today", sale.updatedAt)
        }
    }

    @Test
    fun `should update price and timestamp`() = runBlocking {
        val client = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "client"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val product = transaction {
            ProductDAO.new {
                this.productName = "test product"
                this.image = null
                this.priceGold = 10
                this.stock = 100
                this.description = "test product"
                this.type = "Ammunition"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale = SaleRepository.newCart(client, "yesterday")
        SaleRepository.alterTotalPrice(sale, product, 0, 3, "today")
        transaction {
            assertEquals(30, sale.totalPriceGold)
            assertEquals("today", sale.updatedAt)
        }
    }

    @Test
    fun `hashPw and checkPw should work correctly`() {
        val rawPassword = "test123"
        val hash = AccountRepository.hashPw(rawPassword)
        assertTrue(AccountRepository.checkPw(rawPassword, hash))
        assertFalse(AccountRepository.checkPw("wrongpass", hash))
    }

    @Test
    fun `should create an account correctly`() = runTest {
        val acc = AccountRepository.newAccount("user1", "u1@mail.com", "pass123", "client", "2024-01-01")
        assertEquals("user1", acc.username)
        assertEquals("u1@mail.com", acc.email)
        assertEquals("client", acc.type)
    }
}
