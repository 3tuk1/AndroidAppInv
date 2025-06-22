package com.inv.inventryapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "barcode")
data class Barcode(
    @PrimaryKey(autoGenerate = true)
    var barcodeId: Int = 0,
    @ColumnInfo(name = "barcode_number")
    var barcodeNumber: Long = 0L // ★ Int から Long に変更
)
