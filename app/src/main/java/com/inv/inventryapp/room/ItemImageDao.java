package com.inv.inventryapp.room;

import androidx.room.*;

import com.inv.inventryapp.models.ItemImage;
import com.inv.inventryapp.models.MainItemJoin;

import java.util.List;

@Dao
public interface ItemImageDao {
    @Insert
    void insert(ItemImage itemImage);

    @Update
    void update(ItemImage itemImage);

    @Delete
    void delete(ItemImage itemImage);

    @Query("SELECT * FROM item_images WHERE item_id = :itemId")
    List<ItemImage> getImagesForItem(int itemId);

    @Query("SELECT * FROM item_images")
    List<ItemImage> getAllImages();

    // item_idが一致する画像を取得
    @Query("SELECT * FROM item_images WHERE item_id = :itemId")
    ItemImage getImageByItemId(int itemId);

    // MainItemとitem_imageを結合するクエリ
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT m.*, i.* FROM main_items m LEFT JOIN item_images i ON m.id = i.item_id")
    List<MainItemJoin> getItemsWithItemImages();

    // 特定のアイテムとその場所情報を取得
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("SELECT m.*, i.* FROM main_items m LEFT JOIN item_images i ON m.id = i.item_id WHERE m.id = :itemId")
    MainItemJoin getItemWithItemImageById(int itemId);

}

