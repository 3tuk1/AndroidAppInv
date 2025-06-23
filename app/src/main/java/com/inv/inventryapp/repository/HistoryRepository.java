package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.ModelDatabase;
import com.inv.inventryapp.model.dao.HistoryDao;
import com.inv.inventryapp.model.entity.History;
import java.time.LocalDate;
import java.util.List; // Listをインポート
import androidx.lifecycle.LiveData;

public class HistoryRepository {
    private final HistoryDao historyDao;

    public HistoryRepository(Context context) {
        ModelDatabase db = ModelDatabase.Companion.getInstance(context);
        this.historyDao = db.historyDao();
    }

    public void addHistory(String productName, String type, LocalDate date, int quantity) {
        History history = new History(productName, type, date, quantity);
        historyDao.insert(history);
    }

    public void addHistory(History history) {
        historyDao.insert(history);
    }

    public void updateHistory(History history) {
        historyDao.update(history);
    }

    public void deleteHistory(History history) {
        historyDao.delete(history);
    }

    // 全ての履歴を取得するメソッドを追加
    public List<History> getAllHistories() {
        return historyDao.getAll();
    }

    public LiveData<List<History>> getHistoriesForMonth(String yearMonth) {
        return historyDao.getHistoriesForMonth(yearMonth);
    }

    public void deleteAll() {
        historyDao.deleteAll();
    }
}
