package com.inv.inventryapp.models;

import java.util.Date;

public class FoodItem {
    private String name;
    private String barcode;
    private Date expiryDate;
    private int quantity;
    private String category;

    public FoodItem(String name, String barcode, Date expiryDate, int quantity, String category) {
        this.name = name;
        this.barcode = barcode;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.category = category;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}