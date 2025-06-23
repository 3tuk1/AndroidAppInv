package com.inv.inventryapp.usecase;

import com.inv.inventryapp.model.entity.Analysis;
import com.inv.inventryapp.model.entity.History;
import com.inv.inventryapp.model.entity.Product;
import com.inv.inventryapp.repository.AnalysisRepository;
import com.inv.inventryapp.repository.HistoryRepository;
import com.inv.inventryapp.repository.ProductRepository;
import com.inv.inventryapp.repository.ShoppingListRepository; // インポート

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ConsumptionAnalysisUseCase {
    private final ProductRepository productRepository;
    private final HistoryRepository historyRepository;
    private final AnalysisRepository analysisRepository;
    private final ShoppingListRepository shoppingListRepository; // 追加

    public ConsumptionAnalysisUseCase(ProductRepository productRepository, HistoryRepository historyRepository, AnalysisRepository analysisRepository, ShoppingListRepository shoppingListRepository) {
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
        this.analysisRepository = analysisRepository;
        this.shoppingListRepository = shoppingListRepository; // 追加
    }

    /**
     * 全商品の消費分析を行い、結果をデータベースに保存します。
     * このメソッドはIO負荷が高いため、必ずワーカースレッドで実行してください。
     */
    public void analyzeAndSave() {
        List<Product> products = productRepository.getAllProductsList();
        List<History> allHistories = historyRepository.getAllHistories();

        for (Product product : products) {
            List<History> consumptionHistory = allHistories.stream()
                    .filter(h -> product.getProductName().equals(h.getProductName()) && "消費".equals(h.getType()))
                    .sorted(Comparator.comparing(History::getDate))
                    .collect(Collectors.toList());

            if (consumptionHistory.size() < 2) {
                continue; // 分析には最低2件の消費履歴が必要
            }

            // --- 消費ペースの計算 ---
            LocalDate firstDate = consumptionHistory.get(0).getDate();
            LocalDate lastDate = consumptionHistory.get(consumptionHistory.size() - 1).getDate();
            long daysBetween = ChronoUnit.DAYS.between(firstDate, lastDate);

            if (daysBetween <= 0) {
                continue; // 期間が0日以下の場合は計算不能
            }

            int totalConsumed = consumptionHistory.stream().mapToInt(History::getQuantity).sum();
            double avgConsumptionPerDay = (double) totalConsumed / daysBetween;

            if (avgConsumptionPerDay <= 0) {
                continue; // 消費ペースが0以下は分析不能
            }

            // --- 各種指標の計算 ---
            int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;

            // 1. 消費日数（1個あたり）
            float daysPerItem = (float) (1.0 / avgConsumptionPerDay);

            // 2. 在庫が尽きるまでの残り日数
            float remainingDays = (float) (currentStock * daysPerItem);

            // 3. 消費予想日
            LocalDate outOfStockDate = LocalDate.now().plusDays((long) remainingDays);

            // 4. 推奨購入個数
            int recommendedQuantity = (int) Math.ceil(avgConsumptionPerDay * 7);
            if (recommendedQuantity == 0 && totalConsumed > 0) {
                recommendedQuantity = 1;
            }

            // 5. 優先度スコア (残り日数が少ないほど高スコア)
            float priorityScore = remainingDays > 0 ? 100.0f / remainingDays : 999.0f;


            // --- Analysisオブジェクトの作成と保存 ---
            Analysis existingAnalysis = analysisRepository.findByProductName(product.getProductName());
            Analysis analysisToSave = (existingAnalysis != null) ? existingAnalysis : new Analysis();

            analysisToSave.setProductName(product.getProductName());
            analysisToSave.setPriorityScore(priorityScore);
            analysisToSave.setRecommendedQuantity(recommendedQuantity);
            analysisToSave.setRemainingDays(remainingDays);
            analysisToSave.setOutOfStockDate(outOfStockDate);
            analysisToSave.setDaysPerItem(daysPerItem);

            analysisRepository.addAnalysis(analysisToSave); // INSERT (REPLACE)を実行

            // --- ★ここからが新しいロジック★ ---
            // 消費予想日に基づき、当月中に在庫切れになる商品を購入リストへ追加
            if (outOfStockDate != null) {
                LocalDate today = LocalDate.now();
                // 予想日が今日以降、かつ同じ月内にあるかチェック
                if (!outOfStockDate.isBefore(today) &&
                        outOfStockDate.getYear() == today.getYear() &&
                        outOfStockDate.getMonth() == today.getMonth()) {

                    // 推奨購入数をリストに追加する数量とする（0の場合は1個）
                    int quantityToAdd = analysisToSave.getRecommendedQuantity() > 0 ? analysisToSave.getRecommendedQuantity() : 1;
                    shoppingListRepository.addShoppingList(product.getProductName(), quantityToAdd);
                }
            }
        }
    }
}
