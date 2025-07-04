package com.mac350.repositories

import com.mac350.tables.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

class UtilRepository {
    companion object {
        fun capitalizeFirstLetter(input: String): String {
            if(input.isEmpty()) return input

            return input.replaceFirstChar {
                if(it.isLowerCase()) it.uppercaseChar() else it
            }
        }

        suspend fun parseMultiPart(multipart : MultiPartData): Pair<Map<String, String>, Map<String, ByteArray>> {
            var fields = mutableMapOf<String, String>()
            var files = mutableMapOf<String, ByteArray>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name != null) {
                            fields[part.name!!] = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.name != null) {
                            files[part.name!!] = part.provider().toByteArray()
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            return fields to files
        }

        fun getLabelsAndTypes(vararg tables: Table): List<Pair<String, String>> {
            val ignoredColumns = setOf("id", "createdAt", "updatedAt", "product_id", "type", "image", "account_id", "lastRun", "totalCommissions")
            return tables
                .flatMap { it.columns }
                .filter { it.name !in ignoredColumns }
                .map { it.name to it.columnType.sqlType() }
                .distinctBy { it.first }
        }

        fun getTableName(name: String): Table? = when(name) {
            "admins" -> AdminT
            "ammunition" -> AmmunitionT
            "armor" -> ArmorT
            "books" -> BookT
            "cashiers" -> CashierT
            "carrocaboys" -> CarrocaBoyT
            "clothing" -> ClothingT
            "food" -> FoodT
            "ingredients" -> IngredientT
            "miscellaneous" -> MiscellanyT
            "ores" -> OreT
            "potions" -> PotionT
            "soul gems" -> SoulGemT
            "weapons" -> WeaponT
            else -> null
        }

        fun createTables() {
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
        }
    }
}
