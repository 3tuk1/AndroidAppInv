package com.inv.inventryapp.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.inv.inventryapp.models.History;
import com.inv.inventryapp.models.PrioritizedItem;
import com.inv.inventryapp.models.ShoppingListItem;
import com.inv.inventryapp.repositories.AnalyticsRepository;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.CategoryDao;
import com.inv.inventryapp.room.HistoryDao;
import com.inv.inventryapp.room.ItemAnalyticsDataDao;
import com.inv.inventryapp.room.MainItemDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyticsViewModel extends AndroidViewModel {

    private AnalyticsRepository analyticsRepository;
    private MutableLiveData<Map<LocalDate, Integer>> internalConsumptionTrendData = new MutableLiveData<>();
    public LiveData<Map<LocalDate, Integer>> consumptionTrendData = internalConsumptionTrendData;

    private MutableLiveData<Map<String, Integer>> internalCategoryConsumptionData = new MutableLiveData<>();
    public LiveData<Map<String, Integer>> categoryConsumptionData = internalCategoryConsumptionData;

    private MutableLiveData<Double> internalWasteRateData = new MutableLiveData<>();
    public LiveData<Double> wasteRateData = internalWasteRateData;

    private MutableLiveData<Float> internalRemainingDaysPrediction = new MutableLiveData<>();
    public LiveData<Float> remainingDaysPredictionData = internalRemainingDaysPrediction;

    private MutableLiveData<Integer> internalRequiredRestockQuantity = new MutableLiveData<>();
    public LiveData<Integer> requiredRestockQuantityData = internalRequiredRestockQuantity;

    private MutableLiveData<List<PrioritizedItem>> internalPrioritizedPurchaseItems = new MutableLiveData<>();
    public LiveData<List<PrioritizedItem>> prioritizedPurchaseItemsData = internalPrioritizedPurchaseItems;

    private MutableLiveData<List<ShoppingListItem>> internalShoppingList = new MutableLiveData<>();
    public LiveData<List<ShoppingListItem>> shoppingListData = internalShoppingList;

    public LiveData<List<History>> allConsumptionHistory;

    private ExecutorService executorService;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        HistoryDao historyDao = db.historyDao();
        MainItemDao mainItemDao = db.mainItemDao();
        CategoryDao categoryDao = db.categoryDao();
        ItemAnalyticsDataDao itemAnalyticsDataDao = db.itemAnalyticsDataDao();
        analyticsRepository = new AnalyticsRepository(historyDao, mainItemDao, categoryDao, itemAnalyticsDataDao);
        executorService = Executors.newSingleThreadExecutor();
        allConsumptionHistory = analyticsRepository.getAllOutputAndDeleteHistorySortedDesc();
    }

    public void loadConsumptionTrend(LocalDate startDate, LocalDate endDate, AnalyticsRepository.TrendPeriod period) {
        executorService.execute(() -> {
            LiveData<Map<LocalDate, Integer>> liveData = analyticsRepository.getConsumptionTrendByPeriod(startDate, endDate, period);
            observeOnce(liveData, internalConsumptionTrendData);
        });
    }

    public void loadCategoryConsumptionTrend(LocalDate startDate, LocalDate endDate) {
        executorService.execute(() -> {
            LiveData<Map<String, Integer>> liveData = analyticsRepository.getCategoryConsumptionTrend(startDate, endDate);
            observeOnce(liveData, internalCategoryConsumptionData);
        });
    }

    public void loadWasteRate(LocalDate startDate, LocalDate endDate) {
        executorService.execute(() -> {
            LiveData<Double> liveData = analyticsRepository.getWasteRate(startDate, endDate);
            observeOnce(liveData, internalWasteRateData);
        });
    }

    public void loadRemainingDaysPrediction(int itemId) {
        executorService.execute(() -> {
            LiveData<Float> liveData = analyticsRepository.getRemainingDaysPrediction(itemId);
            observeOnce(liveData, internalRemainingDaysPrediction);
        });
    }

    public void loadRequiredRestockQuantity(int itemId, boolean useOptimalLevel) {
        executorService.execute(() -> {
            LiveData<Integer> liveData = analyticsRepository.getRequiredRestockQuantity(itemId, useOptimalLevel);
            observeOnce(liveData, internalRequiredRestockQuantity);
        });
    }

    /**
     * 全ての消費履歴を取得する
     * @return 消費履歴のLiveData
     */
    public LiveData<List<History>> getAllConsumptionHistory() {
        return allConsumptionHistory;
    }

    public void loadPrioritizedPurchaseItems() {
        executorService.execute(() -> {
            LiveData<List<PrioritizedItem>> liveData = analyticsRepository.getPrioritizedPurchaseItems();
            observeOnce(liveData, internalPrioritizedPurchaseItems);
        });
    }

    public void loadShoppingList() {
        executorService.execute(() -> {
            LiveData<List<ShoppingListItem>> liveData = analyticsRepository.generateShoppingList();
            observeOnce(liveData, internalShoppingList);
        });
    }

    // Helper method to observe LiveData once
    private <T> void observeOnce(LiveData<T> liveData, MutableLiveData<T> targetLiveData) {
        new Handler(Looper.getMainLooper()).post(() -> {
            liveData.observeForever(new Observer<T>() {
                @Override
                public void onChanged(T data) {
                    targetLiveData.postValue(data);
                    liveData.removeObserver(this);
                }
            });
        });
    }

    // 指定された日付の消費予測を取得するメソッド
    public LiveData<Double> getPredictedConsumptionForDate(LocalDate date) {
        return analyticsRepository.getDailyConsumptionPrediction(date);
    }

    /**
     * 買い物リストからアイテムを削除する（実際には推奨購入量を0にするようリポジトリに依頼）。
     * @param item 削除するShoppingListItem
     */
    public void deleteShoppingListItem(ShoppingListItem item) {
        if (item != null && item.getMainItem() != null) {
            executorService.execute(() -> {
                analyticsRepository.removeItemFromShoppingListRecommendations(item.getMainItem().getId());
                loadShoppingList();
            });
        }
    }

    /**
     * 買い物リストアイテムの購入数量を更新する。
     * @param itemId 更新対象のアイテムID
     * @param newQuantity 新しい購入数量
     */
    public void updateShoppingListItemQuantity(int itemId, int newQuantity) {
        executorService.execute(() -> {
            analyticsRepository.updateUserDefinedPurchaseQuantity(itemId, newQuantity);
            loadShoppingList();
        });
    }

    /**
     * 特定のアイテムの平均消費日数を更新するようリポジトリに依頼する
     * @param itemId 対象のアイテムID
     */
    public void triggerUpdateAverageConsumptionDays(int itemId) {
        executorService.execute(() -> {
            analyticsRepository.updateAverageConsumptionDaysForItem(itemId);
        });
    }

    /**
     * 買い物リストデータを取得するためのアクセサメソッド
     * @return 買い物リストのLiveData
     */
    public LiveData<List<ShoppingListItem>> getShoppingListData() {
        return shoppingListData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
