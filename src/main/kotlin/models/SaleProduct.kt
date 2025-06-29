package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class SaleProduct (
    val id: Int,
    val idProduct: Int,
    val idSale: Int,
    val quantity: Long
) {

}