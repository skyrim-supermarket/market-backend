package com.mac350.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
data class SaleProduct (
    val id: Int,
    val idProduct: Int,
    val idSale: Int,
    val quantity: Long
) {

}