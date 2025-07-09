package com.mac350

import com.mac350.plugins.configureSecurity
import com.mac350.plugins.generateToken
import io.ktor.client.request.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import com.mac350.models.Weapon
import com.mac350.repositories.*
import com.mac350.tables.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
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

    // *************************
    // SECURITY FUNCTIONS TESTS
    // *************************

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
    fun `hashPw and checkPw should work correctly`() {
        val rawPassword = "test123"
        val hash = AccountRepository.hashPw(rawPassword)
        assertTrue(AccountRepository.checkPw(rawPassword, hash))
        assertFalse(AccountRepository.checkPw("wrongpass", hash))
    }


    // *************************
    // ACCOUNT FUNCTIONS TESTS
    // *************************

    @Test
    fun `should create an account correctly`() = runBlocking {
        val acc = AccountRepository.newAccount("user1", "u1@mail.com", "pass123", "client", "2024-01-01")
        assertEquals("user1", acc.username)
        assertEquals("u1@mail.com", acc.email)
        assertEquals("client", acc.type)
    }

    @Test
    fun `should update username, email and date`() = runBlocking {
        val acc = AccountRepository.newAccount("olduser", "old@mail.com", "pass", "client", "2024-01-01")
        AccountRepository.editAccount(acc, "newuser", "new@mail.com", "2024-02-01")
        assertEquals("newuser", acc.username)
        assertEquals("new@mail.com", acc.email)
        assertEquals("2024-02-01", acc.updatedAt)
    }

    @Test
    fun `should update password hash`() = runBlocking {
        val acc = AccountRepository.newAccount("pwUser", "pw@mail.com", "oldpw", "client", "2024-01-01")
        val oldHash = acc.password
        AccountRepository.editPw(acc, "newpw")
        val newHash = acc.password
        assertNotEquals(oldHash, newHash)
        assertTrue(AccountRepository.checkPw("newpw", newHash))
    }

    @Test
    fun `should return correct account by id`() = runBlocking {
        val acc = AccountRepository.newAccount("lookupId", "lookupid@mail.com", "pass", "client", "2024-01-01")
        val found = AccountRepository.getAccountById(acc.id.value)
        assertNotNull(found)
        assertEquals("lookupId", found!!.username)
    }

    @Test
    fun `should return correct account by email`() = runBlocking {
        val acc = AccountRepository.newAccount("lookupEmail", "lookupemail@mail.com", "pass", "client", "2024-01-01")
        val found = AccountRepository.getAccountByEmail("lookupemail@mail.com")
        assertNotNull(found)
        assertEquals(acc.id.value, found!!.id.value)
    }

    @Test
    fun `should remove account`() = runBlocking {
        val acc = AccountRepository.newAccount("toDelete", "del@mail.com", "pass", "client", "2024-01-01")
        AccountRepository.deleteAccount(acc)
        val found = AccountRepository.getAccountById(acc.id.value)
        assertNull(found)
    }

    @Test
    fun `should create admin with root flag`() = runBlocking {
        val acc = AccountRepository.newAccount("adminX", "adminx@mail.com", "pass", "admin", "2024-01-01")
        val admin = AccountRepository.newAdmin(acc, "Yes")
        transaction {
            assertEquals(acc.id.value, admin.account.id.value)
            assertEquals("Yes", admin.root)
        }
    }

    @Test
    fun `should update root flag`() = runBlocking {
        val acc = AccountRepository.newAccount("adminY", "adminy@mail.com", "pass", "admin", "2024-01-01")
        val admin = AccountRepository.newAdmin(acc, "false")
        AccountRepository.editAdmin(admin, "true")
        assertEquals("true", admin.root)
    }

    @Test
    fun `should create carrocaboy with zero commissions`() = runBlocking {
        val acc = AccountRepository.newAccount("carrocaNew", "cbnew@mail.com", "pass", "carrocaboy", "2024-01-01")
        val cb = AccountRepository.newCarrocaBoy(acc)
        transaction {
            assertEquals(acc.id.value, cb.account.id.value)
            assertEquals(0, cb.totalCommissions)
        }
    }

    @Test
    fun `should create cashier with section and zero commissions`() = runBlocking {
        val acc = AccountRepository.newAccount("cashNew", "cnew@mail.com", "pass", "cashier", "2024-01-01")
        val cashier = AccountRepository.newCashier(acc, 99L)
        transaction {
            assertEquals(acc.id.value, cashier.account.id.value)
            assertEquals(0, cashier.totalCommissions)
            assertEquals(99L, cashier.section)
        }
    }

    @Test
    fun `should create client with address`() = runBlocking {
        val acc = AccountRepository.newAccount("clientX", "clix@mail.com", "pass", "client", "2024-01-01")
        val client = AccountRepository.newClient(acc, "new street")
        transaction {
            assertEquals(acc.id.value, client.account.id.value)
            assertEquals("new street", client.address)
            assertFalse(client.isSpecialClient)
        }
    }

    @Test
    fun `should change client address`() = runBlocking {
        val acc = AccountRepository.newAccount("clientY", "cliy@mail.com", "pass", "client", "2024-01-01")
        val client = AccountRepository.newClient(acc, "old address")
        AccountRepository.editClient(client, "new address")
        assertEquals("new address", client.address)
    }

    @Test
    fun `should return list with created admin`() = runBlocking {
        val acc = AccountRepository.newAccount("adminList", "a1@mail.com", "pass", "admin", "2024-01-01")
        AccountRepository.newAdmin(acc, "false")
        val admins = AccountRepository.getAdmins()
        assertEquals(1, admins.size)
        assertEquals("adminList", admins.first().username)
    }

    @Test
    fun `should return list with created carrocaBoy`() = runBlocking {
        val acc = AccountRepository.newAccount("cbList", "cb1@mail.com", "pass", "carrocaboy", "2024-01-01")
        AccountRepository.newCarrocaBoy(acc)
        val list = AccountRepository.getCarrocaBoys()
        assertEquals(1, list.size)
        assertEquals("cbList", list.first().username)
    }

    @Test
    fun `should return list with created cashier`() = runBlocking {
        val acc = AccountRepository.newAccount("cashList", "cash1@mail.com", "pass", "cashier", "2024-01-01")
        AccountRepository.newCashier(acc, 1L)
        val list = AccountRepository.getCashiers()
        assertEquals(1, list.size)
        assertEquals("cashList", list.first().username)
    }

    @Test
    fun `should return list with created client`() = runBlocking {
        val acc = AccountRepository.newAccount("clientList", "cl1@mail.com", "pass", "client", "2024-01-01")
        AccountRepository.newClient(acc, "Rua A")
        val list = AccountRepository.getClients()
        assertEquals(1, list.size)
        assertEquals("clientList", list.first().username)
    }

    @Test
    fun `should return correct admin by id`() = runBlocking {
        val acc = AccountRepository.newAccount("adminID", "aid@mail.com", "pass", "admin", "2024-01-01")
        val admin = AccountRepository.newAdmin(acc, "true")
        val result = AccountRepository.getAdminById(acc.id.value)
        assertNotNull(result)
        assertEquals(admin.id.value, result!!.id.value)
    }

    @Test
    fun `should return correct admin by email`() = runBlocking {
        val acc = AccountRepository.newAccount("adminEmail", "admin@email.com", "pass", "admin", "2024-01-01")
        val admin = AccountRepository.newAdmin(acc, "false")
        val result = AccountRepository.getAdminByEmail("admin@email.com")
        assertNotNull(result)
        assertEquals(admin.id.value, result!!.id.value)
    }

    @Test
    fun `should return correct client by id`() = runBlocking {
        val acc = AccountRepository.newAccount("cliID", "cliid@mail.com", "pass", "client", "2024-01-01")
        val client = AccountRepository.newClient(acc, "Rua X")
        val result = AccountRepository.getClientById(acc.id.value)
        assertNotNull(result)
        assertEquals(client.id.value, result!!.id.value)
    }

    @Test
    fun `should return correct client by email`() = runBlocking {
        val acc = AccountRepository.newAccount("cliEmail", "cli@email.com", "pass", "client", "2024-01-01")
        val client = AccountRepository.newClient(acc, "Rua Y")
        val result = AccountRepository.getClientByEmail("cli@email.com")
        assertNotNull(result)
        assertEquals(client.id.value, result!!.id.value)
    }

    @Test
    fun `should return correct cashier by id`() = runBlocking {
        val acc = AccountRepository.newAccount("cashID", "cashid@mail.com", "pass", "cashier", "2024-01-01")
        val cashier = AccountRepository.newCashier(acc, 5L)
        val result = AccountRepository.getCashierById(acc.id.value)
        assertNotNull(result)
        assertEquals(cashier.id.value, result!!.id.value)
    }

    @Test
    fun `should return correct cashier by email`() = runBlocking {
        val acc = AccountRepository.newAccount("cashEmail", "cash@email.com", "pass", "cashier", "2024-01-01")
        val cashier = AccountRepository.newCashier(acc, 7L)
        val result = AccountRepository.getCashierByEmail("cash@email.com")
        assertNotNull(result)
        assertEquals(cashier.id.value, result!!.id.value)
    }

    @Test
    fun `should return correct carrocaboy by id`() = runBlocking {
        val acc = AccountRepository.newAccount("cbID", "cbid@mail.com", "pass", "carrocaboy", "2024-01-01")
        val cb = AccountRepository.newCarrocaBoy(acc)
        val result = AccountRepository.getCarrocaBoyById(acc.id.value)
        assertNotNull(result)
        assertEquals(cb.id.value, result!!.id.value)
    }

    @Test
    fun `should return correct carrocaboy by email`() = runBlocking {
        val acc = AccountRepository.newAccount("cbEmail", "cb@email.com", "pass", "carrocaboy", "2024-01-01")
        val cb = AccountRepository.newCarrocaBoy(acc)
        val result = AccountRepository.getCarrocaBoyByEmail("cb@email.com")
        assertNotNull(result)
        assertEquals(cb.id.value, result!!.id.value)
    }

    @Test
    fun `should update lastRun field`() = runBlocking {
        val acc = AccountRepository.newAccount("lastrunner", "lastrun@mail.com", "pass", "client", "yesterday")
        AccountRepository.setLastRun(acc, "today")
        assertEquals("today", acc.lastRun)
    }

    @Test
    fun `should update cashier and carrocaboy commissions and updatedAts`() = runBlocking {
        val acc1 = AccountRepository.newAccount("cashcomm", "comm@cash.com", "pass", "cashier", "2024-01-01")
        val acc2 = AccountRepository.newAccount("cbcomm", "comm@cb.com", "pass", "carrocaboy", "2024-01-01")
        val cashier = AccountRepository.newCashier(acc1, 3L)
        val cb = AccountRepository.newCarrocaBoy(acc2)
        AccountRepository.addCommissionToEmployee(cashier, 1000L, "today")
        AccountRepository.addCommissionToEmployee(cb, 500L, "today")
        transaction {
            assertEquals(100, cashier.totalCommissions)
            assertEquals(50, cb.totalCommissions)
            assertEquals("today", cashier.account.updatedAt)
            assertEquals("today", cb.account.updatedAt)
        }
    }


    // *************************
    // PRODUCT FUNCTIONS TESTS
    // *************************

    @Test
    fun `should create a product correctly`() = runBlocking {
        val product = ProductRepository.newProduct("product", 100, 200, "desc", "testType", "2024-01-01")
        transaction {
            assertEquals("product", product.productName)
            assertEquals(100, product.priceGold)
            assertEquals(200, product.stock)
            assertEquals("desc", product.description)
            assertEquals("testType", product.type)
            assertEquals("2024-01-01", product.createdAt)
            assertEquals("2024-01-01", product.updatedAt)
        }
    }

    @Test
    fun `should return all products when type is all products`() = runBlocking {
        val p1 = ProductRepository.newProduct("product1", 100, 300, "desc1", "Ammunition", "2024-01-01")
        val p2 = ProductRepository.newProduct("product2", 50, 200, "desc2", "Potions", "2024-01-01")
        val result = ProductRepository.getProducts("All products", null, null, null, null)
        assertNotNull(result)
        assertEquals(2, result!!.size)
    }

    @Test
    fun `should return one product when type is potions`() = runBlocking {
        ProductRepository.newProduct("product1", 100, 300, "desc1", "Ammunition", "2024-01-01")
        ProductRepository.newProduct("product2", 50, 200, "desc2", "Potions", "2024-01-01")
        val result = ProductRepository.getProducts("Potions", null, null, null, null)
        assertNotNull(result)
        assertEquals(1, result!!.size)
    }

    @Test
    fun `should return null for invalid type`() = runBlocking {
        val result = ProductRepository.getProducts("InvalidType", null, null, null, null)
        assertNull(result)
    }

    @Test
    fun `should filter by name`() = runBlocking {
        ProductRepository.newProduct("Steel Armor", 100, 300, "desc1", "Armor", "2024-01-01")
        ProductRepository.newProduct("Iron Helmet", 75, 300, "desc2", "Armor", "2024-01-01")

        val result = ProductRepository.getProducts("Armor", "steel", null, null, null)
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertTrue(result[0].productName.contains("Steel"))
    }

    @Test
    fun `should filter by price range`() = runBlocking {
        ProductRepository.newProduct("Cheap Item", 20, 300, "desc1", "Miscellaneous", "2024-01-01")
        ProductRepository.newProduct("Expensive Item", 500, 300, "desc2", "Miscellaneous", "2024-01-01")

        val result = ProductRepository.getProducts("Miscellaneous", null, 100, 600, null)
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("Expensive Item", result[0].productName)
    }

    @Test
    fun `should order results by price ascending`() = runBlocking {
        ProductRepository.newProduct("Item A", 300, 300, "desc1", "Books", "2024-01-01")
        ProductRepository.newProduct("Item B", 100, 300, "desc2", "Books", "2024-01-01")

        val result = ProductRepository.getProducts("Books", null, null, null, "PriceAsc")
        assertNotNull(result)
        assertEquals("Item B", result!![0].productName)
    }

    @Test
    fun `should return correct product`() = runBlocking {
        val product = ProductRepository.newProduct("Ebony Bow", 800, 300, "desc1", "Weapons", "2024-01-01")

        val result = ProductRepository.getProductById(product.id.value)
        assertNotNull(result)
        assertEquals("Ebony Bow", result!!.productName)
        assertEquals(800, result.priceGold)
    }

    @Test
    fun `should update product fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("Old name", 100, 10, "Old desc", "Weapons", "2024-01-01")

        ProductRepository.editProduct(
            product,
            productName = "New Name",
            priceGold = 200,
            stock = 50,
            description = "Updated Desc",
            date = "Today"
        )

        assertEquals("New Name", product.productName)
        assertEquals(200, product.priceGold)
        assertEquals(50, product.stock)
        assertEquals("Updated Desc", product.description)
        assertEquals("Today", product.updatedAt)
    }

    @Test
    fun `should create correct ammunition`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ammunition", "2024-01-01")

        val ammunition = ProductRepository.newAmmunition(
            product,
            magical = "yes",
            craft = "Blacksmith",
            speed = 85.5,
            gravity = 9.8,
            category = "Fire"
        )

        transaction {  assertEquals(product.id.value, ammunition.product.id.value) }
        assertEquals("yes", ammunition.magical)
        assertEquals("Blacksmith", ammunition.craft)
        assertEquals(85.5, ammunition.speed)
        assertEquals(9.8, ammunition.gravity)
        assertEquals("Fire", ammunition.category)
    }

    @Test
    fun `should update ammunition fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ammunition", "2024-01-01")

        val ammunition = ProductRepository.newAmmunition(
            product,
            magical = "no",
            craft = "Workshop",
            speed = 70.0,
            gravity = 10.0,
            category = "Ice"
        )

        ProductRepository.editAmmunition(
            ammunition,
            magical = "yes",
            craft = "Forge",
            speed = 90.0,
            gravity = 9.5,
            category = "Explosive"
        )

        assertEquals("yes", ammunition.magical)
        assertEquals("Forge", ammunition.craft)
        assertEquals(90.0, ammunition.speed)
        assertEquals(9.5, ammunition.gravity)
        assertEquals("Explosive", ammunition.category)
    }

    @Test
    fun `should create correct armor`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Armor", "2024-01-01")

        val armor = ProductRepository.newArmor(
            product,
            weight = 100.5,
            magical = "yes",
            craft = "Blacksmith",
            protection = 85.5,
            heavy = "yes",
            category = "Fire"
        )

        transaction {  assertEquals(product.id.value, armor.product.id.value)}
        assertEquals(100.5, armor.weight)
        assertEquals("yes", armor.magical)
        assertEquals("Blacksmith", armor.craft)
        assertEquals(85.5, armor.protection)
        assertEquals("yes", armor.heavy)
        assertEquals("Fire", armor.category)
    }

    @Test
    fun `should update armor fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Armor", "2024-01-01")

        val armor = ProductRepository.newArmor(
            product,
            weight = 10.5,
            magical = "no",
            craft = "Nordic",
            protection = 70.5,
            heavy = "no",
            category = "Water"
        )

        ProductRepository.editArmor(
            armor,
            weight = 100.5,
            magical = "yes",
            craft = "Blacksmith",
            protection = 85.5,
            heavy = "yes",
            category = "Fire"
        )

        assertEquals(100.5, armor.weight)
        assertEquals("yes", armor.magical)
        assertEquals("Blacksmith", armor.craft)
        assertEquals(85.5, armor.protection)
        assertEquals("yes", armor.heavy)
        assertEquals("Fire", armor.category)
    }

    @Test
    fun `should create correct book`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Book", "2024-01-01")

        val book = ProductRepository.newBook(
            product,
            skillTaught = "swimming",
            magical = "yes",
            pages = 7
        )

        transaction {  assertEquals(product.id.value, book.product.id.value) }
        assertEquals("swimming", book.skillTaught)
        assertEquals("yes", book.magical)
        assertEquals(7, book.pages)
    }

    @Test
    fun `should update book fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Book", "2024-01-01")

        val book = ProductRepository.newBook(
            product,
            skillTaught = "walking",
            magical = "no",
            pages = 10
        )

        ProductRepository.editBook(
            book,
            skillTaught = "swimming",
            magical = "yes",
            pages = 7
        )

        assertEquals("swimming", book.skillTaught)
        assertEquals("yes", book.magical)
        assertEquals(7, book.pages)
    }

    @Test
    fun `should create correct clothing`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Clothing", "2024-01-01")

        val clothing = ProductRepository.newClothing(
            product,
            protection = 10,
            slot = "torso",
            enchantment = "drip V",
            enchanted = "yes",
            weight = 5.5
        )

        transaction {  assertEquals(product.id.value, clothing.product.id.value) }
        assertEquals(10, clothing.protection)
        assertEquals("torso", clothing.slot)
        assertEquals("drip V", clothing.enchantment)
        assertEquals("yes", clothing.enchanted)
        assertEquals(5.5, clothing.weight)
    }

    @Test
    fun `should update clothing fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Clothing", "2024-01-01")

        val clothing = ProductRepository.newClothing(
            product,
            protection = 1,
            slot = "legs",
            enchantment = "drip I",
            enchanted = "no",
            weight = 15.5
        )

        ProductRepository.editClothing(
            clothing,
            protection = 10,
            slot = "torso",
            enchantment = "drip V",
            enchanted = "yes",
            weight = 5.5
        )

        assertEquals(10, clothing.protection)
        assertEquals("torso", clothing.slot)
        assertEquals("drip V", clothing.enchantment)
        assertEquals("yes", clothing.enchanted)
        assertEquals(5.5, clothing.weight)
    }

    @Test
    fun `should create correct food`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Food", "2024-01-01")

        val food = ProductRepository.newFood(
            product,
            weight = 10.5,
            healthRestored = 1,
            staminaRestored = 2,
            magickaRestored = 3,
            duration = 5
        )

        transaction {  assertEquals(product.id.value, food.product.id.value) }
        assertEquals(10.5, food.weight)
        assertEquals(1, food.healthRestored)
        assertEquals(2, food.staminaRestored)
        assertEquals(3, food.magickaRestored)
        assertEquals(5, food.duration)
    }

    @Test
    fun `should update food fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Food", "2024-01-01")

        val food = ProductRepository.newFood(
            product,
            weight = 1.5,
            healthRestored = 2,
            staminaRestored = 3,
            magickaRestored = 4,
            duration = 6
        )

        ProductRepository.editFood(
            food,
            weight = 10.5,
            healthRestored = 1,
            staminaRestored = 2,
            magickaRestored = 3,
            duration = 5
        )

        assertEquals(10.5, food.weight)
        assertEquals(1, food.healthRestored)
        assertEquals(2, food.staminaRestored)
        assertEquals(3, food.magickaRestored)
        assertEquals(5, food.duration)
    }

    @Test
    fun `should create correct ingredient`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ingredients", "2024-01-01")

        val ingredient = ProductRepository.newIngredient(
            product,
            weight = 10.5,
            magical = "yes",
            effects = "makes you fly"
        )

        transaction {  assertEquals(product.id.value, ingredient.product.id.value) }
        assertEquals(10.5, ingredient.weight)
        assertEquals("yes", ingredient.magical)
        assertEquals("makes you fly", ingredient.effects)
    }

    @Test
    fun `should update ingredient fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ingredients", "2024-01-01")

        val ingredient = ProductRepository.newIngredient(
            product,
            weight = 5.1,
            magical = "no",
            effects = "kills you"
        )

        ProductRepository.editIngredient(
            ingredient,
            weight = 10.5,
            magical = "yes",
            effects = "makes you fly"
        )

        assertEquals(10.5, ingredient.weight)
        assertEquals("yes", ingredient.magical)
        assertEquals("makes you fly", ingredient.effects)
    }

    @Test
    fun `should create correct miscellany`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Miscellaneous", "2024-01-01")

        val miscellany = ProductRepository.newMiscellany(
            product,
            questItem = "yes",
            craftingUse = "blacksmithing",
            modelType = "sphere"
        )

        transaction {  assertEquals(product.id.value, miscellany.product.id.value) }
        assertEquals("yes", miscellany.questItem)
        assertEquals("blacksmithing", miscellany.craftingUse)
        assertEquals("sphere", miscellany.modelType)
    }

    @Test
    fun `should update miscellany fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Miscellaneous", "2024-01-01")

        val miscellany = ProductRepository.newMiscellany(
            product,
            questItem = "no",
            craftingUse = "minecraft",
            modelType = "sphere"
        )

        ProductRepository.editMiscellany(
            miscellany,
            questItem = "yes",
            craftingUse = "blacksmithing",
            modelType = "sphere"
        )

        assertEquals("yes", miscellany.questItem)
        assertEquals("blacksmithing", miscellany.craftingUse)
        assertEquals("sphere", miscellany.modelType)
    }

    @Test
    fun `should create correct ore`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ore", "2024-01-01")

        val ore = ProductRepository.newOre(
            product,
            weight = 6.1,
            metalType = "soft",
            smeltedInto = "iron ingot"
        )

        transaction {  assertEquals(product.id.value, ore.product.id.value) }
        assertEquals(6.1, ore.weight)
        assertEquals("soft", ore.metalType)
        assertEquals("iron ingot", ore.smeltedInto)
    }

    @Test
    fun `should update ore fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ore", "2024-01-01")

        val ore = ProductRepository.newOre(
            product,
            weight = 4.8,
            metalType = "hard",
            smeltedInto = "gold nuggets"
        )

        ProductRepository.editOre(
            ore,
            weight = 6.1,
            metalType = "soft",
            smeltedInto = "iron ingot"
        )

        assertEquals(6.1, ore.weight)
        assertEquals("soft", ore.metalType)
        assertEquals("iron ingot", ore.smeltedInto)
    }

    @Test
    fun `should create correct potion`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Potions", "2024-01-01")

        val potion = ProductRepository.newPotion(
            product,
            effects = "slowness",
            duration = 10,
            magnitude = "small",
            poisoned = "no"
        )

        transaction {  assertEquals(product.id.value, potion.product.id.value) }
        assertEquals("slowness", potion.effects)
        assertEquals(10, potion.duration)
        assertEquals("small", potion.magnitude)
        assertEquals("no", potion.poisoned)
    }

    @Test
    fun `should update potion fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Potions", "2024-01-01")

        val potion = ProductRepository.newPotion(
            product,
            effects = "swiftness",
            duration = 29,
            magnitude = "large",
            poisoned = "yes"
        )

        ProductRepository.editPotion(
            potion,
            effects = "slowness",
            duration = 10,
            magnitude = "small",
            poisoned = "no"
        )

        assertEquals("slowness", potion.effects)
        assertEquals(10, potion.duration)
        assertEquals("small", potion.magnitude)
        assertEquals("no", potion.poisoned)
    }

    @Test
    fun `should create correct soul gem`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Soul Gems", "2024-01-01")

        val soulgem = ProductRepository.newSoulGem(
            product,
            soulSize = "large",
            isFilled = "yes",
            containedSoul = "skeleton",
            canCapture = "yes",
            reusable = "yes"
        )

        transaction {  assertEquals(product.id.value, soulgem.product.id.value) }
        assertEquals("large", soulgem.soulSize)
        assertEquals("yes", soulgem.isFilled)
        assertEquals("skeleton", soulgem.containedSoul)
        assertEquals("yes", soulgem.canCapture)
        assertEquals("yes", soulgem.reusable)
    }

    @Test
    fun `should update soul gem fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Soul Gems", "2024-01-01")

        val soulgem = ProductRepository.newSoulGem(
            product,
            soulSize = "small",
            isFilled = "no",
            containedSoul = "zombie",
            canCapture = "no",
            reusable = "no"
        )

        ProductRepository.editSoulGem(
            soulgem,
            soulSize = "large",
            isFilled = "yes",
            containedSoul = "skeleton",
            canCapture = "yes",
            reusable = "yes"
        )

        assertEquals("large", soulgem.soulSize)
        assertEquals("yes", soulgem.isFilled)
        assertEquals("skeleton", soulgem.containedSoul)
        assertEquals("yes", soulgem.canCapture)
        assertEquals("yes", soulgem.reusable)
    }

    @Test
    fun `should create correct weapon`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Weapons", "2024-01-01")

        val weapon = ProductRepository.newWeapon(
            product,
            weight = 5.5,
            magical = "yes",
            craft = "nordic",
            damage = 10,
            speed = 9.5,
            reach = 15,
            stagger = 4.4,
            battleStyle = "soresu",
            category = "sword"
        )

        transaction {  assertEquals(product.id.value, weapon.product.id.value) }
        assertEquals(5.5, weapon.weight)
        assertEquals("yes", weapon.magical)
        assertEquals("nordic", weapon.craft)
        assertEquals(10, weapon.damage)
        assertEquals(9.5, weapon.speed)
        assertEquals(15, weapon.reach)
        assertEquals(4.4, weapon.stagger)
        assertEquals("soresu", weapon.battleStyle)
        assertEquals("sword", weapon.category)
    }

    @Test
    fun `should update weapon fields correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Weapons", "2024-01-01")

        val weapon = ProductRepository.newWeapon(
            product,
            weight = 2.0,
            magical = "no",
            craft = "scandinavian",
            damage = 6,
            speed = 15.5,
            reach = 5,
            stagger = 8.2,
            battleStyle = "ataru",
            category = "dagger"
        )

        ProductRepository.editWeapon(
            weapon,
            weight = 5.5,
            magical = "yes",
            craft = "nordic",
            damage = 10,
            speed = 9.5,
            reach = 15,
            stagger = 4.4,
            battleStyle = "soresu",
            category = "sword"
        )

        assertEquals(5.5, weapon.weight)
        assertEquals("yes", weapon.magical)
        assertEquals("nordic", weapon.craft)
        assertEquals(10, weapon.damage)
        assertEquals(9.5, weapon.speed)
        assertEquals(15, weapon.reach)
        assertEquals(4.4, weapon.stagger)
        assertEquals("soresu", weapon.battleStyle)
        assertEquals("sword", weapon.category)
    }


    @Test
    fun `should return correct AmmunitionDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ammunition", "2024-01-01")

        val ammunition = ProductRepository.newAmmunition(
            product,
            magical = "yes",
            craft = "Blacksmith",
            speed = 85.5,
            gravity = 9.8,
            category = "Fire"
        )

        val result = ProductRepository.getAmmunition(product.id.value)
        assertNotNull(result)
        transaction { assertEquals(ammunition.id.value, result!!.id.value) }
    }


    @Test
    fun `should return correct ArmorDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Armor", "2024-01-01")

        val armor = ProductRepository.newArmor(
            product,
            weight = 100.5,
            magical = "yes",
            craft = "Blacksmith",
            protection = 85.5,
            heavy = "yes",
            category = "Fire"
        )

        val result = ProductRepository.getArmor(product.id.value)
        assertNotNull(result)
        transaction { assertEquals(armor.id.value, result!!.id.value) }
    }

    @Test
    fun `should return correct BookDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Book", "2024-01-01")

        val book = ProductRepository.newBook(
            product,
            skillTaught = "swimming",
            magical = "yes",
            pages = 7
        )

        val result = ProductRepository.getBook(product.id.value)
        assertNotNull(result)
        assertEquals("swimming", result!!.skillTaught)
    }

    @Test
    fun `should return correct ClothingDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Clothing", "2024-01-01")

        val clothing = ProductRepository.newClothing(
            product,
            protection = 10,
            slot = "torso",
            enchantment = "drip V",
            enchanted = "yes",
            weight = 5.5
        )

        val result = ProductRepository.getClothing(product.id.value)
        assertNotNull(result)
        assertEquals("drip V", result!!.enchantment)
    }

    @Test
    fun `should return correct FoodDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Food", "2024-01-01")

        val food = ProductRepository.newFood(
            product,
            weight = 10.5,
            healthRestored = 1,
            staminaRestored = 2,
            magickaRestored = 3,
            duration = 5
        )

        val result = ProductRepository.getFood(product.id.value)
        assertNotNull(result)
        assertEquals(1, result!!.healthRestored)
    }

    @Test
    fun `should return correct IngredientDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ingredients", "2024-01-01")

        val ingredient = ProductRepository.newIngredient(
            product,
            weight = 10.5,
            magical = "yes",
            effects = "makes you fly"
        )

        val result = ProductRepository.getIngredient(product.id.value)
        assertNotNull(result)
        transaction { assertEquals(ingredient.id.value, result!!.id.value) }
    }

    @Test
    fun `should return correct MiscellanyDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Miscellaneous", "2024-01-01")

        val miscellany = ProductRepository.newMiscellany(
            product,
            questItem = "yes",
            craftingUse = "blacksmithing",
            modelType = "sphere"
        )

        val result = ProductRepository.getMiscellany(product.id.value)
        assertNotNull(result)
        transaction { assertEquals(miscellany.id.value, result!!.id.value) }
    }

    @Test
    fun `should return correct OreDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Ore", "2024-01-01")

        val ore = ProductRepository.newOre(
            product,
            weight = 6.1,
            metalType = "soft",
            smeltedInto = "iron ingot"
        )

        val result = ProductRepository.getOre(product.id.value)
        assertNotNull(result)
        assertEquals("soft", result!!.metalType)
    }

    @Test
    fun `should return correct PotionDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Potions", "2024-01-01")

        val potion = ProductRepository.newPotion(
            product,
            effects = "slowness",
            duration = 10,
            magnitude = "small",
            poisoned = "no"
        )

        val result = ProductRepository.getPotion(product.id.value)
        assertNotNull(result)
        assertEquals("slowness", result!!.effects)
    }

    @Test
    fun `should return correct SoulGemDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Soul Gems", "2024-01-01")

        val soulgem = ProductRepository.newSoulGem(
            product,
            soulSize = "large",
            isFilled = "yes",
            containedSoul = "skeleton",
            canCapture = "yes",
            reusable = "yes"
        )

        val result = ProductRepository.getSoulGem(product.id.value)
        assertNotNull(result)
        assertEquals("skeleton", result!!.containedSoul)
    }

    @Test
    fun `should return correct WeaponDAO`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Weapons", "2024-01-01")

        val weapon = ProductRepository.newWeapon(
            product,
            weight = 5.5,
            magical = "yes",
            craft = "nordic",
            damage = 10,
            speed = 9.5,
            reach = 15,
            stagger = 4.4,
            battleStyle = "soresu",
            category = "sword"
        )

        val result = ProductRepository.getWeapon(product.id.value)
        assertNotNull(result)
        assertEquals(10, result!!.damage)
    }

    @Test
    fun `should convert WeaponDAO to DTO`() = runBlocking {
        val product = ProductRepository.newProduct("Silver Dagger", 100, 10, "desc", "Weapons", "2024-01-01")

        val weapon = ProductRepository.newWeapon(
            product,
            weight = 2.0,
            magical = "no",
            craft = "scandinavian",
            damage = 6,
            speed = 15.5,
            reach = 5,
            stagger = 8.2,
            battleStyle = "ataru",
            category = "dagger"
        )

        val result = ProductRepository.convertDaoToProduct(weapon)
        assertNotNull(result)
        assertTrue(result is Weapon)
        assertEquals("Silver Dagger", (result as Weapon).productName)
    }

    @Test
    fun `should decrease stock correctly`() = runBlocking {
        val product = ProductRepository.newProduct("test", 100, 10, "desc", "Weapons", "2024-01-01")

        ProductRepository.alterStock(product, 3)
        assertEquals(7, product.stock)
    }


    // *************************
    // SALE FUNCTIONS TESTS
    // *************************

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
    fun `should get correct irl purchase`() = runBlocking {
        val employee = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "employee"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val date = "today"
        val sale = SaleRepository.newIrlPurchase(employee, date)
        val fetched = SaleRepository.getIrlPurchaseByAccount(employee)
        transaction {
            assertNotNull(fetched)
            assertEquals(sale.id.value, fetched?.id?.value)
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
    fun `should get a list with two sales`() = runBlocking {
        val sale1 = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 100
                this.totalQuantity = 2
                this.finished = true
                this.status = "test"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale2 = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 300
                this.totalQuantity = 4
                this.finished = true
                this.status = "test"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sales = SaleRepository.getSales()
        transaction {
            assertEquals(sales.size, 2)
            assertEquals(sales[0].id, sale1.id.value.toLong())
            assertEquals(sales[1].id, sale2.id.value.toLong())
        }
    }

    @Test
    fun `should get a list with two finished sales by this client`() = runBlocking {
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
        val sale1 = transaction {
            SaleDAO.new {
                this.idClient = account
                this.idEmployee = null
                this.totalPriceGold = 100
                this.totalQuantity = 2
                this.finished = true
                this.status = "test"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale2 = transaction {
            SaleDAO.new {
                this.idClient = account
                this.idEmployee = null
                this.totalPriceGold = 300
                this.totalQuantity = 4
                this.finished = true
                this.status = "test"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sales = SaleRepository.getFinishedSalesByClient(account.id.value)
        transaction {
            assertEquals(sales.size, 2)
            assertEquals(sales[0].id, sale1.id.value.toLong())
            assertEquals(sales[1].id, sale2.id.value.toLong())
        }
    }

    @Test
    fun `should get a list with two finished sales by this employee`() = runBlocking {
        val account = transaction {
            AccountDAO.new {
                this.username = "username"
                this.email = "email"
                this.password = "hashed"
                this.type = "employee"
                this.createdAt = "today"
                this.updatedAt = "today"
                this.lastRun = "today"
            }
        }
        val sale1 = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = account
                this.totalPriceGold = 100
                this.totalQuantity = 2
                this.finished = true
                this.status = "Delivered!"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale2 = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = account
                this.totalPriceGold = 300
                this.totalQuantity = 4
                this.finished = true
                this.status = "Delivered!"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sales = SaleRepository.getFinishedSalesByEmployee(account.id.value)
        transaction {
            assertEquals(sales.size, 2)
            assertEquals(sales[0].id, sale1.id.value.toLong())
            assertEquals(sales[1].id, sale2.id.value.toLong())
        }
    }

    @Test
    fun `should get a list with one available sale`() = runBlocking {
        val sale1 = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 100
                this.totalQuantity = 2
                this.finished = true
                this.status = "Delivered!"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sale2 = transaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = null
                this.totalPriceGold = 300
                this.totalQuantity = 4
                this.finished = true
                this.status = "Waiting for CarroçaBoy!"
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }
        val sales = SaleRepository.getAvailableSales()
        transaction {
            assertEquals(sales.size, 1)
            assertEquals(sales[0].id, sale2.id.value.toLong())
        }
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
    fun `should update sale with correct status and employee`() = runBlocking {
        val acc = AccountRepository.newAccount("carroca1", "carroca1@mail.com", "pass", "carrocaboy", "2024-01-01")
        val sale = transaction {
            SaleDAO.new {
                idClient = null
                idEmployee = null
                totalPriceGold = 0
                totalQuantity = 0
                finished = false
                status = "Waiting for CarroçaBoy!"
                address = "there"
                createdAt = "2024-01-01"
                updatedAt = "2024-01-01"
            }
        }
        SaleRepository.assignCarrocaBoy(sale, acc, "2024-02-01")
        transaction {
            assertEquals(acc.id.value, sale.idEmployee?.id?.value)
            assertEquals("To be delivered by carroca1", sale.status)
            assertEquals("2024-02-01", sale.updatedAt)
        }
    }

    @Test
    fun `should return sale assigned to account`() = runBlocking {
        val acc = AccountRepository.newAccount("cbuser", "cb@mail.com", "pass", "carrocaboy", "2024-01-01")
        val sale = transaction {
            SaleDAO.new {
                idClient = null
                idEmployee = acc
                totalPriceGold = 0
                totalQuantity = 0
                finished = false
                status = "To be delivered by cbuser"
                address = "there"
                createdAt = "2024-01-01"
                updatedAt = "2024-01-01"
            }
        }
        val found = SaleRepository.getSaleToBeDelivered(acc)
        assertNotNull(found)
        assertEquals(sale.id.value, found!!.id.value)
    }

    @Test
    fun `should find sale by id with correct status pattern`() = runBlocking {
        val acc = AccountRepository.newAccount("userX", "x@mail.com", "pass", "carrocaboy", "2024-01-01")
        val sale = transaction {
            SaleDAO.new {
                idEmployee = acc
                status = "To be delivered by userX"
                totalPriceGold = 0
                totalQuantity = 0
                finished = false
                address = "there"
                createdAt = "2024-01-01"
                updatedAt = "2024-01-01"
            }
        }
        val found = SaleRepository.getSaleToBeDeliveredById(sale.id.value)
        assertNotNull(found)
        assertEquals(sale.id.value, found!!.id.value)
    }

    @Test
    fun `should return sale only if status is correct`() = runBlocking {
        val sale = transaction {
            SaleDAO.new {
                idEmployee = null
                status = "Waiting for CarroçaBoy!"
                totalPriceGold = 0
                totalQuantity = 0
                finished = false
                address = "there"
                createdAt = "2024-01-01"
                updatedAt = "2024-01-01"
            }
        }
        val found = SaleRepository.getAvailableSaleById(sale.id.value)
        assertNotNull(found)
        assertEquals(sale.id.value, found!!.id.value)
    }

    @Test
    fun `should remove IRL sale`() = runBlocking {
        val acc = AccountRepository.newAccount("emp1", "emp1@mail.com", "pass", "employee", "2024-01-01")
        val sale = transaction {
            SaleDAO.new {
                idEmployee = acc
                status = "Ongoing IRL purchase"
                totalPriceGold = 0
                totalQuantity = 0
                finished = false
                address = "Locally"
                createdAt = "2024-01-01"
                updatedAt = "2024-01-01"
            }
        }
        SaleRepository.deleteIrlPurchase(sale)
        val check = SaleRepository.getSaleById(sale.id.value)
        assertNull(check)
    }

    @Test
    fun `should update sale address and timestamp`() = runBlocking {
        val acc = AccountRepository.newAccount("cli1", "cli1@mail.com", "pass", "client", "2024-01-01")
        val sale = transaction {
            SaleDAO.new {
                idClient = acc
                address = "Old Address"
                totalPriceGold = 0
                totalQuantity = 0
                finished = false
                status = "Cart"
                createdAt = "2024-01-01"
                updatedAt = "2024-01-01"
            }
        }
        SaleRepository.alterAddress(sale, "New Address", "2024-02-01")
        assertEquals("New Address", sale.address)
        assertEquals("2024-02-01", sale.updatedAt)
    }


    // *************************
    // SALE PRODUCT FUNCTIONS TESTS
    // *************************

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
                this.address = "there"
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
                this.address = "there"
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
                this.address = "there"
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
                this.address = "there"
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
                this.address = "there"
                this.createdAt = "today"
                this.updatedAt = "today"
            }
        }

        val sp = SaleProductRepository.newSaleProduct(sale, product)
        SaleProductRepository.deleteSaleProduct(sp)

        val result = SaleProductRepository.getSaleProduct(sale.id.value, product.id.value)

        assertNull(result)
    }
}
