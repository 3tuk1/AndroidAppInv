package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;
import com.inv.inventryapp.model.entity.ProductCategory;

@Dao
public interface ProductCategoryDao {
    @Insert
    void insert(ProductCategory category);

    @Update
    void update(ProductCategory category);

    @Delete
    void delete(ProductCategory category);

    @Query("SELECT * FROM product_category")
    List<ProductCategory> getAll();
}

