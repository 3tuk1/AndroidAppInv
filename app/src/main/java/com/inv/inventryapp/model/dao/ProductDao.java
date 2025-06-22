package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;
import com.inv.inventryapp.model.entity.Product;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM product WHERE quantity > 0")
    List<Product> getAll();

    @Query("SELECT * FROM product WHERE productId = :id")
    Product findById(int id);

    @Query("SELECT * FROM product WHERE product_name = :name")
    Product findByName(String name);
}
