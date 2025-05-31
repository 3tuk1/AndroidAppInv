package com.inv.inventryapp.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData; // MutableLiveData をインポート
import androidx.lifecycle.Transformations;

import com.inv.inventryapp.models.Category;
import com.inv.inventryapp.models.History;
import com.inv.inventryapp.models.ItemAnalyticsData; // ItemAnalyticsData をインポート
import com.inv.inventryapp.models.MainItem; // MainItem をインポート
import com.inv.inventryapp.models.PrioritizedItem; // PrioritizedItem をインポート
import com.inv.inventryapp.models.ShoppingListItem; // ShoppingListItem をインポート
import com.inv.inventryapp.room.CategoryDao;
import com.inv.inventryapp.room.HistoryDao;
import com.inv.inventryapp.room.ItemAnalyticsDataDao; // ItemAnalyticsDataDao をインポート
import com.inv.inventryapp.room.MainItemDao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections; // Collections をインポート
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsRepository {

    private HistoryDao historyDao;
    private MainItemDao mainItemDao;
    private CategoryDao categoryDao;
    private ItemAnalyticsDataDao itemAnalyticsDataDao; // ItemAnalyticsDataDao フィールドを追加

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
        return Transformations.map(historyDao.getOutputHistoryByDateRange(startDate, endDate), historyList -> {
            Map<String, Integer> categoryConsumptionMap = new HashMap<>();
            if (historyList == null || historyList.isEmpty()) {
                return categoryConsumptionMap;
            }

            Map<Integer, String> categoryIdToNameMap = new HashMap<>();
            List<Category> allCategories = categoryDao.getAllCategories(); // 同期的に取得するか、LiveDataをチェインするか検討
            if (allCategories != null) {
                for (Category category : allCategories) {
                    categoryIdToNameMap.put(category.getId(), category.getName());
                }
            }

            for (History history : historyList) {
                MainItem item = mainItemDao.findItemById(history.getItemId()); // 同期的に取得するか、LiveDataをチェインするか検討
                if (item != null) {
                    String categoryName = categoryIdToNameMap.getOrDefault(item.getCategoryId(), "不明なカテゴリ");
                    categoryConsumptionMap.put(categoryName,
                            categoryConsumptionMap.getOrDefault(categoryName, 0) + history.getQuantity());
                }
            }
            return categoryConsumptionMap;
        });
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

    /**
     * 特定のアイテムの平均消費日数を計算し、ItemAnalyticsDataに保存・更新する。
     * このメソッドはバックグラウンドスレッドで実行されることを想定しています。
     * @param itemId 対象のアイテムID
     */
    public void updateAverageConsumptionDaysForItem(int itemId) {
        List<History> outputHistory = historyDao.getOutputHistoryForItemDesc(itemId);
        double avgDays = calculateAverageConsumptionDays(outputHistory);

        ItemAnalyticsData analyticsData = itemAnalyticsDataDao.getItemAnalyticsDataByItemId(itemId);
        if (analyticsData == null) {
            analyticsData = new ItemAnalyticsData(
                    itemId,
                    null, // consumptionReason
                    null, // consumptionTiming
                    null, // consumptionPace
                    0,    // minStockLevel
                    0,    // optimalStockLevel
                    null, // restockTimingGuideline
                    (float) avgDays, // averageConsumptionDays
                    null, // stockoutPredictionDate
                    null  // seasonalConsumptionPattern
            );
        } else {
            analyticsData.setAverageConsumptionDays((float) avgDays);
        }
        itemAnalyticsDataDao.insert(analyticsData); // OnConflictStrategy.REPLACE により、存在すれば更新、なければ挿入
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

    /**
     * 買い物リストを自動生成する。
     * 優先購入アイテムリストを基に、実際に購入が必要なアイテムを抽出する。
     * このメソッドはViewModelからバックグラウンドスレッドで呼び出されることを想定しています。
     * @return LiveData<List<ShoppingListItem>> 生成された買い物リスト
     */
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
                    if (prioritizedItem.getMainItem().getQuantity() == 0) {
                        reason = "在庫なし";
                    } else if (prioritizedItem.getRemainingDays() > 0 && prioritizedItem.getRemainingDays() <= 3) {
                        reason = "残日数3日以下";
                    } else if (prioritizedItem.getAnalyticsData() != null && prioritizedItem.getMainItem().getQuantity() <= prioritizedItem.getAnalyticsData().getMinStockLevel() && prioritizedItem.getAnalyticsData().getMinStockLevel() > 0) {
                        reason = "最低在庫レベル以下";
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
     * "output" または "delete" タイプの全消費履歴を日付の新しい順に取得する。
     * @return LiveData<List<History>> 消費履歴のリスト
     */
    public LiveData<List<History>> getAllConsumptionHistory() {
        return historyDao.getAllOutputAndDeleteHistorySortedDesc();
    }
}
