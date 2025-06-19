package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;
import com.inv.inventryapp.model.entity.Analysis;

@Dao
public interface AnalysisDao {
    @Insert
    void insert(Analysis analysis);

    @Update
    void update(Analysis analysis);

    @Delete
    void delete(Analysis analysis);

    @Query("SELECT * FROM analysis")
    List<Analysis> getAll();
}

