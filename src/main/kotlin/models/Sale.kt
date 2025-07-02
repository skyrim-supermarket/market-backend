package com.mac350.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
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

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
data class SaleInfo (
    val sale: Sale,
    val products: List<ProductCartInfo?>
)