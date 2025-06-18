package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Weapon (
    override val id: Long,
    override var productName: String,
    override var image: String?,
    override var priceGold: Long,
    override var stock: Long,
    override var description: String,
    override var type: String,
    override val createdAt: String,
    override var updatedAt: String,
    override var standardDiscount: Long,
    override var specialDiscount: Long,
    override var hasDiscount: Boolean,
    var weight: Double,
    var magical: String,
    var craft: String,
    var damage: Long,
    var speed: Double,
    var reach: Long,
    var stagger: Double,
    var battleStyle: String,
    var category: String
) : Product() {

}

@Serializable
data class WeaponFilter (
    override val page: Long,
    override val productsPerPage: Long,
    override val productName: String?,
    override val minPriceGold: Long?,
    override val maxPriceGold: Long?,
    override val type: String?,
    override val hasDiscount: Boolean?,
    val minWeight: Double?,
    val maxWeight: Double?,
    val magical: String?,
    val craft: String?,
    val minDamage: Long?,
    val maxDamage: Long?,
    val minSpeed: Double?,
    val maxSpeed: Double?,
    val minReach: Long?,
    val maxReach: Long?,
    val minStagger: Double?,
    val maxStagger: Double?,
    val battleStyle: String?,
    val category: String?
) : GeneralFilter()