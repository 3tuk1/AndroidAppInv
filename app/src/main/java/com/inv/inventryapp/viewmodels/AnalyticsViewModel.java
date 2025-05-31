package com.inv.inventryapp.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.inv.inventryapp.models.History;
import com.inv.inventryapp.models.PrioritizedItem; // PrioritizedItem をインポート
import com.inv.inventryapp.models.ShoppingListItem; // ShoppingListItem をインポート
import com.inv.inventryapp.repositories.AnalyticsRepository;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.CategoryDao;
import com.inv.inventryapp.room.HistoryDao;
import com.inv.inventryapp.room.ItemAnalyticsDataDao;
import com.inv.inventryapp.room.MainItemDao;

import java.time.LocalDate;
import java.util.List; // List をインポート
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyticsViewModel extends AndroidViewModel {

    private AnalyticsRepository analyticsRepository;
    private LiveData<Map<LocalDate, Integer>> consumptionTrendData;
    private LiveData<Map<String, Integer>> categoryConsumptionData;
    private MutableLiveData<Map<LocalDate, Integer>> internalConsumptionTrendData = new MutableLiveData<>();
    private MutableLiveData<Map<String, Integer>> internalCategoryConsumptionData = new MutableLiveData<>();

    // For Waste Rate
    private MutableLiveData<Double> internalWasteRateData = new MutableLiveData<>();
    private LiveData<Double> wasteRateData;

    // For Remaining Days Prediction
    private MutableLiveData<Float> internalRemainingDaysPrediction = new MutableLiveData<>();
    private LiveData<Float> remainingDaysPredictionData;

    // For Required Restock Quantity
    private MutableLiveData<Integer> internalRequiredRestockQuantity = new MutableLiveData<>();
    private LiveData<Integer> requiredRestockQuantityData;

    // For Prioritized Purchase Items
    private MutableLiveData<List<PrioritizedItem>> internalPrioritizedPurchaseItems = new MutableLiveData<>();
    private LiveData<List<PrioritizedItem>> prioritizedPurchaseItemsData;

    // For Shopping List
    private MutableLiveData<List<ShoppingListItem>> internalShoppingList = new MutableLiveData<>();
    private LiveData<List<ShoppingListItem>> shoppingListData;

    // For All Consumption History
    private LiveData<List<History>> allConsumptionHistory;

    private ExecutorService executorService;

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        HistoryDao historyDao = db.historyDao();
        MainItemDao mainItemDao = db.mainItemDao();
        CategoryDao categoryDao = db.categoryDao();
        ItemAnalyticsDataDao itemAnalyticsDataDao = db.itemAnalyticsDataDao();
        analyticsRepository = new AnalyticsRepository(historyDao, mainItemDao, categoryDao, itemAnalyticsDataDao);

        consumptionTrendData = internalConsumptionTrendData;
        categoryConsumptionData = internalCategoryConsumptionData;
        wasteRateData = internalWasteRateData;
        remainingDaysPredictionData = internalRemainingDaysPrediction; // Initialize
        requiredRestockQuantityData = internalRequiredRestockQuantity; // Initialize
        prioritizedPurchaseItemsData = internalPrioritizedPurchaseItems; // Initialize
        shoppingListData = internalShoppingList; // Initialize
        executorService = Executors.newSingleThreadExecutor();

        // Initialize LiveData for all consumption history
        allConsumptionHistory = analyticsRepository.getAllConsumptionHistory();
    }

    public LiveData<Map<LocalDate, Integer>> getConsumptionTrendData() {
        return consumptionTrendData;
    }

    public LiveData<Map<String, Integer>> getCategoryConsumptionData() {
        return categoryConsumptionData;
    }

    public LiveData<Double> getWasteRateData() {
        return wasteRateData;
    }

    public LiveData<Float> getRemainingDaysPredictionData() { // Getter
        return remainingDaysPredictionData;
    }

    public LiveData<Integer> getRequiredRestockQuantityData() { // Getter
        return requiredRestockQuantityData;
    }

    public LiveData<List<PrioritizedItem>> getPrioritizedPurchaseItemsData() { // Getter
        return prioritizedPurchaseItemsData;
    }

    public LiveData<List<ShoppingListItem>> getShoppingListData() { // Getter
        return shoppingListData;
    }

    public LiveData<List<History>> getAllConsumptionHistory() {
        return allConsumptionHistory;
    }

    public void loadConsumptionTrend(LocalDate startDate, LocalDate endDate, AnalyticsRepository.TrendPeriod period) {
        executorService.execute(() -> {
            LiveData<Map<LocalDate, Integer>> liveData = analyticsRepository.getConsumptionTrendByPeriod(startDate, endDate, period);
            new Handler(Looper.getMainLooper()).post(() -> {
                liveData.observeForever(data -> {
                    internalConsumptionTrendData.setValue(data);
                });
            });
        });
    }

    public void loadCategoryConsumptionTrend(LocalDate startDate, LocalDate endDate) {
        executorService.execute(() -> {
            LiveData<Map<String, Integer>> liveData = analyticsRepository.getCategoryConsumptionTrend(startDate, endDate);
            new Handler(Looper.getMainLooper()).post(() -> {
                liveData.observeForever(data -> {
                    internalCategoryConsumptionData.setValue(data);
                });
            });
        });
    }

    public void loadWasteRate(LocalDate startDate, LocalDate endDate) {
        executorService.execute(() -> {
            LiveData<Double> liveData = analyticsRepository.getWasteRate(startDate, endDate);
            new Handler(Looper.getMainLooper()).post(() -> {
                liveData.observeForever(data -> {
                    internalWasteRateData.setValue(data);
                });
            });
        });
    }

    public void loadRemainingDaysPrediction(int itemId) {
        executorService.execute(() -> {
            analyticsRepository.getRemainingDaysPrediction(itemId).observeForever(data -> {
                internalRemainingDaysPrediction.postValue(data);
            });
        });
    }

    public void loadRequiredRestockQuantity(int itemId, boolean useOptimalLevel) {
        executorService.execute(() -> {
            analyticsRepository.getRequiredRestockQuantity(itemId, useOptimalLevel).observeForever(data -> {
                internalRequiredRestockQuantity.postValue(data);
            });
        });
    }

    public void loadPrioritizedPurchaseItems() {
        executorService.execute(() -> {
            analyticsRepository.getPrioritizedPurchaseItems().observeForever(data -> {
                internalPrioritizedPurchaseItems.postValue(data);
            });
        });
    }

    public void loadShoppingList() {
        executorService.execute(() -> {
            // バックグラウンドスレッドでLiveDataを取得
            LiveData<List<ShoppingListItem>> shoppingListLiveData = analyticsRepository.generateShoppingList();

            // メインスレッドでobserveForeverを呼び出す
            new Handler(Looper.getMainLooper()).post(() -> {
                shoppingListLiveData.observeForever(data -> {
                    internalShoppingList.postValue(data);
                });
            });
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

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
