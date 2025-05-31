package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    override val id: Long,
    override var username: String,
    override var email: String,
    override var password: String,
    override val type: String,
    override val createdAt: String,
    override var updatedAt: String,
    var isSpecialClient: Boolean,
    var lastRun: String,
    var address: String
) : Account() {
    fun changeSpecialClient() {
        this.isSpecialClient = !isSpecialClient
    }
}