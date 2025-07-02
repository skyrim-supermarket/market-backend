package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Sale (
    val id: Long,
    val idClient: Long?,
    val idEmployee: Long?,
    var totalPriceGold: Long,
    var totalQuantity: Long,
    var finished: Boolean,
    var status: String,
    val createdAt: String,
    var updatedAt: String
) {

}

@Serializable
data class SaleInfo (
    val sale: Sale,
    val products: List<ProductCartInfo?>
)