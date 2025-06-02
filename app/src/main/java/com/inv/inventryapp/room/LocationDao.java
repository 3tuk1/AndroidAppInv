package com.inv.inventryapp.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.RoomWarnings;

import com.inv.inventryapp.models.Location;
import com.inv.inventryapp.models.MainItemJoin;

import java.util.List;

@Dao
public interface LocationDao {
    // 既存のメソッド
    @Insert
    void insert(Location location);

    @Update
    void update(Location location);

    @Delete
    void delete(Location location);

    @Query("SELECT * FROM locations")
    List<Location> getAllLocations();

    @Query("SELECT * FROM locations WHERE id = :id")
    Location getLocationById(int id);

    // 追加するメソッド
    @Query("SELECT * FROM locations WHERE item_id = :itemId")
    Location getLocationByItemId(int itemId);

    // MainItemとLocationを結合するクエリ
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT m.*, l.* FROM main_items m LEFT JOIN locations l ON m.id = l.item_id")
    List<MainItemJoin> getItemsWithLocations();

    // 特定のアイテムとその場所情報を取得
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT m.*, l.* FROM main_items m LEFT JOIN locations l ON m.id = l.item_id WHERE m.id = :itemId")
    MainItemJoin getItemWithLocationById(int itemId);
}

