package com.inv.inventryapp.model.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.time.LocalDate;

@Entity(tableName = "product")
public class Product {
    /**
     * 商品エンティティ
     * 商品の情報を保持するクラス
     * 各フィールドはデータベースのカラムに対応しており、
     * 主キーとしてproductIdを使用します。
     * 各商品は、商品名、価格、数量、保管場所、賞味期限、購入日、画像パス、
     * カテゴリID、バーコードIDを持ちます。
     * カテゴリとバーコードの情報は、別のエンティティとして
     * 結合されており、
     * それぞれProductCategoryとBarcodeとして定義されています。
     */
    @PrimaryKey(autoGenerate = true)
    private int productId;
    @ColumnInfo(name = "product_name")
    private String productName;
    @ColumnInfo(name = "price")
    private int price;
    @ColumnInfo(name = "quantity")
    private int quantity;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "expiration_date")
    private LocalDate expirationDate;
    @ColumnInfo(name = "purchase_date")
    private LocalDate purchaseDate;
    @ColumnInfo(name = "image_path")
    private String imagePath;
    @ColumnInfo(name = "category_id")
    private int categoryId;
    @ColumnInfo(name = "barcode_id")
    private int barcodeId;

    // 結合情報
    @Embedded(prefix = "category_")
    private ProductCategory category;
    @Embedded(prefix = "barcode_")
    private Barcode barcode;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(int barcodeId) {
        this.barcodeId = barcodeId;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Barcode getBarcode() {
        return barcode;
    }

    public void setBarcode(Barcode barcode) {
        this.barcode = barcode;
    }
}
