package com.inv.inventryapp.usecase;

import com.inv.inventryapp.model.entity.History;
import com.inv.inventryapp.model.entity.Product;
import com.inv.inventryapp.repository.HistoryRepository;
import com.inv.inventryapp.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PieChartDataUseCase {

    private final HistoryRepository historyRepository;
    private final ProductRepository productRepository;

    public PieChartDataUseCase(HistoryRepository historyRepository, ProductRepository productRepository) {
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
    }

    public List<Float> execute(String yearMonth) {
        List<History> histories = historyRepository.getHistoriesForMonth(yearMonth);

        // Typeごとに価格を合計
        Map<String, Integer> summedPrices = histories.stream()
                .collect(Collectors.groupingBy(
                        History::getType,
                        Collectors.summingInt(history -> {
                            Product product = productRepository.findByName(history.getProductName());
                            if (product != null && product.getPrice() != null) {
                                return product.getPrice() * history.getQuantity();
                            }
                            return 0;
                        })
                ));

        // 合計値をFloatのリストに変換
        return summedPrices.values().stream()
                .map(Integer::floatValue)
                .collect(Collectors.toList());
    }
}
