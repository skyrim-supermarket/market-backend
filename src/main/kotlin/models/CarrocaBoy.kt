package com.mac350.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
data class CarrocaBoy (
    override val id: Long,
    override var username: String,
    override var email: String,
    override var password: String,
    override val type: String,
    override val createdAt: String,
    override var updatedAt: String,
    override var lastRun: String,
    var totalCommissions: Long
) : Account() {

}