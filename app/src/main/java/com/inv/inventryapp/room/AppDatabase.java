package com.inv.inventryapp.room;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.inv.inventryapp.models.Barcode;
import com.inv.inventryapp.models.ItemImage;
import com.inv.inventryapp.models.Location;
import com.inv.inventryapp.models.MainItem;

@Database(
        entities = {
                MainItem.class,
                ItemImage.class,
                Location.class,
                Barcode.class  // バーコードエンティティを追加
        },
        version = 4,  // バージョンを上げる
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
}