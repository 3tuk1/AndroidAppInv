package com.inv.inventryapp.models;

import android.graphics.Bitmap;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "images",
        foreignKeys = @ForeignKey(
                entity = MainItem.class,
                parentColumns = "id",
                childColumns = "id",
                onDelete = ForeignKey.CASCADE
        )
)
public class ItemImage {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "image")
    private Bitmap image;

    // コンストラクタ
    public ItemImage(int id, Bitmap image) {
        this.id = id;
        this.image = image;
    }

    // ゲッター・セッター
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Bitmap getImage() { return image; }
    public void setImage(Bitmap image) { this.image = image; }
}