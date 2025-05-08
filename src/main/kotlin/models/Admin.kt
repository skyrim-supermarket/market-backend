package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
data class Admin (
    override val id: Long,
    override var name: String,
    override var email: String,
    override var password: String,
    override val createdAt: String,
    override var updatedAt: String
) : Account() {

}