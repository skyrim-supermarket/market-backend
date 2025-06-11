package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Ammunition (
    override val id: Long,
    override var productName: String,
    override var image: String,
    override var priceGold: Long,
    override var stock: Long,
    override var description: String,
    override val createdAt: String,
    override var updatedAt: String,
    override var standardDiscount: Long,
    override var specialDiscount: Long,
    override var hasDiscount: Boolean,
    var magical: String,
    var craft: String,
    var speed: Long,
    var gravity: Double,
    var category: String
) : Product() {

}