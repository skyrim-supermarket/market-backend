package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Account {
    abstract val id: Long
    abstract var username: String
    abstract var email: String
    abstract var password: String
    abstract val type: String
    abstract val createdAt: String
    abstract var updatedAt: String
    abstract var lastRun: String
}

@Serializable
data class Login (
    val email: String,
    val password: String
)

@Serializable
data class Register (
    val username: String,
    val email: String,
    val password: String,
    val address: String
)
