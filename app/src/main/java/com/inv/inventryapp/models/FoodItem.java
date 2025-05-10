package com.inv.inventryapp.models;

import android.graphics.Bitmap;
import android.media.Image;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "food_items") // テーブル名を指定
public class FoodItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String barcode;
    private String expiryDate;
    private int quantity;
    private String category;

    private Bitmap image = null;

    // コンストラクタ、ゲッター、セッター
    public FoodItem(String name, String barcode, String expiryDate, int quantity, String category) {
        this.name = name;
        this.barcode = barcode;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.category = category;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
    public Bitmap getImage() {
        return image;
    }

    // ID の　取得と設定
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // 名前の取得と設定
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // バーコードの取得と設定
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    // 賞味期限の取得と設定
    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    // 日付の取得と設定
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // カテゴリの取得と設定
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}