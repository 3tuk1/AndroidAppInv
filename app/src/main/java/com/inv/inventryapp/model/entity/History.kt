package com.inv.inventryapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.LocalDate

@Entity(tableName = "history")
data class History(
    @ColumnInfo(name = "product_name")
    var productName: String,
    @ColumnInfo(name = "type")
    var type: String,
    @ColumnInfo(name = "date")
    var date: LocalDate,
    @ColumnInfo(name = "quantity")
    var quantity: Int
) {
    @PrimaryKey(autoGenerate = true)
    var historyId: Int = 0
}
