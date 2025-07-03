package com.mac350.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed class Product {
    abstract val id: Long
    abstract var productName: String
    abstract var image: String?
    abstract var priceGold: Long
    abstract var stock: Long
    abstract var description: String
    abstract var type: String
    abstract val createdAt: String
    abstract var updatedAt: String
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
data class Filter (
    val type: String,
    val page: Int,
    val pageSize: Int,
    val productName: String? = null,
    val minPriceGold: Long? = null,
    val maxPriceGold: Long? = null,
    val orderBy: String? = null
)

@Serializable
data class QueryResults (
    val query: List<ProductCardInfo>,
    val totalCount: Int
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
data class ProductCardInfo (
    val id: Long,
    val productName: String,
    val image: String?,
    val priceGold: Long,
    val stock: Long,
    val type: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
data class ProductCartInfo (
    val id: Long,
    val productName: String,
    val image: String?,
    val priceGold: Long,
    val quantity: Long,
    val type: String
)