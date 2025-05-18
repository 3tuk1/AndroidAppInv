package com.inv.inventryapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "item_images",
        foreignKeys = @ForeignKey(
                entity = MainItem.class,
                parentColumns = "id",
                childColumns = "item_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class ItemImage {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "item_id")
    private int itemId;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // コンストラクタ
    public ItemImage(int itemId, String imagePath) {
        this.itemId = itemId;
        this.imagePath = imagePath;
        this.timestamp = System.currentTimeMillis();
    }

    // ゲッター・セッター
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}