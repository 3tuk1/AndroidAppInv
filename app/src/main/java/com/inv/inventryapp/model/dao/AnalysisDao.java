package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.OnConflictStrategy; // インポート
import java.util.List;
import com.inv.inventryapp.model.entity.Analysis;

@Dao
public interface AnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // 競合した場合に置換する
    void insert(Analysis analysis);

    @Update
    void update(Analysis analysis);

    @Delete
    void delete(Analysis analysis);

    @Query("SELECT * FROM analysis")
    List<Analysis> getAll();

    // 追加: 商品名で分析結果を検索する
    @Query("SELECT * FROM analysis WHERE product_name = :productName LIMIT 1")
    Analysis findByProductName(String productName);

    @Query("DELETE FROM analysis")
    void deleteAll();
}
