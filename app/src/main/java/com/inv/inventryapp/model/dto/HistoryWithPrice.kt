package com.inv.inventryapp.model.dto

import java.time.LocalDate

data class HistoryWithPrice(
    val historyId: Int,
    val productName: String,
    val type: String,
    val date: LocalDate,
    val quantity: Int,
    val price: Int? // 商品価格が未設定の場合を考慮し、Nullableにします
)

