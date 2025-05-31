package com.inv.inventryapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters; // 追加
import com.inv.inventryapp.utils.LocalDateConverter; // 追加
import java.time.LocalDate;

@Entity(tableName = "history")
@TypeConverters(LocalDateConverter.class) // 追加
public class History {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "history_id")
    private long historyId;

    @ColumnInfo(name = "id")
    private int itemId;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "type")
    private String type; // "input" または "output"

    @ColumnInfo(name = "date")
    private LocalDate date; // 操作日時

    @ColumnInfo(name = "consumption_reason") // 追加
    private String consumptionReason; // 追加 (例: "通常使用", "廃棄", "他目的")

    // コンストラクタ
    public History(int itemId, int quantity, String type, LocalDate date, String consumptionReason) { // consumptionReason を追加
        this.itemId = itemId;
        this.quantity = quantity;
        this.type = type;
        this.date = date;
        this.consumptionReason = consumptionReason; // 追加
    }

    // ゲッター・セッター
    public long getHistoryId() { return historyId; }
    public void setHistoryId(long historyId) { this.historyId = historyId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getConsumptionReason() { return consumptionReason; } // 追加
    public void setConsumptionReason(String consumptionReason) { this.consumptionReason = consumptionReason; } // 追加
}
