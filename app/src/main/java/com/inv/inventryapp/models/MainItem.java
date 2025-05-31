package com.inv.inventryapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters; // 追加
import com.inv.inventryapp.utils.LocalDateConverter; // 追加

import java.time.LocalDate; // 追加

@Entity(tableName = "main_items")
@TypeConverters(LocalDateConverter.class) // 追加
public class MainItem {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    // 商品の数量
    @ColumnInfo(name = "quantity")
    private int quantity;

    // 商品のカテゴリID（Categoryテーブルの外部キー）
    @ColumnInfo(name = "category_id")
    private int categoryId;

    // 商品の名前
    @ColumnInfo(name = "name")
    private String name;

    // 商品の賞味期限
    @ColumnInfo(name = "expiration_date")
    private LocalDate expirationDate; // String から LocalDate に変更

    // コンストラクタ
    public MainItem(int quantity, int categoryId, String name, LocalDate expirationDate) { // String から LocalDate に変更
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.name = name;
        this.expirationDate = expirationDate;
    }

    // ゲッター・セッター
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getExpirationDate() { return expirationDate; } // String から LocalDate に変更
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; } // String から LocalDate に変更
}

