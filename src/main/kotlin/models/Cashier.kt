package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Cashier (
    override val id: Long,
    override var username: String,
    override var email: String,
    override var password: String,
    override val type: String,
    override val createdAt: String,
    override var updatedAt: String,
    var totalCommissions: Long,
    var section: Long
) : Account() {

}