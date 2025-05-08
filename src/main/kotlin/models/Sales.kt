package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Sales (
    val id: Long,
    val idClient: Long,
    val idEmployee: Long,
    var totalPriceGold: Long,
    var totalQuantity: Long,
    var status: String,
    val createdAt: String,
    var updatedAt: String
) {

}