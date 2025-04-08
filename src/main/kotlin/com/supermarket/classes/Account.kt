package org.example.com.supermarket.classes

abstract class Account (
    private val id: Long,
    private var name: String,
    private var email: String,
    private var password: String,
    private val createdAt: String
) {
    private var updatedAt = this.createdAt

    open fun getId(): Long {
        return this.id
    }

    open fun getName(): String {
        return this.name
    }

    open fun setName(name: String) {
        this.name = name
    }

    open fun getEmail(): String {
        return this.email
    }

    open fun setEmail(email: String) {
        this.email = email
    }

    open fun getPassword(): String {
        return this.password
    }

    open fun setPassword(password: String) {
        this.password = password
    }

    open fun getCreatedAt(): String {
        return this.createdAt
    }

    open fun getUpdatedAt(): String {
        return this.updatedAt
    }

    open fun setUpdatedAt(updatedAt: String) {
        this.updatedAt = updatedAt
    }
}