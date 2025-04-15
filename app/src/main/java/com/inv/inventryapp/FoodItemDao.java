package com.inv.inventryapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FoodItem foodItem);

    @Update
    void update(FoodItem foodItem);

    @Query("SELECT * FROM food_items WHERE isDeleted = 0")
    List<FoodItem> getAllFoodItems();

    @Query("SELECT * FROM food_items WHERE category = :category AND isDeleted = 0")
    List<FoodItem> getFoodItemsByCategory(String category);

    @Query("SELECT * FROM food_items WHERE expiryDate BETWEEN :today AND :weekLater AND isDeleted = 0")
    List<FoodItem> getFoodItemsNearExpiry(String today, String weekLater);

    @Query("SELECT * FROM food_items WHERE barcode = :barcode AND isDeleted = 0")
    List<FoodItem> getFoodItemsByBarcode(String barcode);

    // FoodItemDao.javaに追加するコード

    @Query("SELECT * FROM food_items WHERE id = :id AND isDeleted = 0")
    List<FoodItem> getFoodItemById(String id);
}