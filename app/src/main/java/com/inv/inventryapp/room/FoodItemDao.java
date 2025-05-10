package com.inv.inventryapp.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.inv.inventryapp.models.FoodItem;

import java.util.List;

@Dao
public interface FoodItemDao {
    @Insert
    void insert(FoodItem foodItem);

    @Update
    void update(FoodItem foodItem);

    @Delete
    void delete(FoodItem foodItem);

    @Query("SELECT * FROM food_items")
    List<FoodItem> getAllFoodItems();

    @Query("SELECT * FROM food_items WHERE id = :id")
    FoodItem getFoodItemById(int id);
}