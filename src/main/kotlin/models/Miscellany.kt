package com.mac350.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
data class Miscellany (
    override val id: Long,
    override var productName: String,
    override var image: String?,
    override var priceGold: Long,
    override var stock: Long,
    override var description: String,
    override var type: String,
    override val createdAt: String,
    override var updatedAt: String,
    val questItem: String,
    val craftingUse: String,
    val modelType: String
) : Product() {

}