package com.inv.inventryapp.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.RewriteQueriesToDropUnusedColumns;

import com.inv.inventryapp.models.Barcode;
import com.inv.inventryapp.models.MainItemJoin;

import java.util.List;

@Dao
public interface BarcodeDao {
    // 競合時は置き換え（item_idが主キーなので同じアイテムのバーコードを更新する場合）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Barcode barcode);

    @Update
    void update(Barcode barcode);

    @Delete
    void delete(Barcode barcode);

    @Query("SELECT * FROM barcodes")
    List<Barcode> getAllBarcodes();

    @Query("SELECT * FROM barcodes WHERE item_id = :itemId")
    Barcode getBarcodesForItem(int itemId);

    // バーコード値でバーコードを検索（単純化）
    @Query("SELECT * FROM barcodes WHERE barcode_value = :value")
    Barcode getBarcodeByValue(String value);

    // MainItemとBarcodeを結合するクエリ（変更なし）
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT m.*, b.* FROM main_items m LEFT JOIN barcodes b ON m.id = b.item_id")
    List<MainItemJoin> getItemsWithBarcodes();

    // 特定のアイテムとそのバーコード情報を取得（変更なし）
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT m.*, b.* FROM main_items m LEFT JOIN barcodes b ON m.id = b.item_id WHERE m.id = :itemId")
    MainItemJoin getItemWithBarcodeById(int itemId);

    // バーコード値からアイテムとバーコード情報を取得（単純化）
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT m.*, b.* FROM main_items m JOIN barcodes b ON m.id = b.item_id WHERE b.barcode_value = :barcodeValue")
    MainItemJoin getItemByBarcodeValue(String barcodeValue);

    // バーコード値の存在チェック（変更なし）
    @Query("SELECT EXISTS(SELECT 1 FROM barcodes WHERE barcode_value = :barcodeValue LIMIT 1)")
    boolean existsBarcodeValue(String barcodeValue);

    // 特定のバーコード値を持つすべてのバーコードを削除（バーコード重複クリーンアップ用）
    @Query("DELETE FROM barcodes WHERE barcode_value = :value AND item_id != :exceptItemId")
    void deleteAllBarcodesWithValueExcept(String value, int exceptItemId);
}
