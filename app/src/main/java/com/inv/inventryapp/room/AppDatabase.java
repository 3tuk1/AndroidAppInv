package com.inv.inventryapp.room;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.inv.inventryapp.models.*;

@Database(
        entities = {
                MainItem.class,
                ItemImage.class,
                Location.class,
                Barcode.class  // バーコードエンティティを追加
        },
        version = 5,  // バージョンを上げる
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract MainItemDao mainItemDao();
    public abstract ItemImageDao itemImageDao();
    public abstract LocationDao locationDao();
    public abstract BarcodeDao barcodeDao();  // バーコードDAOのメソッドを追加

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "inventory_database")
                    .fallbackToDestructiveMigration()  // データベース構造変更時に再作成
                    .build();
        }
        return instance;
    }

    public void DeleateId(int id) {
        // データベースから特定のIDを持つアイテムを削除するメソッド
        MainItemJoin mainItem = mainItemDao().getMainItemWithImagesAndLocationById(id);
        if (mainItem != null) {
            // 画像が存在する場合は削除
            if (mainItem.images != null) {
                for (ItemImage image : mainItem.images) {
                    itemImageDao().delete(image);
                }
            }
            // バーコードが存在する場合は削除
            if (mainItem.barcodes != null) {
                for (Barcode barcode : mainItem.barcodes) {
                    barcodeDao().delete(barcode);
                }
            }
            // ロケーションが存在する場合は削除
            if (mainItem.location != null) {
                locationDao().delete(mainItem.location);
            }
            // 最後にメインアイテムを削除
            mainItemDao().delete(mainItem.mainItem);
        }
    }
}