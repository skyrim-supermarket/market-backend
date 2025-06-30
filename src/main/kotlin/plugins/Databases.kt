package com.mac350.plugins

import com.mac350.repositories.AccountRepository
import com.mac350.tables.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.*
import org.jetbrains.exposed.sql.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.Date

object Databases {

    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/test"
            driverClassName = "org.postgresql.Driver"
            username = "postgres"
            password = "root"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(AccountT)
            SchemaUtils.create(AdminT)
            SchemaUtils.create(CarrocaBoyT)
            SchemaUtils.create(CashierT)
            SchemaUtils.create(ClientT)
            SchemaUtils.create(ProductT)
            SchemaUtils.create(SaleProductT)
            SchemaUtils.create(SaleT)
            SchemaUtils.create(AmmunitionT)
            SchemaUtils.create(ArmorT)
            SchemaUtils.create(BookT)
            SchemaUtils.create(ClothingT)
            SchemaUtils.create(FoodT)
            SchemaUtils.create(IngredientT)
            SchemaUtils.create(MiscellanyT)
            SchemaUtils.create(OreT)
            SchemaUtils.create(PotionT)
            SchemaUtils.create(SoulGemT)
            SchemaUtils.create(WeaponT)

        }

        /*transaction {
            val account = AccountDAO.new {
                val date = Date(System.currentTimeMillis()).toString()
                this.username = "admin1"
                this.email = "admin@admin.br"
                this.password = AccountRepository.hashPw("123")
                this.type = "admin"
                this.createdAt = date
                this.updatedAt = date
                this.lastRun = date
            }

            AdminDAO.new {
                this.account = account
                //this.root = true
            }
        }*/
    }
}