package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
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
    abstract var standardDiscount: Long
    abstract var specialDiscount: Long
    abstract var hasDiscount: Boolean
}

@Serializable
sealed class GeneralFilter {
    abstract val page: Long
    abstract val productsPerPage: Long
    abstract val productName: String?
    abstract val minPriceGold: Long?
    abstract val maxPriceGold: Long?
    abstract val type: String?
    abstract val hasDiscount: Boolean?
}

@Serializable
data class AllProductsFilter (
    override val page: Long = 0,
    override val productsPerPage: Long,
    override val productName: String?,
    override val minPriceGold: Long?,
    override val maxPriceGold: Long?,
    override val type: String?,
    override val hasDiscount: Boolean?
) : GeneralFilter()

@Serializable
data class PaginationFilter (
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


@Serializable
data class AmmoInsertTest (
    val productName: String,
    val priceGold: Long,
    val description: String,
    val standardDiscount: Long,
    val specialDiscount: Long,
    val magical: String,
    val craft: String,
    val speed: Double,
    val gravity: Double,
    val category: String
)
