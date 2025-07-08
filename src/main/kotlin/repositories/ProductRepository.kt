package com.mac350.repositories

import com.mac350.models.*
import com.mac350.plugins.suspendTransaction
import com.mac350.tables.*
import org.h2.command.query.QueryOrderBy
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import java.lang.invoke.StringConcatException

class ProductRepository {
    companion object {
        val validTypes = setOf("Ammunition", "Armor", "Books",
            "Clothing", "Food", "Ingredients", "Miscellaneous",
            "Ores", "Potions", "Soul gems", "Weapons")

        val reqFields = mapOf(
            "Ammunition" to listOf("productName", "priceGold", "stock", "description", "magical", "craft", "speed", "gravity", "category"),
            "Armor" to listOf("productName", "priceGold", "stock", "description", "weight", "magical", "craft", "protection", "heavy", "category"),
            "Books" to listOf("productName", "priceGold", "stock", "description", "skillTaught", "magical", "pages"),
            "Clothing" to listOf("productName", "priceGold", "stock", "description", "protection", "slot", "enchantment", "enchanted", "weight"),
            "Food" to listOf("productName", "priceGold", "stock", "description", "weight", "healthRestored", "staminaRestored", "magickaRestored", "duration"),
            "Ingredients" to listOf("productName", "priceGold", "stock", "description", "weight", "magical", "effects"),
            "Miscellaneous" to listOf("productName", "priceGold", "stock", "description", "questItem", "craftingUse", "modelType"),
            "Ores" to listOf("productName", "priceGold", "stock", "description", "weight", "metalType", "smeltedInto"),
            "Potions" to listOf("productName", "priceGold", "stock", "description", "effects", "duration", "magnitude", "poisoned"),
            "Soul gems" to listOf("productName", "priceGold", "stock", "description", "soulSize", "isFilled", "containedSoul", "canCapture", "reusable"),
            "Weapons" to listOf("productName", "priceGold", "stock", "description", "weight", "magical", "craft", "damage", "speed", "reach", "stagger", "battleStyle", "category")
        )

        suspend fun getProducts(
            type: String,
            filterName: String?,
            minPriceGold: Long?,
            maxPriceGold: Long?,
            orderBy: String?
        ) = suspendTransaction {
            val validTypes = setOf(
                "Ammunition", "Armor", "Books", "Clothing", "Food", "Ingredients",
                "Miscellaneous", "Ores", "Potions", "Soul gems", "Weapons"
            )

            if (type != "All products" && type !in validTypes) return@suspendTransaction null

            val query = ProductDAO.find {
                Op.build {
                    val conditions = mutableListOf<Op<Boolean>>()

                    if (type != "All products") {
                        conditions += (ProductT.type eq type)
                    }

                    if (!filterName.isNullOrBlank()) {
                        conditions += (ProductT.productName.lowerCase() like "%${filterName.lowercase()}%")
                    }

                    if (minPriceGold != null) {
                        conditions += (ProductT.priceGold greaterEq minPriceGold)
                    }

                    if (maxPriceGold != null) {
                        conditions += (ProductT.priceGold lessEq maxPriceGold)
                    }

                    conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
                }
            }

            val ordered = when (orderBy) {
                "PriceAsc" -> query.orderBy(ProductT.priceGold to SortOrder.ASC)
                "PriceDesc" -> query.orderBy(ProductT.priceGold to SortOrder.DESC)
                "NameAsc" -> query.orderBy(ProductT.productName to SortOrder.ASC)
                "NameDesc" -> query.orderBy(ProductT.productName to SortOrder.DESC)
                else -> query
            }

            ordered.map(::daoToCard)
        }


        suspend fun getProductById(productId: Int) : ProductDAO? = suspendTransaction {
            ProductDAO.find { ProductT.id eq productId }.firstOrNull()
        }

        suspend fun newProduct(productName: String, priceGold: Long, stock: Long, description: String, type: String, date: String): ProductDAO = suspendTransaction {
            ProductDAO.new {
                this.productName = productName
                this.image = null
                this.priceGold = priceGold
                this.stock = stock
                this.description = description
                this.type = type
                this.createdAt = date
                this.updatedAt = date
            }
        }

        suspend fun editProduct(productDAO: ProductDAO, productName: String, priceGold: Long, stock: Long, description: String, date: String) = suspendTransaction {
            productDAO.productName = productName
            productDAO.priceGold = priceGold
            productDAO.stock = stock
            productDAO.description = description
            productDAO.updatedAt = date
        }

        suspend fun newAmmunition(product: ProductDAO, magical: String, craft: String, speed: Double, gravity: Double, category: String): AmmunitionDAO = suspendTransaction {
            AmmunitionDAO.new {
                this.product = product
                this.magical = magical
                this.craft = craft
                this.speed = speed
                this.gravity = gravity
                this.category = category
            }
        }

        suspend fun editAmmunition(ammunitionDAO: AmmunitionDAO, magical: String, craft: String, speed: Double, gravity: Double, category: String) = suspendTransaction {
            ammunitionDAO.magical = magical
            ammunitionDAO.craft = craft
            ammunitionDAO.speed = speed
            ammunitionDAO.gravity = gravity
            ammunitionDAO.category = category
        }

        suspend fun newArmor(product: ProductDAO, weight: Double, magical: String, craft: String, protection: Double, heavy: String, category: String): ArmorDAO = suspendTransaction {
            ArmorDAO.new {
                this.product = product
                this.weight = weight
                this.magical = magical
                this.craft = craft
                this.protection = protection
                this.heavy = heavy
                this.category = category
            }
        }

        suspend fun editArmor(armorDAO: ArmorDAO, weight: Double, magical: String, craft: String, protection: Double, heavy: String, category: String) = suspendTransaction {
            armorDAO.weight = weight
            armorDAO.magical = magical
            armorDAO.craft = craft
            armorDAO.protection = protection
            armorDAO.heavy = heavy
            armorDAO.category = category
        }

        suspend fun newBook(product: ProductDAO, skillTaught: String, magical: String, pages: Long): BookDAO = suspendTransaction {
            BookDAO.new {
                this.product = product
                this.skillTaught = skillTaught
                this.magical = magical
                this.pages = pages
            }
        }


        suspend fun editBook(bookDAO: BookDAO, skillTaught: String, magical: String, pages: Long) = suspendTransaction {
            bookDAO.skillTaught = skillTaught
            bookDAO.magical = magical
            bookDAO.pages = pages
        }

        suspend fun newClothing(product: ProductDAO, protection: Long, slot: String, enchantment: String, enchanted: String, weight: Double): ClothingDAO = suspendTransaction {
            ClothingDAO.new {
                this.product = product
                this.protection = protection
                this.slot = slot
                this.enchantment = enchantment
                this.enchanted = enchanted
                this.weight = weight
            }
        }

        suspend fun editClothing(clothingDAO: ClothingDAO, protection: Long, slot: String, enchantment: String, enchanted: String, weight: Double) = suspendTransaction {
            clothingDAO.protection = protection
            clothingDAO.slot = slot
            clothingDAO.enchantment = enchantment
            clothingDAO.enchanted = enchanted
            clothingDAO.weight = weight
        }

        suspend fun newFood(product: ProductDAO, weight: Double, healthRestored: Long, staminaRestored: Long, magickaRestored: Long, duration: Long): FoodDAO = suspendTransaction {
            FoodDAO.new {
                this.product = product
                this.weight = weight
                this.healthRestored = healthRestored
                this.staminaRestored = staminaRestored
                this.magickaRestored = magickaRestored
                this.duration = duration
            }
        }

        suspend fun editFood(foodDAO: FoodDAO, weight: Double, healthRestored: Long, staminaRestored: Long, magickaRestored: Long, duration: Long) = suspendTransaction {
            foodDAO.weight = weight
            foodDAO.healthRestored = healthRestored
            foodDAO.staminaRestored = staminaRestored
            foodDAO.magickaRestored = magickaRestored
            foodDAO.duration = duration
        }

        suspend fun newIngredient(product: ProductDAO, weight: Double, magical: String, effects: String): IngredientDAO = suspendTransaction {
            IngredientDAO.new {
                this.product = product
                this.weight = weight
                this.magical = magical
                this.effects = effects
            }
        }

        suspend fun editIngredient(ingredientDAO: IngredientDAO, weight: Double, magical: String, effects: String) = suspendTransaction {
            ingredientDAO.weight = weight
            ingredientDAO.magical = magical
            ingredientDAO.effects = effects
        }

        suspend fun newMiscellany(product: ProductDAO, questItem: String, craftingUse: String, modelType: String): MiscellanyDAO = suspendTransaction {
            MiscellanyDAO.new {
                this.product = product
                this.questItem = questItem
                this.craftingUse = craftingUse
                this.modelType = modelType
            }
        }

        suspend fun editMiscellany(miscellanyDAO: MiscellanyDAO, questItem: String, craftingUse: String, modelType: String) = suspendTransaction {
            miscellanyDAO.questItem = questItem
            miscellanyDAO.craftingUse = craftingUse
            miscellanyDAO.modelType = modelType
        }

        suspend fun newOre(product: ProductDAO, weight: Double, metalType: String, smeltedInto: String): OreDAO = suspendTransaction {
            OreDAO.new {
                this.product = product
                this.weight = weight
                this.metalType = metalType
                this.smeltedInto = smeltedInto
            }
        }

        suspend fun editOre(oreDAO: OreDAO, weight: Double, metalType: String, smeltedInto: String) = suspendTransaction {
            oreDAO.weight = weight
            oreDAO.metalType = metalType
            oreDAO.smeltedInto = smeltedInto
        }

        suspend fun newPotion(product: ProductDAO, effects: String, duration: Long, magnitude: String, poisoned: String): PotionDAO = suspendTransaction {
            PotionDAO.new {
                this.product = product
                this.effects = effects
                this.duration = duration
                this.magnitude = magnitude
                this.poisoned = poisoned
            }
        }

        suspend fun editPotion(potionDAO: PotionDAO, effects: String, duration: Long, magnitude: String, poisoned: String) = suspendTransaction {
            potionDAO.effects = effects
            potionDAO.duration = duration
            potionDAO.magnitude = magnitude
            potionDAO.poisoned = poisoned
        }

        suspend fun newSoulGem(product: ProductDAO, soulSize: String, isFilled: String, containedSoul: String, canCapture: String, reusable: String): SoulGemDAO = suspendTransaction {
            SoulGemDAO.new {
                this.product = product
                this.soulSize = soulSize
                this.isFilled = isFilled
                this.containedSoul = containedSoul
                this.canCapture = canCapture
                this.reusable = reusable
            }
        }

        suspend fun editSoulGem(soulGemDAO: SoulGemDAO, soulSize: String, isFilled: String, containedSoul: String, canCapture: String, reusable: String) = suspendTransaction {
            soulGemDAO.soulSize = soulSize
            soulGemDAO.isFilled = isFilled
            soulGemDAO.containedSoul = containedSoul
            soulGemDAO.canCapture = canCapture
            soulGemDAO.reusable = reusable
        }

        suspend fun newWeapon(product: ProductDAO, weight: Double, magical: String, craft: String, damage: Long, speed: Double, reach: Long, stagger: Double, battleStyle: String, category: String): WeaponDAO = suspendTransaction {
            WeaponDAO.new {
                this.product = product
                this.weight = weight
                this.magical = magical
                this.craft = craft
                this.damage = damage
                this.speed = speed
                this.reach = reach
                this.stagger = stagger
                this.battleStyle = battleStyle
                this.category = category
            }
        }

        suspend fun editWeapon(weaponDAO: WeaponDAO, weight: Double, magical: String, craft: String, damage: Long, speed: Double, reach: Long, stagger: Double, battleStyle: String, category: String) = suspendTransaction {
            weaponDAO.weight = weight
            weaponDAO.magical = magical
            weaponDAO.craft = craft
            weaponDAO.damage = damage
            weaponDAO.speed = speed
            weaponDAO.reach = reach
            weaponDAO.stagger = stagger
            weaponDAO.battleStyle = battleStyle
            weaponDAO.category = category
        }

        suspend fun getAmmunition(productId: Int): AmmunitionDAO? = suspendTransaction {
            AmmunitionDAO.find{ AmmunitionT.product eq productId }.firstOrNull()
        }

        suspend fun getArmor(productId: Int): ArmorDAO? = suspendTransaction {
            ArmorDAO.find{ ArmorT.product eq productId }.firstOrNull()
        }

        suspend fun getBook(productId: Int): BookDAO? = suspendTransaction {
            BookDAO.find{ BookT.product eq productId }.firstOrNull()
        }

        suspend fun getClothing(productId: Int): ClothingDAO? = suspendTransaction {
            ClothingDAO.find{ ClothingT.product eq productId }.firstOrNull()
        }

        suspend fun getFood(productId: Int): FoodDAO? = suspendTransaction {
            FoodDAO.find{ FoodT.product eq productId }.firstOrNull()
        }

        suspend fun getIngredient(productId: Int): IngredientDAO? = suspendTransaction {
            IngredientDAO.find{ IngredientT.product eq productId }.firstOrNull()
        }

        suspend fun getMiscellany(productId: Int): MiscellanyDAO? = suspendTransaction {
            MiscellanyDAO.find{ MiscellanyT.product eq productId }.firstOrNull()
        }

        suspend fun getOre(productId: Int): OreDAO? = suspendTransaction {
            OreDAO.find{ OreT.product eq productId }.firstOrNull()
        }

        suspend fun getPotion(productId: Int): PotionDAO? = suspendTransaction {
            PotionDAO.find{ PotionT.product eq productId }.firstOrNull()
        }

        suspend fun getSoulGem(productId: Int): SoulGemDAO? = suspendTransaction {
            SoulGemDAO.find{ SoulGemT.product eq productId }.firstOrNull()
        }

        suspend fun getWeapon(productId: Int): WeaponDAO? = suspendTransaction {
            WeaponDAO.find{ WeaponT.product eq productId }.firstOrNull()
        }

        suspend fun convertDaoToProduct(product: Any) = suspendTransaction {
            when(product) {
                is AmmunitionDAO -> daoToAmmunition(product)
                is ArmorDAO -> daoToArmor(product)
                is BookDAO -> daoToBook(product)
                is ClothingDAO -> daoToClothing(product)
                is FoodDAO -> daoToFood(product)
                is IngredientDAO -> daoToIngredient(product)
                is MiscellanyDAO -> daoToMiscellany(product)
                is OreDAO -> daoToOre(product)
                is PotionDAO -> daoToPotion(product)
                is SoulGemDAO -> daoToSoulGem(product)
                is WeaponDAO -> daoToWeapon(product)
                else -> null
            }
        }

        suspend fun alterStock(product: ProductDAO, subtract: Long) = suspendTransaction {
            product.stock -= subtract
        }
    }
}