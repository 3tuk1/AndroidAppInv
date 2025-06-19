package com.inv.inventryapp.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "product_category")
public class ProductCategory {
    /**
     * 商品カテゴリエンティティ
     * 商品のカテゴリ情報を保持するクラス
     * 各フィールドはデータベースのカラムに対応しており、
     * 主キーとしてcategoryIdを使用します。
     */

    @PrimaryKey(autoGenerate = true)
    private int categoryId;

    @ColumnInfo(name = "category_name")
    private String categoryName;

    // コンストラクタ
    public ProductCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    // getter, setter
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}

