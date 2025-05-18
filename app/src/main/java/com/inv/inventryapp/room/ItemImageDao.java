package com.inv.inventryapp.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.inv.inventryapp.models.ItemImage;

import java.util.List;

public interface ItemImageDao {
    // Insert a new item image
    @Insert
    void insert(ItemImage itemImage);

    // Update an existing item image
    @Update
    void update(ItemImage itemImage);

    // Delete an item image
    @Delete
    void delete(ItemImage itemImage);

    // Get all item images
    @Query("SELECT * FROM images")
    List<ItemImage> getAllItemImages();

    // Get an item image by its ID
    @Query("SELECT * FROM images WHERE id = :id")
    ItemImage getItemImageById(int id);
}
