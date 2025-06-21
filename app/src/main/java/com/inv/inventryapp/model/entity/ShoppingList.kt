package com.inv.inventryapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "shopping_list")
data class ShoppingList(

    @ColumnInfo(name = "priority")
    var priority: Int,
    @ColumnInfo(name = "product_name")
    var productName: String,
    @ColumnInfo(name = "quantity")
    var quantity: Int
){
    @PrimaryKey(autoGenerate = true)
    var listId: Int = 0
}

