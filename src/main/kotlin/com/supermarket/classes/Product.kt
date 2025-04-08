package org.example.com.supermarket.classes

abstract class Product (
    private val id: Long,
    private var name: String,
    private var image: String,
    private var priceGold: Long,
    private var stock: Long,
    private val createdAt: String,
    private var standardDiscount: Long,
    private var specialDiscount: Long
) {
    private var hasDiscount = false
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

    open fun getImage(): String {
        return this.image
    }

    open fun setImage(image: String) {
        this.image = image
    }

    open fun getPrice(): Long {
        return this.priceGold
    }

    open fun setPrice(priceGold: Long) {
        this.priceGold = priceGold
    }

    open fun getStock(): Long {
        return this.stock
    }

    open fun setStock(stock: Long) {
        this.stock = stock
    }

    open fun getCreatedAt(): String {
        return this.createdAt
    }

    open fun hasDiscount(): Boolean {
        return this.hasDiscount
    }

    open fun changeDiscount() {
        this.hasDiscount = !this.hasDiscount
    }

    open fun getUpdatedAt(): String {
        return this.updatedAt
    }

    open fun setUpdatedAt(updatedAt: String) {
        this.updatedAt = updatedAt
    }

    open fun getStandardDiscount(): Long {
        return this.standardDiscount
    }

    open fun setStandardDiscount(standardDiscount: Long) {
        this.standardDiscount = standardDiscount
    }

    open fun getSpecialDiscount(): Long {
        return this.specialDiscount
    }

    open fun setSpecialDiscount(specialDiscount: Long) {
        this.specialDiscount = specialDiscount
    }

    abstract fun getAll()
}