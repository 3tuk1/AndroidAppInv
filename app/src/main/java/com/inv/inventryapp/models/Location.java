package com.inv.inventryapp.models;

import androidx.room.*;

@Entity(
        tableName = "locations",
        indices = {@Index(value = "item_id")},
        foreignKeys = @ForeignKey(
                entity = MainItem.class,
                parentColumns = "id",
                childColumns = "item_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Location {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "item_id")
    private int itemId;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // コンストラクタ
    public Location(int itemId, String location) {
        this.itemId = itemId;
        this.location = location;
        this.timestamp = System.currentTimeMillis();
    }

    // ゲッター・セッター
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}