package com.inv.inventryapp;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodRepository {
    private static final String TAG = "FoodRepository";
    private final FoodItemDao localDb;
    private final ExecutorService executor;
    private static FoodRepository instance;

    private FoodRepository(Context context) {
        AppDatabase database = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class, "food_database")
                .build();
        localDb = database.foodItemDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized FoodRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FoodRepository(context);
        }
        return instance;
    }

    // 食品を追加
    public void addFood(FoodItem item, OnFoodItemAddedCallback callback) {
        // ローカルDBに保存
        executor.execute(() -> {
            try {
                long localId = localDb.insert(item);
                // IDを設定（Roomでは自動生成されたIDを使用）
                item.setId(String.valueOf(localId));
                callback.onSuccess(item);
            } catch (Exception e) {
                Log.e(TAG, "Error adding food item", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // 全ての食品を取得
    public void getAllFood(OnFoodItemsLoadedCallback callback) {
        executor.execute(() -> {
            try {
                List<FoodItem> localItems = localDb.getAllFoodItems();
                callback.onLoaded(localItems);
            } catch (Exception e) {
                Log.e(TAG, "Error getting all food items", e);
                callback.onLoaded(new ArrayList<>());
            }
        });
    }

    // カテゴリーで絞り込み
    public void getFoodByCategory(String category, OnFoodItemsLoadedCallback callback) {
        executor.execute(() -> {
            try {
                List<FoodItem> localItems = localDb.getFoodItemsByCategory(category);
                callback.onLoaded(localItems);
            } catch (Exception e) {
                Log.e(TAG, "Error getting food by category", e);
                callback.onLoaded(new ArrayList<>());
            }
        });
    }

    // 賞味期限が近い食品を取得
    public void getNearExpiryFood(OnFoodItemsLoadedCallback callback) {
        executor.execute(() -> {
            try {
                String today = java.time.LocalDate.now().toString();
                String weekLater = java.time.LocalDate.now().plusDays(7).toString();
                List<FoodItem> localItems = localDb.getFoodItemsNearExpiry(today, weekLater);
                callback.onLoaded(localItems);
            } catch (Exception e) {
                Log.e(TAG, "Error getting near expiry food", e);
                callback.onLoaded(new ArrayList<>());
            }
        });
    }

    // 食品を更新
    public void updateFood(FoodItem item) {
        item.setLastModified(new Date());

        executor.execute(() -> {
            try {
                localDb.update(item);
            } catch (Exception e) {
                Log.e(TAG, "Error updating food item", e);
            }
        });
    }

    // バーコードで検索
    public void getFoodByBarcode(String barcode, OnFoodItemsLoadedCallback callback) {
        executor.execute(() -> {
            try {
                List<FoodItem> localItems = localDb.getFoodItemsByBarcode(barcode);
                callback.onLoaded(localItems);
            } catch (Exception e) {
                Log.e(TAG, "Error getting food by barcode", e);
                callback.onLoaded(new ArrayList<>());
            }
        });
    }

    // FoodRepository.javaに追加するコード

    public interface OnFoodItemLoadedCallback {
        void onLoaded(FoodItem item);
    }

    public void getFoodById(String foodId, OnFoodItemLoadedCallback callback) {
        executor.execute(() -> {
            try {
                List<FoodItem> localItems = localDb.getFoodItemById(foodId);
                if (localItems.isEmpty()) {
                    callback.onLoaded(null);
                } else {
                    callback.onLoaded(localItems.get(0));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting food by ID", e);
                callback.onLoaded(null);
            }
        });
    }

    // コールバックインターフェース
    public interface OnFoodItemAddedCallback {
        void onSuccess(FoodItem item);
        void onError(String errorMessage);
    }

    public interface OnFoodItemsLoadedCallback {
        void onLoaded(List<FoodItem> items);
    }
}