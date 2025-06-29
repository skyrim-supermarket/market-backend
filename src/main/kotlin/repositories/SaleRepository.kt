package com.mac350.repositories

import com.mac350.models.Account
import com.mac350.models.Client
import com.mac350.models.Product
import com.mac350.plugins.suspendTransaction
import com.mac350.tables.AccountDAO
import com.mac350.tables.ProductDAO
import com.mac350.tables.SaleDAO
import com.mac350.tables.SaleT
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import java.util.*

class SaleRepository {
    companion object {
        suspend fun newCart(account: AccountDAO, date: String): SaleDAO = suspendTransaction {
            SaleDAO.new {
                this.idClient = account
                this.totalPriceGold = 0
                this.totalQuantity = 0
                this.finished = false
                this.status = "Cart"
                this.createdAt = date
                this.updatedAt = date
            }
        }

        suspend fun getCartByAccount(account: AccountDAO): SaleDAO = suspendTransaction {
            val cart = SaleDAO.find {
                (SaleT.idClient eq account.id) and
                        (SaleT.finished eq false)
            }.firstOrNull()

            if (cart == null) {
                val date = Date(System.currentTimeMillis()).toString()
                SaleDAO.new {
                    this.idClient = account
                    this.totalPriceGold = 0
                    this.totalQuantity = 0
                    this.finished = false
                    this.status = "Cart"
                    this.createdAt = date
                    this.updatedAt = date
                }
            } else cart
        }

        suspend fun alterTotalPrice(sale: SaleDAO, product: ProductDAO, previousQuantity: Long, quantity: Long, date: String): Unit = suspendTransaction {
            sale.totalPriceGold += (quantity-previousQuantity)*product.priceGold
            sale.updatedAt = date
        }

        suspend fun finishOnlineSale(sale: SaleDAO, date: String) = suspendTransaction {
            sale.finished = true
            sale.status = "Waiting for CarroçaBoy"
            sale.updatedAt = date
        }
    }
}