package com.inv.inventryapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
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
    private String date; // 操作日時

    // コンストラクタ
    public History(int itemId, int quantity, String type, String date) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.type = type;
        this.date = date;
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

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}