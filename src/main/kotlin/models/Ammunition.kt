package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Ammunition (
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
    var magical: Boolean,
    var craft: String,
    var speed: Double,
    var gravity: Double,
    var category: String
) : Product() {

}

@Serializable
data class AmmunitionFilter (
    override val page: Long,
    override val productsPerPage: Long,
    override val productName: String?,
    override val minPriceGold: Long?,
    override val maxPriceGold: Long?,
    override val type: String?,
    override val hasDiscount: Boolean?,
    val magical: Boolean?,
    val craft: String?,
    val minSpeed: Double?,
    val maxSpeed: Double?,
    val minGravity: Double?,
    val maxGravity: Double?,
    val category: String?
) : GeneralFilter()