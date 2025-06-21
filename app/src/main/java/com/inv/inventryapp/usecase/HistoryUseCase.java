package com.inv.inventryapp.usecase;

import com.inv.inventryapp.repository.HistoryRepository;
import java.time.LocalDate;

public class HistoryUseCase {
    private final HistoryRepository historyRepository;

    public HistoryUseCase(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void addHistory(String productName, String type, LocalDate date, int quantity) {
        historyRepository.addHistory(productName, type, date, quantity);
    }
}
