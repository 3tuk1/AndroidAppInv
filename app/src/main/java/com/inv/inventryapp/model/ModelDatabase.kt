package com.inv.inventryapp.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.inv.inventryapp.model.dao.*
import com.inv.inventryapp.model.entity.*
import com.inv.inventryapp.model.room.Converters

@Database(
    entities = [
        Product::class,
        Barcode::class,
        ProductCategory::class,
        History::class,
        Analysis::class,
        ShoppingList::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ModelDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun barcodeDao(): BarcodeDao
    abstract fun productCategoryDao(): ProductCategoryDao
    abstract fun historyDao(): HistoryDao
    abstract fun analysisDao(): AnalysisDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        @Volatile
        private var INSTANCE: ModelDatabase? = null

        fun getInstance(context: Context): ModelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ModelDatabase::class.java, "model_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

