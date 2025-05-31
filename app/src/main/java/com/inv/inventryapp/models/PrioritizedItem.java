package com.inv.inventryapp.models;

import java.time.LocalDate;

public class PrioritizedItem {
    public MainItem mainItem;
    public ItemAnalyticsData analyticsData; // 関連する分析データ
    public float priorityScore;         // 計算された優先度スコア
    public int recommendedPurchaseQuantity; // 推奨購入量
    public float remainingDays;          // 残存予測日数
    public LocalDate stockoutDate;       // 在庫切れ予測日

    public PrioritizedItem(MainItem mainItem, ItemAnalyticsData analyticsData, float priorityScore, int recommendedPurchaseQuantity, float remainingDays, LocalDate stockoutDate) {
        this.mainItem = mainItem;
        this.analyticsData = analyticsData;
        this.priorityScore = priorityScore;
        this.recommendedPurchaseQuantity = recommendedPurchaseQuantity;
        this.remainingDays = remainingDays;
        this.stockoutDate = stockoutDate;
    }

    // Getters (and setters if needed) can be added here
    public MainItem getMainItem() {
        return mainItem;
    }

    public ItemAnalyticsData getAnalyticsData() {
        return analyticsData;
    }

    public float getPriorityScore() {
        return priorityScore;
    }

    public int getRecommendedPurchaseQuantity() {
        return recommendedPurchaseQuantity;
    }

    public float getRemainingDays() {
        return remainingDays;
    }

    public LocalDate getStockoutDate() {
        return stockoutDate;
    }
}

