package com.inv.inventryapp.model.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.LocalDate

@Entity(tableName = "product")
data class Product(
    @PrimaryKey(autoGenerate = true)
    var productId: Int = 0,
    @ColumnInfo(name = "product_name")
    var productName: String = "",
    @ColumnInfo(name = "price")
    var price: Int? = null,
    @ColumnInfo(name = "quantity")
    var quantity: Int? = null,
    @ColumnInfo(name = "location")
    var location: String = "",
    @ColumnInfo(name = "expiration_date")
    var expirationDate: LocalDate? = null,
    @ColumnInfo(name = "purchase_date")
    var purchaseDate: LocalDate? = null,
    @ColumnInfo(name = "image_path")
    var imagePath: String = "",
    @ColumnInfo(name = "category_id")
    var categoryId: Int = 0,
    @ColumnInfo(name = "barcode_id")
    var barcodeId: Int = 0,

    // 結合情報
    @Embedded(prefix = "category_")
    var category: ProductCategory = ProductCategory(),
    @Embedded(prefix = "barcode_")
    var barcode: Barcode = Barcode()
)
