package com.inv.inventryapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.LocalDate

@Entity(tableName = "analysis")
data class Analysis(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "product_name")
    var productName: String,
    @ColumnInfo(name = "priority_score")
    var priorityScore: Float,
    @ColumnInfo(name = "recommended_quantity")
    var recommendedQuantity: Int,
    @ColumnInfo(name = "remaining_days")
    var remainingDays: Float,
    @ColumnInfo(name = "out_of_stock_date")
    var outOfStockDate: LocalDate
)

