package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;
import com.inv.inventryapp.model.entity.ShoppingList;

@Dao
public interface ShoppingListDao {
    @Insert
    void insert(ShoppingList shoppingList);

    @Update
    void update(ShoppingList shoppingList);

    @Delete
    void delete(ShoppingList shoppingList);

    @Query("SELECT * FROM shopping_list")
    List<ShoppingList> getAll();
}

