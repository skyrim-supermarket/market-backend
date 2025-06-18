package com.mac350.models

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable
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
    val page: Int = 1,
    val pageSize: Int = 42,
    val type: String = ""
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

/*suspend fun createNewProduct(fields : Map<String, String>, files : Map<String, ByteArray>, type : String): void {
    val productName = fields["productName"]
    val priceGold = fields["priceGold"]
    val description = fields["description"]
    val standardDiscount = fields["standardDiscount"]
    val specialDiscount = fields["specialDiscount"]

    val imageBytes = files["image"]

    if(productName == null || priceGold == null || description == null || standardDiscount == null || specialDiscount == null)
}*/
