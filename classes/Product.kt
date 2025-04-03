class Product(
    val id: ULong,
    val name: String,
    val image: String,
    val price_gold: UInt,
    val stock: UInt,
    val has_discount: Boolean,
    val standard_discount: UInt,
    val special_discount: UInt,
    val category: String,
    val created_at: String,
    val updated_at: String
) {

    fun applyDiscount() {
        has_discount = true;
    }

    fun removeDiscount() {
        has_discount = false;
    }

    fun viewDetails() : List<Any> {
        if(has_discount) return arrayOf(name, image, price_gold, stock, standard_discount, special_discount, category, created_at, updated_at)
        return arrayOf(name, image, price_gold, stock, category, created_at, updated_at)

    }

}