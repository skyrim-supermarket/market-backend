package org.example.com.supermarket.classes

class SaleProduct (
    private val idProduct: Long,
    private val idSale: Long,
    private val quantity: Long
) {
    fun getIdProduct(): Long {
        return this.idProduct
    }

    fun getIdSale(): Long {
        return this.idSale
    }

    fun getQuantity(): Long {
        return this.quantity
    }
}