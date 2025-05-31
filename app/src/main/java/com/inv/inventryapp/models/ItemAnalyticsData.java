package com.inv.inventryapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.inv.inventryapp.utils.LocalDateConverter;
import java.time.LocalDate;

@Entity(tableName = "item_analytics_data",
        foreignKeys = @ForeignKey(entity = MainItem.class,
                                parentColumns = "id",
                                childColumns = "item_id",
                                onDelete = ForeignKey.CASCADE))
@TypeConverters(LocalDateConverter.class)
public class ItemAnalyticsData {

    @PrimaryKey
    @ColumnInfo(name = "item_id")
    private int itemId;

    @ColumnInfo(name = "consumption_reason")
    private String consumptionReason; // 通常使用/廃棄/他目的

    @ColumnInfo(name = "consumption_timing")
    private String consumptionTiming; // 朝/昼/夕/夜

    @ColumnInfo(name = "consumption_pace")
    private String consumptionPace; // 例: "3日ごと", "週に1回" など

    @ColumnInfo(name = "min_stock_level")
    private int minStockLevel;

    @ColumnInfo(name = "optimal_stock_level")
    private int optimalStockLevel;

    @ColumnInfo(name = "restock_timing_guideline")
    private String restockTimingGuideline; // 例: "残り3個になったら", "毎週土曜日"

    @ColumnInfo(name = "average_consumption_days")
    private float averageConsumptionDays; // 平均消費にかかる日数

    @ColumnInfo(name = "stockout_prediction_date")
    private LocalDate stockoutPredictionDate; // 在庫切れ予測日

    @ColumnInfo(name = "seasonal_consumption_pattern")
    private String seasonalConsumptionPattern; // 例: "夏に消費量増加", "冬は消費しない"

    // Constructor
    public ItemAnalyticsData(int itemId, String consumptionReason, String consumptionTiming, String consumptionPace,
                             int minStockLevel, int optimalStockLevel, String restockTimingGuideline,
                             float averageConsumptionDays, LocalDate stockoutPredictionDate, String seasonalConsumptionPattern) {
        this.itemId = itemId;
        this.consumptionReason = consumptionReason;
        this.consumptionTiming = consumptionTiming;
        this.consumptionPace = consumptionPace;
        this.minStockLevel = minStockLevel;
        this.optimalStockLevel = optimalStockLevel;
        this.restockTimingGuideline = restockTimingGuideline;
        this.averageConsumptionDays = averageConsumptionDays;
        this.stockoutPredictionDate = stockoutPredictionDate;
        this.seasonalConsumptionPattern = seasonalConsumptionPattern;
    }

    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getConsumptionReason() { return consumptionReason; }
    public void setConsumptionReason(String consumptionReason) { this.consumptionReason = consumptionReason; }

    public String getConsumptionTiming() { return consumptionTiming; }
    public void setConsumptionTiming(String consumptionTiming) { this.consumptionTiming = consumptionTiming; }

    public String getConsumptionPace() { return consumptionPace; }
    public void setConsumptionPace(String consumptionPace) { this.consumptionPace = consumptionPace; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public int getOptimalStockLevel() { return optimalStockLevel; }
    public void setOptimalStockLevel(int optimalStockLevel) { this.optimalStockLevel = optimalStockLevel; }

    public String getRestockTimingGuideline() { return restockTimingGuideline; }
    public void setRestockTimingGuideline(String restockTimingGuideline) { this.restockTimingGuideline = restockTimingGuideline; }

    public float getAverageConsumptionDays() { return averageConsumptionDays; }
    public void setAverageConsumptionDays(float averageConsumptionDays) { this.averageConsumptionDays = averageConsumptionDays; }

    public LocalDate getStockoutPredictionDate() { return stockoutPredictionDate; }
    public void setStockoutPredictionDate(LocalDate stockoutPredictionDate) { this.stockoutPredictionDate = stockoutPredictionDate; }

    public String getSeasonalConsumptionPattern() { return seasonalConsumptionPattern; }
    public void setSeasonalConsumptionPattern(String seasonalConsumptionPattern) { this.seasonalConsumptionPattern = seasonalConsumptionPattern; }
}

