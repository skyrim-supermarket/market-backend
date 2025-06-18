package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Armor (
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
    var protection: Double,
    var heavy: Boolean,
    var category: String
) : Product() {

}

@Serializable
data class ArmorFilter (
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
    val minProtection: Double?,
    val maxProtection: Double?,
    val heavy: Boolean?,
    val category: String?
) : GeneralFilter()