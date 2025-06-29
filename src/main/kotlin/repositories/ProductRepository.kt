package com.mac350.repositories

import com.mac350.plugins.suspendTransaction
import com.mac350.tables.ProductDAO
import com.mac350.tables.ProductT

class ProductRepository {
    companion object {
        suspend fun getProductById(productId: Int) : ProductDAO? = suspendTransaction {
            ProductDAO.find { ProductT.id eq productId }.firstOrNull()
        }

        suspend fun newProduct(productName: String, priceGold: Long, description: String, standardDiscount: Long, specialDiscount: Long, date: String): ProductDAO = suspendTransaction {
            ProductDAO.new {
                this.productName = productName
                this.image = null
                this.priceGold = priceGold
                this.stock = 0
                this.description = description
                this.type = "ammunition"
                this.createdAt = date
                this.updatedAt = date
                this.standardDiscount = standardDiscount
                this.specialDiscount = specialDiscount
                this.hasDiscount = false
            }
        }

        suspend fun alterStock(product: ProductDAO, subtract: Long) = suspendTransaction {
            product.stock -= subtract
        }
    }
}