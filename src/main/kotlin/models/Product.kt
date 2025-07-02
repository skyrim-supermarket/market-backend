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

@Serializable
sealed class GeneralFilter {
    abstract val page: Long
    abstract val productsPerPage: Long
    abstract val productName: String?
    abstract val minPriceGold: Long?
    abstract val maxPriceGold: Long?
    abstract val type: String?
}

@Serializable
data class AllProductsFilter (
    override val page: Long = 0,
    override val productsPerPage: Long,
    override val productName: String?,
    override val minPriceGold: Long?,
    override val maxPriceGold: Long?,
    override val type: String?,
) : GeneralFilter()

@Serializable
data class Filter (
    val type: String,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class QueryResults (
    val query: List<ProductCardInfo>,
    val totalCount: Int
)

@Serializable
data class ProductCardInfo (
    val id: Long,
    val productName: String,
    val image: String?,
    val priceGold: Long,
    val stock: Long,
    val type: String
)

@Serializable
data class ProductCartInfo (
    val id: Long,
    val productName: String,
    val image: String?,
    val priceGold: Long,
    val quantity: Long,
    val type: String
)