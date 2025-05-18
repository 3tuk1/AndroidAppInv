package com.inv.inventryapp.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
public class MainItemJoin {
    @Embedded
    public MainItem mainItem;

    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public List<ItemImage> images;
    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public Location location;

    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public List<Barcode> barcodes;

}
