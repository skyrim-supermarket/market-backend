package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Product {
    abstract val id: Long
    abstract var productName: String
    abstract var image: String
    abstract var priceGold: Long
    abstract var stock: Long
    abstract val createdAt: String
    abstract var updatedAt: String
    abstract var standardDiscount: Long
    abstract var specialDiscount: Long
    abstract var hasDiscount: Boolean
}