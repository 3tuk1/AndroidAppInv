package com.inv.inventryapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "hidden_items")
public class HiddenItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "item_id")
    public int itemId;

    public HiddenItem(int itemId) {
        this.itemId = itemId;
    }
}

