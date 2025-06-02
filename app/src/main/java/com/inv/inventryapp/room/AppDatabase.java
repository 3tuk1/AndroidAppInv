package com.inv.inventryapp.room;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.inv.inventryapp.models.*;

import java.time.LocalDate;
import java.util.concurrent.Executors;

@Database(
        entities = {
                MainItem.class,
                ItemImage.class,
                Location.class,
                Barcode.class,  // バーコードエンティティを追加
                Category.class,  // カテゴリエンティティを追加
                HiddenItem.class,  // 非表示管理エンティティを追加
                History.class,  // 履歴エンティティを追加
                ItemAnalyticsData.class // 解析データエンティティを追加
        },
        version = 19,  // バージョンを18に上げる（dataPointCountフィールド追加対応）
        exportSchema = false
)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract MainItemDao mainItemDao();
    public abstract ItemImageDao itemImageDao();
    public abstract LocationDao locationDao();
    public abstract BarcodeDao barcodeDao();  // バーコードDAOのメソッドを追加
    public abstract CategoryDao categoryDao();  // カテゴリーDAOのメソッドを追加
    public abstract HiddenItemDao hiddenItemDao();  // 非表示DAOのメソッドを追加
    public abstract HistoryDao historyDao();  // 履歴DAOのメソッドを追加
    public abstract ItemAnalyticsDataDao itemAnalyticsDataDao(); // 解析データDAOのメソッドを追加

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "inventory_database")
                    .fallbackToDestructiveMigration()
                    .build();
            // カテゴリー初期化処理
            Executors.newSingleThreadExecutor().execute(() -> {
                CategoryDao categoryDao = instance.categoryDao();
                if (categoryDao.getAllCategories().isEmpty()) {
                    categoryDao.insert(new Category("肉類"));
                    categoryDao.insert(new Category("魚介類"));
                    categoryDao.insert(new Category("野菜"));
                    categoryDao.insert(new Category("果物"));
                    categoryDao.insert(new Category("乳製品"));
                    categoryDao.insert(new Category("穀物"));
                    categoryDao.insert(new Category("飲料"));
                    categoryDao.insert(new Category("調味料"));
                    categoryDao.insert(new Category("冷凍食品"));
                    categoryDao.insert(new Category("菓子類"));
                    categoryDao.insert(new Category("日用品"));
                    categoryDao.insert(new Category("ペット用品"));
                    categoryDao.insert(new Category("医薬品"));
                    categoryDao.insert(new Category("化粧品"));
                    categoryDao.insert(new Category("雑貨"));
                    categoryDao.insert(new Category("その他"));
                }
            });
        }
        return instance;
    }

    public void DeleateId(int id) {
        // データベースから特定のIDを持つアイテムを削除するメソッド
        MainItemJoin mainItem = mainItemDao().getMainItemWithImagesAndLocationById(id);
        if (mainItem != null) {
            // 数量を0にして非表示に追加
            mainItemDao().setQuantityZero(mainItem.mainItem.getId());
            HiddenItem hiddenItem = new HiddenItem(mainItem.mainItem.getId());
            hiddenItemDao().insert(hiddenItem);
            // 最後にメインアイテムの数量を0にする
            mainItemDao().setQuantityZero(mainItem.mainItem.getId());
            historyDao().insert(new History(
                    mainItem.mainItem.getId(),
                    0,
                    "delete", // typeは "delete" のまま
                    LocalDate.now(),
                    "廃棄" // consumptionReason に "廃棄" を指定
            ));
        }
    }
}

