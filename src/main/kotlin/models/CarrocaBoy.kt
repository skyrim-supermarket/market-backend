package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class CarrocaBoy (
    override val id: Long,
    override var name: String,
    override var email: String,
    override var password: String,
    override val createdAt: String,
    override var updatedAt: String,
    var totalCommissions: Long,
) : Account() {

}