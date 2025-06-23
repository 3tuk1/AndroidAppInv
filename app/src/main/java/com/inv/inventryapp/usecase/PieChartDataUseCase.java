package com.inv.inventryapp.usecase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.inv.inventryapp.model.dto.PieChartSummary; // 新しいDTOをインポート
import com.inv.inventryapp.model.entity.History;
import com.inv.inventryapp.model.entity.Product;
import com.inv.inventryapp.repository.HistoryRepository;
import com.inv.inventryapp.repository.ProductRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PieChartDataUseCase {

    private final HistoryRepository historyRepository;
    private final ProductRepository productRepository;

    public PieChartDataUseCase(HistoryRepository historyRepository, ProductRepository productRepository) {
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
    }

    // 戻り値の型を `LiveData<PieChartSummary>` に変更
    public LiveData<PieChartSummary> execute(String yearMonth) {
        LiveData<List<History>> historiesLiveData = historyRepository.getHistoriesForMonth(yearMonth);
        // 在庫金額計算のため、在庫が0以上の商品も取得対象に
        LiveData<List<Product>> productsLiveData = productRepository.getAllProducts();

        MediatorLiveData<PieChartSummary> result = new MediatorLiveData<>();

        // Lambda内で使用するためにfinal化
        final Function<Object, Void> recalculate = (ignored) -> {
            List<History> histories = historiesLiveData.getValue();
            List<Product> products = productsLiveData.getValue();
            if (histories != null && products != null) {
                calculateAndPost(histories, products, result);
            }
            return null;
        };

        result.addSource(historiesLiveData, histories -> recalculate.apply(null));
        result.addSource(productsLiveData, products -> recalculate.apply(null));

        return result;
    }

    private void calculateAndPost(List<History> histories, List<Product> products, MediatorLiveData<PieChartSummary> result) {
        // 商品名と価格のマップを作成
        Map<String, Integer> priceMap = products.stream()
                .filter(p -> p.getPrice() != null)
                .collect(Collectors.toMap(Product::getProductName, Product::getPrice, (p1, p2) -> p1));

        // タイプ別に合計金額を計算
        int totalPurchase = 0;
        int totalConsumption = 0;
        int totalDisposal = 0;

        for (History history : histories) {
            int price = priceMap.getOrDefault(history.getProductName(), 0);
            int amount = price * history.getQuantity();

            switch (history.getType()) {
                case "購入":
                    totalPurchase += amount;
                    break;
                case "消費":
                    totalConsumption += amount;
                    break;
                case "削除": // 「削除」を「廃棄」として扱う
                    totalDisposal += amount;
                    break;
            }
        }

        // 円グラフ用のデータマップを作成
        Map<String, Float> pieDataMap = new HashMap<>();
        pieDataMap.put("購入", (float) totalPurchase);
        pieDataMap.put("消費", (float) totalConsumption);
        pieDataMap.put("廃棄", (float) totalDisposal);

        // 現在の在庫総額を計算
        int currentStockValue = products.stream()
                .filter(p -> p.getQuantity() != null && p.getPrice() != null && p.getQuantity() > 0)
                .mapToInt(p -> p.getQuantity() * p.getPrice())
                .sum();

        // 総合計（画像の例に基づき、購入合計額とする）
        int overallTotal = totalPurchase;

        // 結果をPieChartSummaryオブジェクトにまとめてpost
        PieChartSummary summary = new PieChartSummary(pieDataMap, totalPurchase, totalConsumption, totalDisposal, currentStockValue, overallTotal);
        result.postValue(summary);
    }
}
