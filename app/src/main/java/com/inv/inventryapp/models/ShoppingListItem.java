package com.inv.inventryapp.models;

public class ShoppingListItem {
    public MainItem mainItem;
    public int quantityToBuy;
    public String reason; // なぜ購入リストに入ったかの理由（例: "在庫僅少", "最低在庫レベル以下"）

    public ShoppingListItem(MainItem mainItem, int quantityToBuy, String reason) {
        this.mainItem = mainItem;
        this.quantityToBuy = quantityToBuy;
        this.reason = reason;
    }

    // Getters
    public MainItem getMainItem() {
        return mainItem;
    }

    public int getQuantityToBuy() {
        return quantityToBuy;
    }

    public String getReason() {
        return reason;
    }
}

