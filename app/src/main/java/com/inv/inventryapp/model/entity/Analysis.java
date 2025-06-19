package com.inv.inventryapp.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.time.LocalDate;

@Entity(tableName = "analysis")
public class Analysis {
    /** Analysisエンティティは、商品の分析結果を格納するためのデータモデルです。
     * 各商品の優先度スコア、推奨数量、残り日数、欠品予定日などの情報を含みます。
     * このエンティティは、Roomデータベースのテーブルとして定義されており、
     * 主キーとして自動生成されるIDを持ちます。
     */

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "product_name")
    private String productName;

    @ColumnInfo(name = "priority_score")
    private float priorityScore;

    @ColumnInfo(name = "recommended_quantity")
    private int recommendedQuantity;

    @ColumnInfo(name = "remaining_days")
    private float remainingDays;

    @ColumnInfo(name = "out_of_stock_date")
    private LocalDate outOfStockDate;

    // コンストラクタ
    public Analysis(String productName, float priorityScore, int recommendedQuantity, float remainingDays, LocalDate outOfStockDate) {
        this.productName = productName;
        this.priorityScore = priorityScore;
        this.recommendedQuantity = recommendedQuantity;
        this.remainingDays = remainingDays;
        this.outOfStockDate = outOfStockDate;
    }

    // getter, setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public float getPriorityScore() { return priorityScore; }
    public void setPriorityScore(float priorityScore) { this.priorityScore = priorityScore; }
    public int getRecommendedQuantity() { return recommendedQuantity; }
    public void setRecommendedQuantity(int recommendedQuantity) { this.recommendedQuantity = recommendedQuantity; }
    public float getRemainingDays() { return remainingDays; }
    public void setRemainingDays(float remainingDays) { this.remainingDays = remainingDays; }
    public LocalDate getOutOfStockDate() { return outOfStockDate; }
    public void setOutOfStockDate(LocalDate outOfStockDate) { this.outOfStockDate = outOfStockDate; }
}

