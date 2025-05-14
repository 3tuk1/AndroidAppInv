package com.inv.inventryapp.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.inv.inventryapp.models.MainItem;

import java.util.List;

public interface MainItemDao {
     @Insert
     void insert(MainItem mainItem);

     @Update
     void update(MainItem mainItem);

     @Delete
     void delete(MainItem mainItem);

     @Query("SELECT * FROM main_items")
     List<MainItem> getAllMainItems();

     @Query("SELECT * FROM main_items WHERE id = :id")
     MainItem getMainItemById(int id);
}
