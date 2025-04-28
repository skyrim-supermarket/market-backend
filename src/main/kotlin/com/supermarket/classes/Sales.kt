package org.example.com.supermarket.classes

class Sales (
    private val id: Long,
    private val idClient: Long,
    private val idEmployee: Long,
    private var totalPriceGold: Long,
    private var totalQuantity: Long,
    private var status: String,
    private val createdAt: String
) {
    private var updatedAt = this.createdAt

    fun getId(): Long {
        return this.id
    }

    fun getIdClient(): Long {
        return this.idClient
    }

    fun getIdEmployee(): Long {
        return this.idEmployee
    }

    fun getTotalPriceGold(): Long {
        return this.totalPriceGold
    }

    fun setTotalPriceGold(totalPriceGold: Long) {
        this.totalPriceGold = totalPriceGold
    }

    fun getTotalQuantity(): Long {
        return this.totalQuantity
    }

    fun setTotalQuantity(totalQuantity: Long) {
        this.totalQuantity = totalQuantity
    }

    fun getStatus(): String {
        return this.status
    }

    fun setStatus(status: String) {
        this.status = status
    }

    fun getCreatedAt(): String {
        return this.createdAt
    }

    fun getUpdatedAt(): String {
        return this.updatedAt
    }

    fun setUpdatedAt(updatedAt: String) {
        this.updatedAt = updatedAt
    }
}