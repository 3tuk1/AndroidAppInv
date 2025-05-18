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
}
