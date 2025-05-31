package com.inv.inventryapp.models;

import androidx.room.*;

@Entity(
        tableName = "barcodes",
        indices = {@Index(value = "item_id")},
        foreignKeys = @ForeignKey(
                entity = MainItem.class,
                parentColumns = "id",
                childColumns = "item_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Barcode {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

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

    // ゲッター・セッター
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getBarcodeValue() { return barcodeValue; }

    public void setBarcodeValue(String barcodeValue) { this.barcodeValue = barcodeValue; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}