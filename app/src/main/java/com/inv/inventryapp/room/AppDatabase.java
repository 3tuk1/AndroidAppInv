package com.inv.inventryapp.room;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.room.TypeConverters;
import com.inv.inventryapp.models.FoodItem;

@Database(entities = {FoodItem.class}, version = 1)
@TypeConverters({Converters.class}) // コンバータを登録
public abstract class AppDatabase extends RoomDatabase {
    public abstract FoodItemDao foodItemDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}