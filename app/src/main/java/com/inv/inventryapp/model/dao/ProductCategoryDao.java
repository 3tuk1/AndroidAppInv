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

    // カテゴリ名からカテゴリを取得
    @Query("SELECT * FROM product_category WHERE name = :name LIMIT 1")
    ProductCategory getByName(String name);

    // カテゴリ名の存在チェック
    @Query("SELECT EXISTS(SELECT 1 FROM product_category WHERE name = :name)")
    boolean exists(String name);
}

