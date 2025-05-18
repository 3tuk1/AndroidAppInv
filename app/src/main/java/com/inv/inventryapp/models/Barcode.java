package com.inv.inventryapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

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
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "item_id")
    private int itemId;

    @ColumnInfo(name = "barcode_value")
    private String barcodeValue;

    @ColumnInfo(name = "barcode_type")
    private String barcodeType;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // コンストラクタ
    public Barcode(int itemId, String barcodeValue, String barcodeType) {
        this.itemId = itemId;
        this.barcodeValue = barcodeValue;
        this.barcodeType = barcodeType;
        this.timestamp = System.currentTimeMillis();
    }

    // ゲッター・セッター
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getBarcodeValue() { return barcodeValue; }
    public void setBarcodeValue(String barcodeValue) { this.barcodeValue = barcodeValue; }

    public String getBarcodeType() { return barcodeType; }
    public void setBarcodeType(String barcodeType) { this.barcodeType = barcodeType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}