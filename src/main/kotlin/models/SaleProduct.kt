package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class SaleProduct (
    val idProduct: Long,
    val idSale: Long,
    val quantity: Long
) {

}