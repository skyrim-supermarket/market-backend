package com.mac350.repositories

import com.mac350.models.Account
import com.mac350.models.Client
import com.mac350.models.Product
import com.mac350.models.Sale
import com.mac350.plugins.suspendTransaction
import com.mac350.tables.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import java.util.*

class SaleRepository {
    companion object {
        suspend fun newCart(account: AccountDAO, date: String): SaleDAO = suspendTransaction {
            SaleDAO.new {
                this.idClient = account
                this.idEmployee = null
                this.totalPriceGold = 0
                this.totalQuantity = 0
                this.finished = false
                this.status = "Cart"
                this.address = ""
                this.createdAt = date
                this.updatedAt = date
            }
        }

        suspend fun newIrlPurchase(account: AccountDAO, date: String): SaleDAO = suspendTransaction {
            SaleDAO.new {
                this.idClient = null
                this.idEmployee = account
                this.totalPriceGold = 0
                this.totalQuantity = 0
                this.finished = false
                this.status = "Ongoing IRL purchase"
                this.address = "Locally"
                this.createdAt = date
                this.updatedAt = date
            }
        }

        suspend fun getCartByAccount(account: AccountDAO): SaleDAO = suspendTransaction {
            val cart = SaleDAO.find {
                (SaleT.idClient eq account.id) and
                        (SaleT.finished eq false) and
                        (SaleT.status eq "Cart")
            }.firstOrNull()

            if (cart == null) {
                val date = Date(System.currentTimeMillis()).toString()
                SaleDAO.new {
                    this.idClient = account
                    this.totalPriceGold = 0
                    this.totalQuantity = 0
                    this.finished = false
                    this.status = "Cart"
                    this.address = ""
                    this.createdAt = date
                    this.updatedAt = date
                }
            } else cart
        }

        suspend fun getIrlPurchaseByAccount(account: AccountDAO): SaleDAO? = suspendTransaction {
            SaleDAO.find {
                (SaleT.idEmployee eq account.id) and
                        (SaleT.finished eq false) and
                        (SaleT.status eq "Ongoing IRL purchase")
            }.firstOrNull()
        }

        suspend fun getSaleById(saleId: Int): SaleDAO? = suspendTransaction {
            SaleDAO.find {
                (SaleT.id eq saleId)
            }.firstOrNull()
        }

        suspend fun getSales(): List<Sale> = suspendTransaction {
            SaleDAO.find {
                    (SaleT.finished eq true)
            }.map(::daoToSale)
        }

        suspend fun getFinishedSalesByClient(clientId: Int): List<Sale> = suspendTransaction {
            SaleDAO.find {
                (SaleT.idClient eq clientId) and
                        (SaleT.finished eq true)
            }.map(::daoToSale)
        }

        suspend fun getFinishedSalesByEmployee(employeeId: Int): List<Sale> = suspendTransaction {
            SaleDAO.find {
                (SaleT.idEmployee eq employeeId) and
                        (SaleT.status eq "Delivered!")
            }.map(::daoToSale)
        }

        suspend fun getAvailableSales(): List<Sale> = suspendTransaction {
            SaleDAO.find {
                SaleT.status eq "Waiting for CarroçaBoy!"
            }.map(::daoToSale)
        }

        suspend fun assignClient(client: AccountDAO, sale: SaleDAO) = suspendTransaction {
            sale.idClient = client
        }

        suspend fun getSaleToBeDelivered(account: AccountDAO): SaleDAO? = suspendTransaction {
            SaleDAO.find {
                (SaleT.status eq "To be delivered by ${account.username}") and
                        (SaleT.idEmployee eq account.id.value)
            }.firstOrNull()
        }

        suspend fun getSaleToBeDeliveredById(saleId: Int): SaleDAO? = suspendTransaction {
            SaleDAO.find {
                (SaleT.id eq saleId) and
                (SaleT.status like "To be delivered by %")
            }.firstOrNull()
        }

        suspend fun getAvailableSaleById(saleId: Int): SaleDAO? = suspendTransaction {
            SaleDAO.find {
                (SaleT.id eq saleId) and
                        (SaleT.status eq "Waiting for CarroçaBoy!")
            }.firstOrNull()
        }

        suspend fun deleteIrlPurchase(purchase: SaleDAO) = suspendTransaction {
            purchase.delete()
        }

        suspend fun assignCarrocaBoy(saleDAO: SaleDAO, account: AccountDAO, date: String) = suspendTransaction {
            saleDAO.idEmployee = account
            saleDAO.updatedAt = date
            saleDAO.status = "To be delivered by ${account.username}"
        }

        suspend fun alterTotalPrice(sale: SaleDAO, product: ProductDAO, previousQuantity: Long, quantity: Long, date: String) = suspendTransaction {
            sale.totalPriceGold += (quantity-previousQuantity)*product.priceGold
            sale.updatedAt = date
        }

        suspend fun alterTotalQuantity(sale: SaleDAO, delta: Long, date: String) = suspendTransaction {
            sale.totalQuantity += delta
            sale.updatedAt = date
        }

        suspend fun alterAddress(sale: SaleDAO, address: String, date: String) = suspendTransaction {
            sale.address = address
            sale.updatedAt = date
        }

        suspend fun finishSale(sale: SaleDAO, message: String, date: String) = suspendTransaction {
            sale.finished = true
            sale.status = message
            sale.updatedAt = date
        }
    }
}