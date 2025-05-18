package com.inv.inventryapp.room;

import androidx.room.Dao;
import com.inv.inventryapp.models.History;

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
}
