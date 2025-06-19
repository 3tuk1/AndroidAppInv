package com.inv.inventryapp.model.entity;

import java.time.LocalDate;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "history")
public class History {
    /**
     * 履歴エンティティ
     * 商品の履歴情報を保持するクラス
     * 各フィールドはデータベースのカラムに対応しており、
     * 主キーとしてhistoryIdを使用します。
     * 各履歴は、商品名、タイプ、日付、数量を持ちます。
     */
    @PrimaryKey(autoGenerate = true)
    private int historyId;

    @ColumnInfo(name = "product_name")
    private String productName;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "date")
    private LocalDate date;

    @ColumnInfo(name = "quantity")
    private int quantity;

    // コンストラクタ
    public History(String productName, String type, LocalDate date, int quantity) {
        this.productName = productName;
        this.type = type;
        this.date = date;
        this.quantity = quantity;
    }

    // getter, setter
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

