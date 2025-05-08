package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Account {
    abstract val id: Long
    abstract var name: String
    abstract var email: String
    abstract var password: String
    abstract val createdAt: String
    abstract var updatedAt: String
}