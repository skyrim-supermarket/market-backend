package com.mac350.repositories

import com.mac350.models.ProductCardInfo
import com.mac350.plugins.suspendTransaction
import com.mac350.tables.*

class ProductRepository {
    companion object {
        val validTypes = setOf("Ammunition", "Armor", "Books",
            "Clothing", "Food", "Ingredients", "Miscellaneous",
            "Ores", "Potions", "Soul gems", "Weapons")

        val reqFields = mapOf(
            "Ammunition" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount", "magical", "craft", "speed", "gravity", "category"),
            "Armor" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount", "weight", "magical", "craft", "protection", "heavy", "category"),
            "Books" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Clothing" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Food" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Ingredients" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Miscellaneous" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Ores" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Potions" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Soul gems" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount"),
            "Weapon" to listOf("productName", "priceGold", "description", "standardDiscount", "specialDiscount", "weight", "magical", "craft", "damage", "speed", "reach", "stagger", "battleStyle", "category")
        )

        suspend fun getProducts() = suspendTransaction {

        }

        suspend fun getProductById(productId: Int) : ProductDAO? = suspendTransaction {
            ProductDAO.find { ProductT.id eq productId }.firstOrNull()
        }

        suspend fun newProduct(productName: String, priceGold: Long, description: String, type: String, standardDiscount: Long, specialDiscount: Long, date: String): ProductDAO = suspendTransaction {
            ProductDAO.new {
                this.productName = productName
                this.image = null
                this.priceGold = priceGold
                this.stock = 0
                this.description = description
                this.type = type
                this.createdAt = date
                this.updatedAt = date
                this.standardDiscount = standardDiscount
                this.specialDiscount = specialDiscount
                this.hasDiscount = false
            }
        }

        suspend fun newAmmunition(product: ProductDAO, magical: Boolean, craft: String, speed: Double, gravity: Double, category: String): AmmunitionDAO = suspendTransaction {
            AmmunitionDAO.new {
                this.product = product
                this.magical = magical
                this.craft = craft
                this.speed = speed
                this.gravity = gravity
                this.category = category
            }
        }

        suspend fun newArmor(product: ProductDAO, weight: Double, magical: Boolean, craft: String, protection: Double, heavy: Boolean, category: String): ArmorDAO = suspendTransaction {
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

        suspend fun newBook(product: ProductDAO): BookDAO = suspendTransaction {
            BookDAO.new {
                this.product = product
            }
        }

        suspend fun newClothing(product: ProductDAO): ClothingDAO = suspendTransaction {
            ClothingDAO.new {
                this.product = product
            }
        }

        suspend fun newFood(product: ProductDAO): FoodDAO = suspendTransaction {
            FoodDAO.new {
                this.product = product
            }
        }

        suspend fun newIngredient(product: ProductDAO): IngredientDAO = suspendTransaction {
            IngredientDAO.new {
                this.product = product
            }
        }

        suspend fun newMiscellany(product: ProductDAO): MiscellanyDAO = suspendTransaction {
            MiscellanyDAO.new {
                this.product = product
            }
        }

        suspend fun newOre(product: ProductDAO): OreDAO = suspendTransaction {
            OreDAO.new {
                this.product = product
            }
        }

        suspend fun newPotion(product: ProductDAO): PotionDAO = suspendTransaction {
            PotionDAO.new {
                this.product = product
            }
        }

        suspend fun newSoulGem(product: ProductDAO): SoulGemDAO = suspendTransaction {
            SoulGemDAO.new {
                this.product = product
            }
        }

        suspend fun newWeapon(product: ProductDAO, weight: Double, magical: Boolean, craft: String, damage: Long, speed: Double, reach: Long, stagger: Double, battleStyle: String, category: String): WeaponDAO = suspendTransaction {
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

        suspend fun alterStock(product: ProductDAO, subtract: Long) = suspendTransaction {
            product.stock -= subtract
        }
    }
}