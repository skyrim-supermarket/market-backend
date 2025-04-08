package org.example.com.supermarket.classes

class Client(
    id: Long,
    name: String,
    email: String,
    password: String,
    createdAt: String,
    private var isSpecialClient: Boolean,
    private var lastRun: String,
    private var address: String
) : Account(id, name, email, password, createdAt) {
    fun getIsSpecialClient(): Boolean {
        return this.isSpecialClient
    }

    fun changeSpecialClient() {
        this.isSpecialClient = !isSpecialClient
    }

    fun getLastRun(): String {
        return this.lastRun
    }

    fun setLastRun(lastRun: String) {
        this.lastRun = lastRun
    }

    fun getAddress(): String {
        return this.address
    }

    fun setAddress(address: String) {
        this.address = address
    }
}