package com.inv.inventryapp.room;

import androidx.room.*;
import com.inv.inventryapp.models.HiddenItem;
import java.util.List;

@Dao
public interface HiddenItemDao {
    @Insert
    void insert(HiddenItem hiddenItem);

    @Delete
    void delete(HiddenItem hiddenItem);

    @Query("SELECT * FROM hidden_items")
    List<HiddenItem> getAll();

    @Query("SELECT * FROM hidden_items WHERE item_id = :itemId LIMIT 1")
    HiddenItem getByItemId(int itemId);

    @Query("DELETE FROM hidden_items WHERE item_id = :itemId")
    void deleteByItemId(int itemId);
}

