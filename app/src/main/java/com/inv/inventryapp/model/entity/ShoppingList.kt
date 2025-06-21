package com.inv.inventryapp.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "shopping_list")
public class ShoppingList {
    /**
     * ショッピングリストエンティティ
     * 買い物リストの情報を保持するクラス
     * 商品のショッピングリスト情報を保持するクラス
     * 各フィールドはデータベースのカラムに対応しており、
     * 主キーとしてlistIDを使用します。
     * 各ショッピングリストは、商品名と数量を持ちます。
     */
    @PrimaryKey(autoGenerate = true)
    private int listId;

    @ColumnInfo(name = "priority")
    private int priority;

    @ColumnInfo(name = "product_name")
    private String productName;

    @ColumnInfo(name = "quantity")
    private int quantity;

    // コンストラクタ
    public ShoppingList(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    // getter, setter
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

