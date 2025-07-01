package com.mac350.tables

import com.mac350.models.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object WeaponT : IntIdTable("Weapons") {
    val product = reference("product_id", ProductT, onDelete = ReferenceOption.CASCADE)
    val weight = double("weight")
    val magical = bool("magical")
    val craft = varchar("craft", 255)
    val damage = long("damage")
    val speed = double("speed")
    val reach = long("reach")
    val stagger = double("stagger")
    val battleStyle = varchar("battleStyle", 255)
    val category = varchar("category", 255)
}

class WeaponDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WeaponDAO>(WeaponT)

    var product by ProductDAO referencedOn WeaponT.product
    var weight by WeaponT.weight
    var magical by WeaponT.magical
    var craft by WeaponT.craft
    var damage by WeaponT.damage
    var speed by WeaponT.speed
    var reach by WeaponT.reach
    var stagger by WeaponT.stagger
    var battleStyle by WeaponT.battleStyle
    var category by WeaponT.category
}

fun daoToWeapon(dao: WeaponDAO): Weapon = Weapon(
    id = dao.product.id.value.toLong(),
    productName = dao.product.productName,
    image = dao.product.image?.let { "http://localhost:8080$it" },
    priceGold = dao.product.priceGold,
    stock = dao.product.stock,
    description = dao.product.description,
    type = dao.product.type,
    createdAt = dao.product.createdAt,
    updatedAt = dao.product.updatedAt,
    standardDiscount = dao.product.standardDiscount,
    specialDiscount = dao.product.specialDiscount,
    hasDiscount = dao.product.hasDiscount,
    weight = dao.weight,
    magical = dao.magical,
    craft = dao.craft,
    damage = dao.damage,
    speed = dao.speed,
    reach = dao.reach,
    stagger = dao.stagger,
    battleStyle = dao.battleStyle,
    category = dao.category
)
