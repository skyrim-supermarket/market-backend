package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Clothing (
    override val id: Long,
    override var productName: String,
    override var image: String?,
    override var priceGold: Long,
    override var stock: Long,
    override var description: String,
    override var type: String,
    override val createdAt: String,
    override var updatedAt: String
) : Product() {

}