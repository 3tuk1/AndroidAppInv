package com.inv.inventryapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.LocalDate

@Entity(tableName = "analysis")
open class Analysis { // data class を open class に変更
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @ColumnInfo(name = "product_name")
    var productName: String = ""
    @ColumnInfo(name = "priority_score")
    var priorityScore: Float = 0f
    @ColumnInfo(name = "recommended_quantity")
    var recommendedQuantity: Int = 0
    @ColumnInfo(name = "remaining_days")
    var remainingDays: Float = 0f
    @ColumnInfo(name = "out_of_stock_date")
    var outOfStockDate: LocalDate? = null
    @ColumnInfo(name = "days_per_item")
    var daysPerItem: Float = 0f
}
