package com.inv.inventryapp.model;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.inv.inventryapp.model.entity.*;
import com.inv.inventryapp.model.dao.*;

@Database(entities = {
        Product.class,
        Barcode.class,
        ProductCategory.class,
        History.class,
        Analysis.class,
        ShoppingList.class
    }, version = 1, exportSchema = false)
public abstract class ModelDatabase extends RoomDatabase {
    private static volatile ModelDatabase INSTANCE;

    public abstract ProductDao productDao();
    public abstract BarcodeDao barcodeDao();
    public abstract ProductCategoryDao productCategoryDao();
    public abstract HistoryDao historyDao();
    public abstract AnalysisDao analysisDao();
    public abstract ShoppingListDao shoppingListDao();

    public static ModelDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ModelDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ModelDatabase.class, "model_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

