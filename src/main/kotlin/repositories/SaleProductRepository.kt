package com.mac350.repositories

import com.mac350.models.Sale
import com.mac350.models.SaleProduct
import com.mac350.plugins.suspendTransaction
import com.mac350.tables.*
import org.jetbrains.exposed.sql.and

class SaleProductRepository {
    companion object {
        suspend fun getSaleProduct(idSale: Int, idProduct: Int) : SaleProductDAO? = suspendTransaction {
            SaleProductDAO.find {
                (SaleProductT.idSale eq idSale) and
                (SaleProductT.idProduct eq idProduct)
            }.firstOrNull()
        }

        suspend fun getSaleProductsBySale(idSale: Int) : List<SaleProduct> = suspendTransaction {
            SaleProductDAO.find { SaleProductT.idSale eq idSale }
                .map(::daoToSaleProduct)
        }

        suspend fun getCartSize(idSale: Int) : Long = suspendTransaction {
            SaleProductDAO.find { SaleProductT.idSale eq idSale }.count()
        }

        suspend fun newSaleProduct(cart: SaleDAO, product: ProductDAO) : SaleProductDAO = suspendTransaction {
            SaleProductDAO.new {
                this.idSale = cart
                this.idProduct = product
                this.quantity = 1
            }
        }

        suspend fun alterQuantity(saleProduct: SaleProductDAO, quantity: Long) = suspendTransaction {
            saleProduct.quantity = quantity
        }

        suspend fun deleteSaleProduct(saleProduct: SaleProductDAO) = suspendTransaction {
            saleProduct.delete()
        }
    }
}