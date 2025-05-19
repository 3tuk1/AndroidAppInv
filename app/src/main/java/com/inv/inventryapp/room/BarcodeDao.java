package com.inv.inventryapp.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.inv.inventryapp.models.Barcode;
import com.inv.inventryapp.models.MainItemJoin;

import java.util.List;

@Dao
public interface BarcodeDao {
    @Insert
    void insert(Barcode barcode);

    @Update
    void update(Barcode barcode);

    @Delete
    void delete(Barcode barcode);

    @Query("SELECT * FROM barcodes")
    List<Barcode> getAllBarcodes();

    @Query("SELECT * FROM barcodes WHERE id = :id")
    Barcode getBarcodeById(int id);

    @Query("SELECT * FROM barcodes WHERE item_id = :itemId")
    Barcode getBarcodesForItem(int itemId);

    @Query("SELECT * FROM barcodes WHERE barcode_value = :value")
    Barcode getBarcodeByValue(String value);

    // MainItemとBarcodeを結合するクエリ
    @Transaction
    @Query("SELECT m.*, b.* FROM main_items m LEFT JOIN barcodes b ON m.id = b.item_id")
    List<MainItemJoin> getItemsWithBarcodes();

    // 特定のアイテムとそのバーコード情報を取得
    @Transaction
    @Query("SELECT m.*, b.* FROM main_items m LEFT JOIN barcodes b ON m.id = b.item_id WHERE m.id = :itemId")
    MainItemJoin getItemWithBarcodeById(int itemId);

    // バーコード値からアイテムとバーコード情報を取得
    @Transaction
    @Query("SELECT m.*, b.* FROM main_items m JOIN barcodes b ON m.id = b.item_id WHERE b.barcode_value = :barcodeValue")
    MainItemJoin getItemByBarcodeValue(String barcodeValue);

    // 指定されたバーコード値が存在するかどうかを確認
    @Query("SELECT EXISTS(SELECT 1 FROM barcodes WHERE barcode_value = :barcodeValue LIMIT 1)")
    boolean existsBarcodeValue(String barcodeValue);
}