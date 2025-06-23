package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;
import com.inv.inventryapp.model.entity.ShoppingList;
import androidx.lifecycle.LiveData;

@Dao
public interface ShoppingListDao {
    @Insert
    void insert(ShoppingList shoppingList);

    @Update
    void update(ShoppingList shoppingList);

    @Delete
    void delete(ShoppingList shoppingList);

    @Query("SELECT * FROM shopping_list ORDER BY priority ASC")
    LiveData<List<ShoppingList>> getAll();

    @Query("SELECT * FROM shopping_list")
    List<ShoppingList> getAllList();

    // 追加: 商品名で検索して存在を確認する
    @Query("SELECT * FROM shopping_list WHERE product_name = :productName LIMIT 1")
    ShoppingList findByProductName(String productName);
}
