package com.inv.inventryapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "food_items")
public class FoodItem {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String category;
    private String expiryDate;
    private int quantity;
    private String barcode;
    private boolean isDeleted;
    private boolean isSynced;
    private Date lastModified;

    // 引数なしコンストラクタ
    public FoodItem() {
        this.id = java.util.UUID.randomUUID().toString();  // IDを自動生成
    }

    @Ignore
    public FoodItem(String name, String category, String expiryDate, int quantity, String barcode) {
        this();  // 自動ID生成のため
        this.name = name;
        this.category = category;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.barcode = barcode;
        this.isDeleted = false;
        this.isSynced = false;
        this.lastModified = new Date();
    }

    // 既存のコンストラクタに対応
    @Ignore
    public FoodItem(String name, String category, String expiryDate, int quantity) {
        this(name, category, expiryDate, quantity, "");
    }

    // Getter/Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public Date getLastModified() { return lastModified; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }

    // 消費メソッド
    public boolean consume(int amount) {
        if (amount <= 0 || amount > quantity) return false;
        quantity -= amount;
        lastModified = new Date();
        isSynced = false;
        return true;
    }

    // 賞味期限が近いか判定するメソッド
    public boolean isNearExpiry() {
        // 実装例：日付解析して現在日との差分計算
        // 簡易版として文字列比較
        try {
            return expiryDate.compareTo(java.time.LocalDate.now().plusDays(7).toString()) <= 0;
        } catch (Exception e) {
            return false;
        }
    }
}