package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.lifecycle.LiveData;
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
    LiveData<List<Product>> getAll();

    @Query("SELECT * FROM product WHERE quantity > 0")
    List<Product> getAllList();

    @Query("SELECT * FROM product ")
    LiveData<List<Product>> getAllWithZeroQuantity();

    @Query("SELECT * FROM product WHERE productId = :id")
    Product findById(int id);

    @Query("SELECT * FROM product WHERE product_name = :name")
    Product findByName(String name);

    /**
     * バーコード番号を元に商品を検索します。
     * @param barcodeNumber 検索するバーコード番号
     * @return 見つかった商品。見つからない場合はnull
     */
    @Query("SELECT * FROM product WHERE barcode_barcode_number = :barcodeNumber LIMIT 1")
    Product findByBarcode(long barcodeNumber);

    @Query("DELETE FROM product")
    void deleteAll();
}
