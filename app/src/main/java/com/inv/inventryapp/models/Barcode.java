package com.inv.inventryapp.models;

import androidx.room.*;

@Entity(
        tableName = "barcodes",
        foreignKeys = @ForeignKey(
                entity = MainItem.class,
                parentColumns = "id",
                childColumns = "item_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Barcode {
    // item_idを主キーとして設定（1アイテム1バーコードを保証）
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    private int itemId;

    @ColumnInfo(name = "barcode_value")
    private String barcodeValue;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // コンストラクタ
    public Barcode(int itemId, String barcodeValue) {
        this.itemId = itemId;
        this.barcodeValue = barcodeValue;
        this.timestamp = System.currentTimeMillis();
    }

    // ゲッターとセッター
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getBarcodeValue() {
        return barcodeValue;
    }

    public void setBarcodeValue(String barcodeValue) {
        this.barcodeValue = barcodeValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

