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
            "Ammunition" to listOf("productName", "priceGold", "stock", "description", "magical", "craft", "speed", "gravity", "category"),
            "Armor" to listOf("productName", "priceGold", "stock", "description", "weight", "magical", "craft", "protection", "heavy", "category"),
            "Books" to listOf("productName", "priceGold", "stock", "description"),
            "Clothing" to listOf("productName", "priceGold", "stock", "description"),
            "Food" to listOf("productName", "priceGold", "stock", "description"),
            "Ingredients" to listOf("productName", "priceGold", "stock", "description"),
            "Miscellaneous" to listOf("productName", "priceGold", "stock", "description"),
            "Ores" to listOf("productName", "priceGold", "stock", "description"),
            "Potions" to listOf("productName", "priceGold", "stock", "description"),
            "Soul gems" to listOf("productName", "priceGold", "stock", "description"),
            "Weapons" to listOf("productName", "priceGold", "stock", "description", "weight", "magical", "craft", "damage", "speed", "reach", "stagger", "battleStyle", "category")
        )

        suspend fun getProducts(type: String) = suspendTransaction {
            if(type=="All products") {
                ProductDAO.all().map(::daoToCard)
            } else if(
                type=="Ammunition" ||
                type=="Armor" ||
                type=="Books" ||
                type=="Clothing" ||
                type=="Food" ||
                type=="Ingredients" ||
                type=="Miscellaneous" ||
                type=="Ores" ||
                type=="Potions" ||
                type=="Soul gems" ||
                type=="Weapons"
            ) {
                ProductDAO.find { ProductT.type eq type }.map(::daoToCard)
            } else null
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

        suspend fun editAmmunition(ammunitionDAO: AmmunitionDAO, magical: Boolean, craft: String, speed: Double, gravity: Double, category: String) = suspendTransaction {
            ammunitionDAO.magical = magical
            ammunitionDAO.craft = craft
            ammunitionDAO.speed = speed
            ammunitionDAO.gravity = gravity
            ammunitionDAO.category = category
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

        suspend fun editArmor(armorDAO: ArmorDAO, weight: Double, magical: Boolean, craft: String, protection: Double, heavy: Boolean, category: String) = suspendTransaction {
            armorDAO.weight = weight
            armorDAO.magical = magical
            armorDAO.craft = craft
            armorDAO.protection = protection
            armorDAO.heavy = heavy
            armorDAO.category = category
        }

        suspend fun newBook(product: ProductDAO): BookDAO = suspendTransaction {
            BookDAO.new {
                this.product = product
            }
        }


        suspend fun editBook(bookDAO: BookDAO) = suspendTransaction {

        }

        suspend fun newClothing(product: ProductDAO): ClothingDAO = suspendTransaction {
            ClothingDAO.new {
                this.product = product
            }
        }

        suspend fun editClothing(clothingDAO: ClothingDAO) = suspendTransaction {

        }

        suspend fun newFood(product: ProductDAO): FoodDAO = suspendTransaction {
            FoodDAO.new {
                this.product = product
            }
        }

        suspend fun editFood(foodDAO: FoodDAO) = suspendTransaction {

        }

        suspend fun newIngredient(product: ProductDAO): IngredientDAO = suspendTransaction {
            IngredientDAO.new {
                this.product = product
            }
        }

        suspend fun editIngredient(ingredientDAO: IngredientDAO) = suspendTransaction {

        }

        suspend fun newMiscellany(product: ProductDAO): MiscellanyDAO = suspendTransaction {
            MiscellanyDAO.new {
                this.product = product
            }
        }

        suspend fun editMiscellany(miscellanyDAO: MiscellanyDAO) = suspendTransaction {

        }

        suspend fun newOre(product: ProductDAO): OreDAO = suspendTransaction {
            OreDAO.new {
                this.product = product
            }
        }

        suspend fun editOre(oreDAO: OreDAO) = suspendTransaction {

        }

        suspend fun newPotion(product: ProductDAO): PotionDAO = suspendTransaction {
            PotionDAO.new {
                this.product = product
            }
        }

        suspend fun editPotion(potionDAO: PotionDAO) = suspendTransaction {

        }

        suspend fun newSoulGem(product: ProductDAO): SoulGemDAO = suspendTransaction {
            SoulGemDAO.new {
                this.product = product
            }
        }

        suspend fun editSoulGem(soulGemDAO: SoulGemDAO) = suspendTransaction {

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

        suspend fun editWeapon(weaponDAO: WeaponDAO, weight: Double, magical: Boolean, craft: String, damage: Long, speed: Double, reach: Long, stagger: Double, battleStyle: String, category: String) = suspendTransaction {
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