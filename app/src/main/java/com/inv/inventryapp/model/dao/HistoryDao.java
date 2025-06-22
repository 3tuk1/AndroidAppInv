package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;
import androidx.lifecycle.LiveData;
import java.util.List;
import com.inv.inventryapp.model.entity.History;

@Dao
public interface HistoryDao {
    @Insert
    void insert(History history);

    @Update
    void update(History history);

    @Delete
    void delete(History history);

    @Query("SELECT * FROM history ORDER BY date DESC")
    List<History> getAll();

    @Query("SELECT * FROM history WHERE strftime('%Y-%m', date) = :yearMonth")
    LiveData<List<History>> getHistoriesForMonth(String yearMonth);
}
