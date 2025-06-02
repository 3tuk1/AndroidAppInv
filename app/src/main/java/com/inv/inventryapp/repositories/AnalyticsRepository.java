package com.inv.inventryapp.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
// MutableLiveData をインポート
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.inv.inventryapp.models.*;
// PrioritizedItem をインポート
// ShoppingListItem をインポート
import com.inv.inventryapp.room.CategoryDao;
import com.inv.inventryapp.room.HistoryDao;
import com.inv.inventryapp.room.ItemAnalyticsDataDao; // ItemAnalyticsDataDao をインポート
import com.inv.inventryapp.room.MainItemDao;

import java.time.LocalDate;
// Collections をインポート
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AnalyticsRepository {

    private HistoryDao historyDao;
    private MainItemDao mainItemDao;
    private CategoryDao categoryDao;
    private ItemAnalyticsDataDao itemAnalyticsDataDao; // ItemAnalyticsDataDao フィールドを追加
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // バックグラウンド処理用のExecutorService

    public AnalyticsRepository(HistoryDao historyDao, MainItemDao mainItemDao, CategoryDao categoryDao, ItemAnalyticsDataDao itemAnalyticsDataDao) { // コンストラクタに ItemAnalyticsDataDao を追加
        this.historyDao = historyDao;
        this.mainItemDao = mainItemDao;
        this.categoryDao = categoryDao;
        this.itemAnalyticsDataDao = itemAnalyticsDataDao; // ItemAnalyticsDataDao を初期化
    }

    // Enum for defining the aggregation period
    public enum TrendPeriod {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    /**
     * 消費予測の信頼度レベルを定義するenum
     */
    public enum PredictionConfidenceLevel {
        HIGH,    // 高信頼度 (多くの過去データに基づく)
        MEDIUM,  // 中信頼度 (一定の過去データに基づく)
        LOW,     // 低信頼度 (少ないデータに基づく)
        UNKNOWN  // 信頼度不明 (データ不足)
    }

    /**
     * 消費予測の詳細情報を格納するクラス
     */
    public static class ConsumptionPredictionDetail {
        private double predictedAmount;  // 予測消費量
        private PredictionConfidenceLevel confidenceLevel;  // 予測の信頼度
        private String statusMessage;    // 予測状態メッセージ
        private int daysUntilStockout;   // 在庫切れまでの日数（予測）
        private boolean isUrgent;        // 緊急フラグ（すぐに消費される予測）

        public ConsumptionPredictionDetail(double predictedAmount, PredictionConfidenceLevel confidenceLevel,
                                           String statusMessage, int daysUntilStockout, boolean isUrgent) {
            this.predictedAmount = predictedAmount;
            this.confidenceLevel = confidenceLevel;
            this.statusMessage = statusMessage;
            this.daysUntilStockout = daysUntilStockout;
            this.isUrgent = isUrgent;
        }

        // Getterメソッド
        public double getPredictedAmount() { return predictedAmount; }
        public PredictionConfidenceLevel getConfidenceLevel() { return confidenceLevel; }
        public String getStatusMessage() { return statusMessage; }
        public int getDaysUntilStockout() { return daysUntilStockout; }
        public boolean isUrgent() { return isUrgent; }
    }

    /**
     * 日別/週別/月別の消費傾向データを取得する
     * @param startDate 開始日
     * @param endDate 終了日
     * @param period 集計単位 (DAILY, WEEKLY, MONTHLY)
     * @return LiveData<Map<LocalDate, Integer>> 各期間の開始日と消費量のマップ
     */
    public LiveData<Map<LocalDate, Integer>> getConsumptionTrendByPeriod(LocalDate startDate, LocalDate endDate, TrendPeriod period) {
        return Transformations.map(historyDao.getOutputHistoryByDateRange(startDate, endDate), historyList -> {
            Map<LocalDate, Integer> trendMap = new HashMap<>();
            if (historyList == null || historyList.isEmpty()) {
                return trendMap;
            }

            switch (period) {
                case DAILY:
                    // 日毎に集計
                    trendMap = historyList.stream()
                            .collect(Collectors.groupingBy(
                                    History::getDate,
                                    Collectors.summingInt(History::getQuantity)
                            ));
                    break;
                case WEEKLY:
                    // 週毎に集計 (週の開始日をキーとする)
                    trendMap = historyList.stream()
                            .collect(Collectors.groupingBy(
                                    history -> history.getDate().with(java.time.DayOfWeek.MONDAY), // 週の開始日(月曜日)
                                    Collectors.summingInt(History::getQuantity)
                            ));
                    break;
                case MONTHLY:
                    // 月毎に集計 (月の開始日をキーとする)
                    trendMap = historyList.stream()
                            .collect(Collectors.groupingBy(
                                    history -> history.getDate().withDayOfMonth(1), // 月の開始日
                                    Collectors.summingInt(History::getQuantity)
                            ));
                    break;
            }
            return trendMap;
        });
    }

    /**
     * カテゴリ別消費傾向データを取得する
     * @param startDate 開始日
     * @param endDate 終了日
     * @return LiveData<Map<String, Integer>> カテゴリ名と消費量のマップ
     */
    public LiveData<Map<String, Integer>> getCategoryConsumptionTrend(LocalDate startDate, LocalDate endDate) {
        MediatorLiveData<Map<String, Integer>> resultLiveData = new MediatorLiveData<>();
        resultLiveData.addSource(historyDao.getOutputHistoryByDateRange(startDate, endDate), historyList -> {
            executorService.execute(() -> {
                Map<String, Integer> categoryConsumptionMap = new HashMap<>();
                if (historyList == null || historyList.isEmpty()) {
                    resultLiveData.postValue(categoryConsumptionMap);
                    return;
                }

                Map<Integer, String> categoryIdToNameMap = new HashMap<>();
                // バックグラウンドスレッドで実行
                List<Category> allCategories = categoryDao.getAllCategories();
                if (allCategories != null) {
                    for (Category category : allCategories) {
                        categoryIdToNameMap.put(category.getId(), category.getName());
                    }
                }

                for (History history : historyList) {
                    // バックグラウンドスレッドで実行
                    MainItem item = mainItemDao.findItemById(history.getItemId());
                    if (item != null) {
                        String categoryName = categoryIdToNameMap.getOrDefault(item.getCategoryId(), "不明なカテゴリ");
                        categoryConsumptionMap.put(categoryName,
                                categoryConsumptionMap.getOrDefault(categoryName, 0) + history.getQuantity());
                    }
                }
                resultLiveData.postValue(categoryConsumptionMap);
            });
        });
        return resultLiveData;
    }

    /**
     * 特定のアイテムの在庫切れ予測日を取得する
     * @param itemId アイテムID
     * @return LiveData<LocalDate> 在庫切れ予測日。予測できない場合はnull。
     */
    public LiveData<LocalDate> getStockoutPredictionDateForItem(int itemId) {
        return Transformations.map(itemAnalyticsDataDao.getAnalyticsDataByItemIdLiveData(itemId), analyticsData -> {
            if (analyticsData != null) {
                return analyticsData.getStockoutPredictionDate();
            }
            return null;
        });
    }

    /**
     * 特定のアイテムの平均消費日数を取得する
     * @param itemId アイテムID
     * @return LiveData<Float> 平均消費日数。データがない場合は0。
     */
    public LiveData<Float> getAverageConsumptionDaysForItem(int itemId) {
        return Transformations.map(itemAnalyticsDataDao.getAnalyticsDataByItemIdLiveData(itemId), analyticsData -> {
            if (analyticsData != null) {
                return analyticsData.getAverageConsumptionDays();
            }
            return 0f;
        });
    }

    /**
     * 特定の日に消費が予測される総量を取得する
     * @param date 予測対象の日付
     * @return LiveData<Double> その日の予測消費総量
     */
    public LiveData<Double> getDailyConsumptionPrediction(LocalDate date) {
        MediatorLiveData<Double> resultLiveData = new MediatorLiveData<>();
        resultLiveData.addSource(mainItemDao.getAllMainItemsLiveData(), allItems -> {
            executorService.execute(() -> { // バックグラウンドスレッドで実行
                double predictedConsumption = 0.0;
                if (allItems == null || allItems.isEmpty()) {
                    resultLiveData.postValue(0.0); // LiveDataに結果をpost
                    return;
                }

                for (MainItem item : allItems) {
                    // バックグラウンドスレッドで実行
                    ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(item.getId());
                    if (analyticsData != null) {
                        // 1. 在庫切れ予測日に基づく予測
                        if (analyticsData.getStockoutPredictionDate() != null && analyticsData.getStockoutPredictionDate().equals(date)) {
                            predictedConsumption += item.getQuantity(); // その日に切れるなら現在の在庫が消費されると仮定
                        }

                        // 2. 平均消費日数に基づく予測
                        if (analyticsData.getAverageConsumptionDays() > 0) {
                            // 1単位を消費するのにかかる平均日数
                            float avgDaysPerUnit = analyticsData.getAverageConsumptionDays();
                            // 1日あたりの平均消費量
                            double dailyConsumptionRate = 1.0 / avgDaysPerUnit;

                            // その日に消費される予測量 (現在の在庫を超えない)
                            double dailyPredictedForItem = Math.min(item.getQuantity(), dailyConsumptionRate);

                            // 在庫切れ予測日と重複しないように、まだ加算されていない場合のみ加算
                            // (より単純化するため、ここでは重複を許容して加算し、後で調整するアプローチも考えられる)
                            // 今回は簡略化のため、在庫切れ予測日のロジックと独立して加算するが、
                            // より正確には、在庫切れ予測で既に加算された分は除くべき。
                            // ここでは、在庫切れ予測日でない場合にのみ平均消費量からの予測を加える
                            if (analyticsData.getStockoutPredictionDate() == null || !analyticsData.getStockoutPredictionDate().equals(date)) {
                                 predictedConsumption += dailyPredictedForItem;
                            }
                        }
                    }
                }
                resultLiveData.postValue(predictedConsumption); // LiveDataに最終結果をpost
            });
        });
        return resultLiveData;
    }

    /**
     * 特定の日の詳細な消費予測情報を取得する（改善版）
     * @param date 予測対象の日付
     * @return LiveData<List<ConsumptionPredictionDetail>> 各アイテムの詳細予測情報
     */
    public LiveData<List<ConsumptionPredictionDetail>> getDetailedConsumptionPrediction(LocalDate date) {
        MediatorLiveData<List<ConsumptionPredictionDetail>> resultLiveData = new MediatorLiveData<>();
        resultLiveData.addSource(mainItemDao.getAllMainItemsLiveData(), allItems -> {
            executorService.execute(() -> { // バックグラウンドスレッドで実行
                List<ConsumptionPredictionDetail> predictions = new ArrayList<>();
                if (allItems == null || allItems.isEmpty()) {
                    resultLiveData.postValue(predictions);
                    return;
                }

                LocalDate today = LocalDate.now();

                for (MainItem item : allItems) {
                    // バックグラウンドスレッドでアイテムの分析データを取得
                    ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(item.getId());
                    if (analyticsData != null) {
                        double predictedAmount = 0.0;
                        String statusMessage = "予測なし";
                        PredictionConfidenceLevel confidenceLevel = PredictionConfidenceLevel.UNKNOWN;
                        int daysUntilStockout = -1;
                        boolean isUrgent = false;

                        // 在庫切れ予測日がある場合
                        if (analyticsData.getStockoutPredictionDate() != null) {
                            daysUntilStockout = (int) ChronoUnit.DAYS.between(today, analyticsData.getStockoutPredictionDate());

                            // 予測日が今日の場合
                            if (analyticsData.getStockoutPredictionDate().equals(date)) {
                                predictedAmount = item.getQuantity();
                                statusMessage = "本日在庫切れの予測";
                                isUrgent = true;
                            }
                            // 予測日が明日の場合
                            else if (analyticsData.getStockoutPredictionDate().equals(date.plusDays(1))) {
                                predictedAmount = item.getQuantity() / 2.0; // 半分程度消費と予測
                                statusMessage = "明日在庫切れの予測";
                                isUrgent = true;
                            }

                            // 在庫切れまでの日数に基づいて信頼度を設定
                            if (daysUntilStockout <= 3) {
                                confidenceLevel = PredictionConfidenceLevel.HIGH;
                            } else if (daysUntilStockout <= 7) {
                                confidenceLevel = PredictionConfidenceLevel.MEDIUM;
                            } else {
                                confidenceLevel = PredictionConfidenceLevel.LOW;
                            }
                        }

                        // 平均消費日数に基づく予測がある場合
                        if (analyticsData.getAverageConsumptionDays() > 0) {
                            float avgDaysPerUnit = analyticsData.getAverageConsumptionDays();
                            double dailyRate = 1.0 / avgDaysPerUnit;
                            double dailyPredictedForItem = Math.min(item.getQuantity(), dailyRate);

                            // 予測の重複がない場合のみ加算
                            if (predictedAmount == 0.0) {
                                predictedAmount = dailyPredictedForItem;

                                // 消費ペースが速い場合
                                if (avgDaysPerUnit <= 2) {
                                    statusMessage = "高頻度消費アイテム";
                                    isUrgent = dailyPredictedForItem > 0.3 * item.getQuantity();
                                } else {
                                    statusMessage = "通常消費ペース";
                                }

                                // データ量に基づいて信頼度を評価
                                if (analyticsData.getDataPointCount() >= 10) {
                                    confidenceLevel = PredictionConfidenceLevel.HIGH;
                                } else if (analyticsData.getDataPointCount() >= 5) {
                                    confidenceLevel = PredictionConfidenceLevel.MEDIUM;
                                } else {
                                    confidenceLevel = PredictionConfidenceLevel.LOW;
                                }
                            }
                        }

                        // 予測情報が得られた場合のみリストに追加
                        if (predictedAmount > 0) {
                            ConsumptionPredictionDetail predictionDetail = new ConsumptionPredictionDetail(
                                predictedAmount, confidenceLevel, statusMessage, daysUntilStockout, isUrgent
                            );
                            predictions.add(predictionDetail);
                        }
                    }
                }

                // 緊急度でソート（緊急なものを先頭に）
                Collections.sort(predictions, (p1, p2) -> {
                    if (p1.isUrgent() != p2.isUrgent()) {
                        return p1.isUrgent() ? -1 : 1;
                    }
                    return Integer.compare(p1.getDaysUntilStockout(), p2.getDaysUntilStockout());
                });

                resultLiveData.postValue(predictions);
            });
        });
        return resultLiveData;
    }

    /**
     * 予測消費量の視覚表示用カテゴリを取得する
     * @param item アイテム
     * @param prediction 予測情報
     * @return 消費予測カテゴリ（"HIGH", "MEDIUM", "LOW"のいずれか）
     */
    public String getConsumptionCategory(MainItem item, ConsumptionPredictionDetail prediction) {
        if (prediction.isUrgent() || prediction.getDaysUntilStockout() <= 2) {
            return "HIGH";
        } else if (prediction.getPredictedAmount() > 0.2 * item.getQuantity()) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 全ての出力履歴と削除履歴を日付の降順で取得する。
     * HistoryDaoから直接LiveDataを取得する。
     * @return 消費履歴のLiveData
     */
    public LiveData<List<History>> getAllOutputAndDeleteHistorySortedDesc() {
        return historyDao.getAllOutputAndDeleteHistorySortedDesc();
    }

    /**
     * 指定期間の廃棄率を取得する
     * @param startDate 開始日
     * @param endDate 終了日
     * @return LiveData<Double> 廃棄率 (パーセンテージ)
     */
    public LiveData<Double> getWasteRate(LocalDate startDate, LocalDate endDate) {
        return Transformations.map(historyDao.getOutputHistoryByDateRange(startDate, endDate), historyList -> {
            if (historyList == null || historyList.isEmpty()) {
                return 0.0; // 消費がない場合は廃棄率0
            }

            int totalConsumedQuantity = 0;
            int wastedQuantity = 0;

            for (History history : historyList) {
                totalConsumedQuantity += history.getQuantity();
                // "廃棄" という文字列で比較
                if ("廃棄".equals(history.getConsumptionReason())) {
                    wastedQuantity += history.getQuantity();
                }
            }

            if (totalConsumedQuantity == 0) {
                return 0.0; // 全体の消費量が0の場合も廃棄率0
            }

            return (double) wastedQuantity / totalConsumedQuantity * 100;
        });
    }

    /**
     * アイテムの残存日数を予測し、在庫切れ予測日をItemAnalyticsDataに保存する。
     * このメソッドはViewModelからバックグラウンドスレッドで呼び出されることを想定しています。
     * @param itemId 対象のアイテムID
     * @return LiveData<Float> 残存予測日数。計算不可の場合は0fを返すLiveData。
     */
    public LiveData<Float> getRemainingDaysPrediction(int itemId) {
        // DAO呼び出しはViewModelのExecutorServiceによってバックグラウンドで実行されるため、
        // ここでの同期的な呼び出しは許容される。
        MainItem mainItem = mainItemDao.findItemById(itemId);
        ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(itemId);

        float remainingDays = 0f;
        LocalDate stockoutDate = null;

        if (mainItem != null && analyticsData != null && analyticsData.getAverageConsumptionDays() > 0 && mainItem.getQuantity() > 0) {
            float averageConsumptionDays = analyticsData.getAverageConsumptionDays();
            int currentQuantity = mainItem.getQuantity();
            remainingDays = currentQuantity * averageConsumptionDays;

            if (remainingDays > 0) {
                stockoutDate = LocalDate.now().plusDays((long) Math.ceil(remainingDays));
            }
        }

        // ItemAnalyticsDataに在庫切れ予測日を保存 (analyticsDataがnullでない場合のみ)
        if (analyticsData != null) {
            analyticsData.setStockoutPredictionDate(stockoutDate);
            itemAnalyticsDataDao.insert(analyticsData); // OnConflictStrategy.REPLACE により更新
        } else if (mainItem != null) {
            // analyticsData が null だが mainItem は存在する場合、
            // 新しい analyticsData を作成して stockoutDate (nullの可能性あり) を設定することも検討できる。
            // ただし、averageConsumptionDaysが不明なため、ここでは何もしない。
            // updateAverageConsumptionDaysForItem が先に呼ばれることを期待する。
        }

        return new MutableLiveData<>(remainingDays);
    }

    /**
     * アイテムの必要補充量を計算する
     * @param itemId 対象のアイテムID
     * @param useOptimalLevel trueなら最適在庫レベル基準、falseなら最低在庫レベル基準で計算
     * @return LiveData<Integer> 必要補充量。補充不要なら0を返すLiveData。
     */
    public LiveData<Integer> getRequiredRestockQuantity(int itemId, boolean useOptimalLevel) {
        // 注意: 以下のDAO呼び出しは同期的に行っています。 (上記と同様の注意)
        MainItem mainItem = mainItemDao.findItemById(itemId);
        ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(itemId);

        if (mainItem == null || analyticsData == null) {
            return new MutableLiveData<>(0);
        }

        int targetStockLevel = useOptimalLevel ? analyticsData.getOptimalStockLevel() : analyticsData.getMinStockLevel();
        int currentQuantity = mainItem.getQuantity();
        int requiredQuantity = targetStockLevel - currentQuantity;

        return new MutableLiveData<>(Math.max(0, requiredQuantity)); // マイナスにならないように
    }

    /**
     * 購入優先度が高いアイテムのリストを生成する。
     * このメソッドはViewModelからバックグラウンドスレッドで呼び出されることを想定しています。
     * @return LiveData<List<PrioritizedItem>> 優先度付けされたアイテムのリスト
     */
    public LiveData<List<PrioritizedItem>> getPrioritizedPurchaseItems() {
        // DAO呼び出しはViewModelのExecutorServiceによってバックグラウンドで実行されるため、
        // ここでの同期的な呼び出しは許容される。
        List<MainItem> allMainItems = mainItemDao.getAllMainItems(); // MainItemDaoにこのメソッドがある前提
        List<PrioritizedItem> prioritizedItems = new ArrayList<>();

        if (allMainItems == null || allMainItems.isEmpty()) {
            return new MutableLiveData<>(prioritizedItems); // 空のリストを返す
        }

        for (MainItem item : allMainItems) {
            ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(item.getId());

            // ItemAnalyticsDataが存在しない、または平均消費日数が未計算の場合は計算・作成
            if (analyticsData == null || analyticsData.getAverageConsumptionDays() <= 0) {
                updateAverageConsumptionDaysForItem(item.getId()); // 平均消費日数と初期analyticsDataを計算・保存
                analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(item.getId()); // 再取得
            }

            if (analyticsData == null) { // それでもnullならスキップ (エラーケース)
                continue;
            }

            // 残存日数と在庫切れ予測日を計算 (getRemainingDaysPredictionのロジックを一部再利用・適用)
            float calculatedRemainingDays = 0f;
            LocalDate calculatedStockoutDate = null;
            if (analyticsData.getAverageConsumptionDays() > 0 && item.getQuantity() > 0) {
                calculatedRemainingDays = item.getQuantity() * analyticsData.getAverageConsumptionDays();
                if (calculatedRemainingDays > 0) {
                    calculatedStockoutDate = LocalDate.now().plusDays((long) Math.ceil(calculatedRemainingDays));
                }
            }
            // 在庫切れ予測日をItemAnalyticsDataに保存更新
            analyticsData.setStockoutPredictionDate(calculatedStockoutDate);
            itemAnalyticsDataDao.insert(analyticsData); // 更新


            // 優先度スコア計算
            float priorityScore = 0f;
            if (item.getQuantity() == 0) {
                priorityScore = 110f; // 在庫0は最優先
            } else if (calculatedRemainingDays > 0 && calculatedRemainingDays <= 3) {
                priorityScore = 100f; // 残り3日以内
            } else if (item.getQuantity() <= analyticsData.getMinStockLevel() && analyticsData.getMinStockLevel() > 0) {
                priorityScore = 90f;  // 最低在庫レベル以下
            } else if (calculatedRemainingDays > 0 && calculatedRemainingDays <= 7) {
                priorityScore = 80f;  // 残り7日以内
            } else if (calculatedRemainingDays > 0) {
                priorityScore = Math.max(0, 70 - calculatedRemainingDays); // 残り日数が多いほどスコア減 (最大70)
            } else {
                priorityScore = 75f; // 上記以外で在庫あり (例: 平均消費日数0だが在庫はある)
            }

            // 推奨購入量の計算
            int recommendedQuantity = 0;
            if (analyticsData.getOptimalStockLevel() > 0) { // 最適在庫レベルが設定されている場合
                recommendedQuantity = Math.max(0, analyticsData.getOptimalStockLevel() - item.getQuantity());
            } else if (analyticsData.getMinStockLevel() > 0 && item.getQuantity() < analyticsData.getMinStockLevel()) { // 最適在庫未設定で、最低在庫レベルが設定され、それを下回っている場合
                recommendedQuantity = Math.max(0, analyticsData.getMinStockLevel() - item.getQuantity());
            } else if (item.getQuantity() == 0) { // 在庫が0で、目標在庫レベルも未設定の場合
                recommendedQuantity = 1; // とりあえず1つ
            }
            // 上記以外（在庫があり、最低在庫レベル以上、または最低在庫レベル未設定）の場合は推奨購入量0のまま

            prioritizedItems.add(new PrioritizedItem(
                    item,
                    analyticsData,
                    priorityScore,
                    recommendedQuantity,
                    calculatedRemainingDays,
                    calculatedStockoutDate
            ));
        }

        // 優先度スコアで降順ソート、スコアが同じなら残存日数で昇順ソート
        Collections.sort(prioritizedItems, (o1, o2) -> {
            int scoreCompare = Float.compare(o2.getPriorityScore(), o1.getPriorityScore());
            if (scoreCompare == 0) {
                return Float.compare(o1.getRemainingDays(), o2.getRemainingDays());
            }
            return scoreCompare;
        });

        return new MutableLiveData<>(prioritizedItems);
    }

    public LiveData<List<ShoppingListItem>> generateShoppingList() {
        // getPrioritizedPurchaseItems() は LiveData<List<PrioritizedItem>> を返す。
        // これを変換して LiveData<List<ShoppingListItem>> を得る。
        return Transformations.map(getPrioritizedPurchaseItems(), prioritizedItems -> {
            List<ShoppingListItem> shoppingList = new ArrayList<>();
            if (prioritizedItems == null) {
                return shoppingList; // 空のリスト
            }

            for (PrioritizedItem prioritizedItem : prioritizedItems) {
                if (prioritizedItem.getRecommendedPurchaseQuantity() > 0) {
                    String reason = ""; // 購入理由を決定するロジック
                    ItemAnalyticsData analyticsData = prioritizedItem.getAnalyticsData(); // analyticsDataを事前に取得

                    if (prioritizedItem.getMainItem().getQuantity() == 0) {
                        reason = "在庫なし";
                    } else if (analyticsData != null && analyticsData.getOptimalStockLevel() > 0 && prioritizedItem.getMainItem().getQuantity() < analyticsData.getOptimalStockLevel()) {
                        reason = "最適在庫レベル未満";
                    } else if (analyticsData != null && analyticsData.getMinStockLevel() > 0 && prioritizedItem.getMainItem().getQuantity() < analyticsData.getMinStockLevel()) {
                        reason = "最低在庫レベル未満";
                    } else if (prioritizedItem.getRemainingDays() > 0 && prioritizedItem.getRemainingDays() <= 3) {
                        reason = "残日数3日以下";
                    } else if (prioritizedItem.getRemainingDays() > 0 && prioritizedItem.getRemainingDays() <= 7) {
                        reason = "残日数7日以下";
                    } else if (prioritizedItem.getRecommendedPurchaseQuantity() > 0) { // 上記以外で推奨購入量がある場合
                        reason = "補充推奨";
                    }

                    shoppingList.add(new ShoppingListItem(
                            prioritizedItem.getMainItem(),
                            prioritizedItem.getRecommendedPurchaseQuantity(),
                            reason
                    ));
                }
            }
            return shoppingList;
        });
    }

    /**
     * 特定のアイテムを買い物リスト推奨から除外する
     * @param itemId 除外するアイテムのID
     */
    public void removeItemFromShoppingListRecommendations(int itemId) {
        ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(itemId);
        if (analyticsData != null) {
            // 推奨購入量を計算するもとになる最適在庫レベルなどを一時的に0に設定して
            // 買い物リストに表示されないようにする
            analyticsData.setOptimalStockLevel(0);
            analyticsData.setMinStockLevel(0);
            itemAnalyticsDataDao.insert(analyticsData); // OnConflictStrategy.REPLACE により更新
        }
    }

    /**
     * ユーザーが定義した購入数量を更新する
     * @param itemId 対象のアイテムID
     * @param newQuantity 新しい購入数量
     */
    public void updateUserDefinedPurchaseQuantity(int itemId, int newQuantity) {
        // 将来的にはユーザー定義の購入数量を格納するテーブルを作成することも検討
        // 現在は簡略化のため、ItemAnalyticsDataの最適在庫レベルを一時的に調整して実装
        ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(itemId);
        MainItem item = mainItemDao.findItemById(itemId);

        if (analyticsData != null && item != null) {
            // 現在の在庫 + 新しい購入数量 = 目標とする最適在庫レベル
            analyticsData.setOptimalStockLevel(item.getQuantity() + newQuantity);
            itemAnalyticsDataDao.insert(analyticsData); // OnConflictStrategy.REPLACE により更新
        }
    }

    /**
     * アイテムの消費分析データを更新する
     * 平均消費日数、データポイント数、在庫切れ予測日を計算して更新
     * @param itemId 更新対象のアイテムID
     */
    public void updateAverageConsumptionDaysForItem(int itemId) {
        executorService.execute(() -> {
            // 出力履歴を取得（日付の新しい順）
            List<History> outputHistory = historyDao.getOutputHistoryForItemDesc(itemId);

            // データポイント数 = 履歴レコード総数
            int dataPointCount = outputHistory != null ? outputHistory.size() : 0;

            // 平均消費日数を計算
            double avgDays = calculateAverageConsumptionDays(outputHistory);

            // 現在のアイテム情報を取得
            MainItem item = mainItemDao.findItemById(itemId);
            ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(itemId);

            if (analyticsData == null) {
                // 分析データが存在しない場合は新規作成
                analyticsData = new ItemAnalyticsData(
                        itemId,
                        null,       // consumptionReason
                        null,       // consumptionTiming
                        null,       // consumptionPace
                        0,          // minStockLevel
                        0,          // optimalStockLevel
                        null,       // restockTimingGuideline
                        (float) avgDays, // averageConsumptionDays
                        null,       // stockoutPredictionDate
                        null        // seasonalConsumptionPattern
                );
            } else {
                // 既存データの更新
                analyticsData.setAverageConsumptionDays((float) avgDays);
            }

            // データポイント数を更新
            analyticsData.setDataPointCount(dataPointCount);

            // 在庫切れ予測日を計算・更新
            if (item != null && avgDays > 0 && item.getQuantity() > 0) {
                // 残り日数 = 現在の在庫数 × 1単位あたりの平均消費日数
                float remainingDays = item.getQuantity() * (float) avgDays;
                // 在庫切れ予測日 = 今日 + 残り日数
                LocalDate stockoutDate = LocalDate.now().plusDays((long) Math.ceil(remainingDays));
                analyticsData.setStockoutPredictionDate(stockoutDate);
            } else {
                // 計算に必要な条件を満たさない場合は予測日をクリア
                analyticsData.setStockoutPredictionDate(null);
            }

            // 消費理由と消費タイミングの分析（データがあれば）
            if (dataPointCount > 0) {
                // 最も多い消費理由を特定
                Map<String, Integer> reasonCounts = new HashMap<>();
                for (History history : outputHistory) {
                    String reason = history.getConsumptionReason();
                    if (reason != null && !reason.isEmpty()) {
                        reasonCounts.put(reason, reasonCounts.getOrDefault(reason, 0) + 1);
                    }
                }

                // 消費理由が一番多いものを設定
                if (!reasonCounts.isEmpty()) {
                    String mostCommonReason = Collections.max(reasonCounts.entrySet(),
                            Map.Entry.comparingByValue()).getKey();
                    analyticsData.setConsumptionReason(mostCommonReason);
                }

                // 季節性の検出（過去1年のデータがある場合）
                // 月別の消費量を分析し、顕著な差があれば季節パターンを設定
                if (dataPointCount >= 12) {
                    analyticsData.setSeasonalConsumptionPattern(detectSeasonalPattern(outputHistory));
                }
            }

            // データベースに保存
            itemAnalyticsDataDao.insert(analyticsData); // OnConflictStrategy.REPLACE により、存在すれば更新、なければ挿入
        });
    }

    /**
     * 消費履歴から季節的なパターンを検出
     * @param history 消費履歴データ
     * @return 検出された季節パターンの説明文。パターンが検出されない場合はnull。
     */
    private String detectSeasonalPattern(List<History> history) {
        // 履歴が少ない場合は季節性なしと判断
        if (history == null || history.size() < 12) {
            return null;
        }

        // 月ごとの消費量を集計
        Map<Integer, Integer> monthlyConsumption = new HashMap<>();
        for (History record : history) {
            if (record.getDate() != null) {
                int month = record.getDate().getMonthValue(); // 1-12の月
                monthlyConsumption.put(month,
                    monthlyConsumption.getOrDefault(month, 0) + record.getQuantity());
            }
        }

        // 四季に分類（日本の季節区分）
        // 春：3-5月、夏：6-8月、秋：9-11月、冬：12-2月
        int springConsumption = getSeasonConsumption(monthlyConsumption, 3, 5);
        int summerConsumption = getSeasonConsumption(monthlyConsumption, 6, 8);
        int fallConsumption = getSeasonConsumption(monthlyConsumption, 9, 11);
        int winterConsumption = getSeasonConsumption(monthlyConsumption, 12, 2);

        // 総消費量
        int totalConsumption = springConsumption + summerConsumption + fallConsumption + winterConsumption;
        if (totalConsumption == 0) return null;

        // 各季節の消費割合
        double springRatio = (double) springConsumption / totalConsumption;
        double summerRatio = (double) summerConsumption / totalConsumption;
        double fallRatio = (double) fallConsumption / totalConsumption;
        double winterRatio = (double) winterConsumption / totalConsumption;

        // 顕著な季節パターンを検出（ある季節の消費が平均の1.5倍以上）
        double averageRatio = 0.25; // 理論上の平均（25%）
        StringBuilder pattern = new StringBuilder();

        if (springRatio > averageRatio * 1.5) {
            pattern.append("春に消費量が増加。");
        }
        if (summerRatio > averageRatio * 1.5) {
            pattern.append("夏に消費量が増加。");
        }
        if (fallRatio > averageRatio * 1.5) {
            pattern.append("秋に消費量が増加。");
        }
        if (winterRatio > averageRatio * 1.5) {
            pattern.append("冬に消費量が増加。");
        }

        // 逆に消費が少ない季節も検出（平均の0.5倍以下）
        if (springRatio < averageRatio * 0.5) {
            pattern.append("春は消費が少ない。");
        }
        if (summerRatio < averageRatio * 0.5) {
            pattern.append("夏は消費が少ない。");
        }
        if (fallRatio < averageRatio * 0.5) {
            pattern.append("秋は消費が少ない。");
        }
        if (winterRatio < averageRatio * 0.5) {
            pattern.append("冬は消費が少ない。");
        }

        return pattern.length() > 0 ? pattern.toString() : null;
    }

    /**
     * 指定された月の範囲の消費量合計を計算
     * @param monthlyConsumption 月別消費量のマップ
     * @param startMonth 開始月（1～12）
     * @param endMonth 終了月（1～12）
     * @return 指定月範囲の消費量合計
     */
    private int getSeasonConsumption(Map<Integer, Integer> monthlyConsumption, int startMonth, int endMonth) {
        int total = 0;
        if (startMonth <= endMonth) {
            // 通常の月範囲（例：3月～5月）
            for (int month = startMonth; month <= endMonth; month++) {
                total += monthlyConsumption.getOrDefault(month, 0);
            }
        } else {
            // 年をまたぐ月範囲（例：12月～2月）
            for (int month = startMonth; month <= 12; month++) {
                total += monthlyConsumption.getOrDefault(month, 0);
            }
            for (int month = 1; month <= endMonth; month++) {
                total += monthlyConsumption.getOrDefault(month, 0);
            }
        }
        return total;
    }

    /**
     * アイテムの平均消費日数を計算する（直近最大7回の消費イベントに基づく）
     * @param outputHistory 対象アイテムの "output" 履歴リスト (日付の新しい順にソートされていること)
     * @return 1単位あたりの平均消費日数。計算に必要なデータが不足している場合は0.0を返す。
     */
    public double calculateAverageConsumptionDays(List<History> outputHistory) {
        if (outputHistory == null || outputHistory.isEmpty()) {
            return 0.0; // 履歴がない
        }

        // 直近最大7回の消費履歴を取得 (outputHistory は新しい順を想定)
        List<History> recentHistory = outputHistory.subList(0, Math.min(7, outputHistory.size()));

        if (recentHistory.size() < 2) {
            // 平均を計算するには最低2回の消費イベントが必要
            return 0.0;
        }

        long totalQuantityConsumedInPeriod = 0;
        for (History history : recentHistory) {
            totalQuantityConsumedInPeriod += history.getQuantity();
        }

        if (totalQuantityConsumedInPeriod == 0) {
            return 0.0; // 対象期間の消費量がない
        }

        // 期間内の最初と最後の日付を取得 (recentHistoryは新しい順なので、最後が最も古く、最初が最も新しい)
        LocalDate firstDateInPeriod = recentHistory.get(recentHistory.size() - 1).getDate(); // 期間内の最も古い消費日
        LocalDate lastDateInPeriod = recentHistory.get(0).getDate();         // 期間内の最も新しい消費日

        long daysBetween = ChronoUnit.DAYS.between(firstDateInPeriod, lastDateInPeriod);

        if (daysBetween <= 0) {
            // 全ての消費が同日、または非常に短期間の場合
            // 1日あたりの消費量から、1単位あたりの日数を計算 (例: 1日で5個消費なら、1/5 = 0.2日/個)
            // totalQuantityConsumedInPeriod が 0 の場合は上で弾かれているので、ここでは常に正。
            return 1.0 / totalQuantityConsumedInPeriod;
        } else {
            // 消費期間が1日以上ある場合
            return (double) daysBetween / totalQuantityConsumedInPeriod;
        }
    }
}

