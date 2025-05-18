package com.inv.inventryapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "main_items")
public class MainItem {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    // 商品の数量
    @ColumnInfo(name = "quantity")
    private int quantity;

    // 商品のカテゴリ
    @ColumnInfo(name = "category")
    private String category;

    // 商品の名前
    @ColumnInfo(name = "name")
    private String name;

    // 商品の賞味期限
    @ColumnInfo(name = "expiration_date")
    private String expirationDate;

    // コンストラクタ
    public MainItem(int quantity, String category, String name, String expirationDate) {
        this.quantity = quantity;
        this.category = category;
        this.name = name;
        this.expirationDate = expirationDate;
    }

    // ゲッター・セッター
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
}