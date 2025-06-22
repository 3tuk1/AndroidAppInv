package com.inv.inventryapp.usecase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.inv.inventryapp.model.entity.History;
import com.inv.inventryapp.model.entity.Product;
import com.inv.inventryapp.repository.HistoryRepository;
import com.inv.inventryapp.repository.ProductRepository;

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

    public LiveData<List<Float>> execute(String yearMonth) {
        LiveData<List<History>> historiesLiveData = historyRepository.getHistoriesForMonth(yearMonth);
        LiveData<List<Product>> productsLiveData = productRepository.getAllProductsWithZeroQuantity();

        MediatorLiveData<List<Float>> result = new MediatorLiveData<>();

        result.addSource(historiesLiveData, histories -> {
            List<Product> products = productsLiveData.getValue();
            if (histories != null && products != null) {
                calculateAndPost(histories, products, result);
            }
        });

        result.addSource(productsLiveData, products -> {
            List<History> histories = historiesLiveData.getValue();
            if (histories != null && products != null) {
                calculateAndPost(histories, products, result);
            }
        });

        return result;
    }

    private void calculateAndPost(List<History> histories, List<Product> products, MediatorLiveData<List<Float>> result) {
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductName, Function.identity(), (p1, p2) -> p1));

        Map<String, Integer> summedPricesByProduct = histories.stream()
                .collect(Collectors.groupingBy(
                        History::getProductName,
                        Collectors.summingInt(history -> {
                            Product product = productMap.get(history.getProductName());
                            if (product != null && product.getPrice() != null) {
                                return product.getPrice() * history.getQuantity();
                            }
                            return 0;
                        })
                ));

        List<Float> pieData = products.stream()
                .map(product -> summedPricesByProduct.getOrDefault(product.getProductName(), 0).floatValue())
                .collect(Collectors.toList());

        result.postValue(pieData);
    }
}
