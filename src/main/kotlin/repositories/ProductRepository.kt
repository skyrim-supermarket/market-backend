package com.mac350.repositories

import com.mac350.models.*
import com.mac350.plugins.suspendTransaction
import com.mac350.tables.*

class ProductRepository {
    companion object {
        val validTypes = setOf("Ammunition", "Armor", "Books",
            "Clothing", "Food", "Ingredients", "Miscellaneous",
            "Ores", "Potions", "Soul gems", "Weapons")

        val reqFields = mapOf(
            "Ammunition" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount", "magical", "craft", "speed", "gravity", "category"),
            "Armor" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount", "weight", "magical", "craft", "protection", "heavy", "category"),
            "Books" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Clothing" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Food" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Ingredients" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Miscellaneous" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Ores" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Potions" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Soul gems" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount"),
            "Weapons" to listOf("productName", "priceGold", "stock", "description", "standardDiscount", "specialDiscount", "weight", "magical", "craft", "damage", "speed", "reach", "stagger", "battleStyle", "category")
        )

        suspend fun getProducts() = suspendTransaction {

        }

        suspend fun getProductById(productId: Int) : ProductDAO? = suspendTransaction {
            ProductDAO.find { ProductT.id eq productId }.firstOrNull()
        }

        suspend fun newProduct(productName: String, priceGold: Long, stock: Long, description: String, type: String, standardDiscount: Long, specialDiscount: Long, date: String): ProductDAO = suspendTransaction {
            ProductDAO.new {
                this.productName = productName
                this.image = null
                this.priceGold = priceGold
                this.stock = stock
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

        suspend fun getAmmunition(productId: Int): Ammunition? = suspendTransaction {
            val product = AmmunitionDAO.find{ AmmunitionT.product eq productId }.firstOrNull()

            if(product != null) daoToAmmunition(product)
            else null
        }

        suspend fun getArmor(productId: Int): Armor? = suspendTransaction {
            val product = ArmorDAO.find{ ArmorT.product eq productId }.firstOrNull()

            if(product != null) daoToArmor(product)
            else null
        }

        suspend fun getBook(productId: Int): Book? = suspendTransaction {
            val product = BookDAO.find{ BookT.product eq productId }.firstOrNull()

            if(product != null) daoToBook(product)
            else null
        }

        suspend fun getClothing(productId: Int): Clothing? = suspendTransaction {
            val product = ClothingDAO.find{ ClothingT.product eq productId }.firstOrNull()

            if(product != null) daoToClothing(product)
            else null
        }

        suspend fun getFood(productId: Int): Food? = suspendTransaction {
            val product = FoodDAO.find{ FoodT.product eq productId }.firstOrNull()

            if(product != null) daoToFood(product)
            else null
        }

        suspend fun getIngredient(productId: Int): Ingredient? = suspendTransaction {
            val product = IngredientDAO.find{ IngredientT.product eq productId }.firstOrNull()

            if(product != null) daoToIngredient(product)
            else null
        }

        suspend fun getMiscellany(productId: Int): Miscellany? = suspendTransaction {
            val product = MiscellanyDAO.find{ MiscellanyT.product eq productId }.firstOrNull()

            if(product != null) daoToMiscellany(product)
            else null
        }

        suspend fun getOre(productId: Int): Ore? = suspendTransaction {
            val product = OreDAO.find{ OreT.product eq productId }.firstOrNull()

            if(product != null) daoToOre(product)
            else null
        }

        suspend fun getPotion(productId: Int): Potion? = suspendTransaction {
            val product = PotionDAO.find{ PotionT.product eq productId }.firstOrNull()

            if(product != null) daoToPotion(product)
            else null
        }

        suspend fun getSoulGem(productId: Int): SoulGem? = suspendTransaction {
            val product = SoulGemDAO.find{ SoulGemT.product eq productId }.firstOrNull()

            if(product != null) daoToSoulGem(product)
            else null
        }

        suspend fun getWeapon(productId: Int): Weapon? = suspendTransaction {
            val product = WeaponDAO.find{ WeaponT.product eq productId }.firstOrNull()

            if(product != null) daoToWeapon(product)
            else null
        }

        suspend fun alterStock(product: ProductDAO, subtract: Long) = suspendTransaction {
            product.stock -= subtract
        }
    }
}