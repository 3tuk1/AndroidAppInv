package com.inv.inventryapp.model.dto

data class PieChartSummary(
    // 円グラフ用のデータ (Key: "購入", "消費", "廃棄", Value: 合計金額)
    val pieDataMap: Map<String, Float>,
    // テキスト表示用の各合計値
    val totalPurchase: Int,
    val totalConsumption: Int,
    val totalDisposal: Int,
    val currentStockValue: Int,
    val overallTotal: Int
)

