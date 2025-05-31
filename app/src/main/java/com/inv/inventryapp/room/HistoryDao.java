package com.inv.inventryapp.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import com.inv.inventryapp.models.History;
import java.time.LocalDate;
import java.util.List;

@Dao
public interface HistoryDao {
    // Insert a new history record
    @androidx.room.Insert
    void insert(History history);

    // Update an existing history record
    @androidx.room.Update
    void update(History history);

    // Delete a history record
    @androidx.room.Delete
    void delete(History history);

    // Get all history records
    @androidx.room.Query("SELECT * FROM history")
    java.util.List<History> getAllHistories();

    // Get a history record by its ID
    @androidx.room.Query("SELECT * FROM history WHERE id = :id")
    History getHistoryById(int id);

    // 指定期間の消費量合計を取得
    @androidx.room.Query("SELECT SUM(quantity) FROM history WHERE type = 'output' AND date BETWEEN :startDate AND :endDate")
    LiveData<Integer> getConsumptionSumByDateRange(LocalDate startDate, LocalDate endDate);

    // 指定期間のアイテム別消費量合計を取得
    @androidx.room.Query("SELECT SUM(quantity) FROM history WHERE id = :itemId AND type = 'output' AND date BETWEEN :startDate AND :endDate")
    LiveData<Integer> getConsumptionSumForItemByDateRange(int itemId, LocalDate startDate, LocalDate endDate);

    // 指定期間の "output" 履歴リストを取得
    @androidx.room.Query("SELECT * FROM history WHERE type = 'output' AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<History>> getOutputHistoryByDateRange(LocalDate startDate, LocalDate endDate);

    // 特定アイテムの "output" 履歴リストを日付の新しい順に取得 (追加)
    @androidx.room.Query("SELECT * FROM history WHERE id = :itemId AND type = 'output' ORDER BY date DESC")
    List<History> getOutputHistoryForItemDesc(int itemId);

    // "output" または "delete" タイプの全履歴を日付の新しい順に取得 (追加)
    @androidx.room.Query("SELECT * FROM history WHERE type = 'output' OR type = 'delete' ORDER BY date DESC")
    LiveData<List<History>> getAllOutputAndDeleteHistorySortedDesc();
}
