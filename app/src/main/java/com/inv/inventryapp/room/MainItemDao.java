package com.inv.inventryapp.room;

import androidx.room.*;
import com.inv.inventryapp.models.MainItem;
import com.inv.inventryapp.models.MainItemJoin;

import java.util.List;

@Dao
public interface MainItemDao {
     @Insert
     long insert(MainItem mainItem);

     @Update
     void update(MainItem mainItem);

     @Delete
     void delete(MainItem mainItem);

     @Query("SELECT * FROM main_items")
     List<MainItem> getAllMainItems();

     @Query("SELECT * FROM main_items WHERE id = :id")
     MainItem getMainItemById(int id);

     @Transaction
     @Query("SELECT * FROM main_items")
     List<MainItemJoin> getMainItemWithImagesAndLocation();

     @Transaction
     @Query("SELECT * FROM main_items WHERE id = :id")
     MainItemJoin getMainItemWithImagesAndLocationById(int id);

     @Query("UPDATE main_items SET quantity = 0 WHERE id = :id")
     void setQuantityZero(int id);

     @Transaction
     @Query("SELECT * FROM main_items WHERE quantity > 0")
     List<MainItemJoin> getMainItemWithImagesAndLocationOnlyPositive();
}
