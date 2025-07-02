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
    var weight: Double,
    var magical: Boolean,
    var craft: String,
    var protection: Double,
    var heavy: Boolean,
    var category: String
) : Product() {

}