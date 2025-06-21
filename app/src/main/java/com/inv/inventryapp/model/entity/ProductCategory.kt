package com.inv.inventryapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "product_category")
data class ProductCategory(
    @PrimaryKey(autoGenerate = true)
    var categoryId: Int = 0,
    @ColumnInfo(name = "category_name")
    var categoryName: String= ""
)

