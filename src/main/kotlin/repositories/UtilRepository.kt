package com.mac350.repositories

import com.mac350.tables.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import org.jetbrains.exposed.sql.Table

class UtilRepository {
    companion object {
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
            val ignoredColumns = setOf("id", "createdAt", "updatedAt", "hasDiscount", "product_id", "type", "image")
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
    }
}
