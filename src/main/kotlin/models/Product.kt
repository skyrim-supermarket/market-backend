package com.mac350.models

import com.mac350.plugins.suspendTransaction
import com.mac350.tables.ProductDAO
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.CumeDist
import java.util.Date

@Serializable
sealed class Product {
    abstract val id: Long
    abstract var productName: String
    abstract var image: String?
    abstract var priceGold: Long
    abstract var stock: Long
    abstract var description: String
    abstract var type: String
    abstract val createdAt: String
    abstract var updatedAt: String
    abstract var standardDiscount: Long
    abstract var specialDiscount: Long
    abstract var hasDiscount: Boolean
}

@Serializable
sealed class GeneralFilter {
    abstract val page: Long
    abstract val productsPerPage: Long
    abstract val productName: String?
    abstract val minPriceGold: Long?
    abstract val maxPriceGold: Long?
    abstract val type: String?
    abstract val hasDiscount: Boolean?
}

@Serializable
data class AllProductsFilter (
    override val page: Long = 0,
    override val productsPerPage: Long,
    override val productName: String?,
    override val minPriceGold: Long?,
    override val maxPriceGold: Long?,
    override val type: String?,
    override val hasDiscount: Boolean?
) : GeneralFilter()

@Serializable
data class ProductFilterTest (
    val type: String,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class QueryResults (
    val query: List<ProductCardInfo>,
    val totalCount: Int
)

@Serializable
data class ProductCardInfo (
    val id: Long,
    val productName: String,
    val image: String?,
    val priceGold: Long,
    val stock: Long,
    val type: String
)

suspend fun parseMultiPart(multipart : MultiPartData): Pair<Map<String, String>, Map<String, ByteArray>> {
    var fields = mutableMapOf<String, String>()
    var files = mutableMapOf<String, ByteArray>()

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                if (part.name != null) {
                    fields[part.name!!] = part.value
                }
            }
            is PartData.FileItem -> {
                if (part.name != null) {
                    files[part.name!!] = part.provider().toByteArray()
                }
            }
            else -> {}
        }
        part.dispose()
    }

    return fields to files
}

suspend fun createNewProduct(productName: String, priceGold: Long, description: String, standardDiscount: Long, specialDiscount: Long): ProductDAO {
    val date = Date(System.currentTimeMillis()).toString()
    val newProduct = suspendTransaction {
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

    return newProduct
}


@Serializable
data class AmmoInsertTest (
    val productName: String,
    val priceGold: Long,
    val description: String,
    val standardDiscount: Long,
    val specialDiscount: Long,
    val magical: String,
    val craft: String,
    val speed: Double,
    val gravity: Double,
    val category: String
)
