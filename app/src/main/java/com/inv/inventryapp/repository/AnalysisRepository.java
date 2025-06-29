package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.dao.AnalysisDao;
import com.inv.inventryapp.model.entity.Analysis;
import com.inv.inventryapp.model.ModelDatabase;
import java.util.List;

public class AnalysisRepository {
    private final AnalysisDao analysisDao;

    /**
     * このコンストラクタは、AnalysisRepositoryのインスタンスを生成する際に、
     * ModelDatabaseからAnalysisDaoを取得して初期化する役割を持っています。
     */
    public AnalysisRepository(Context context) {
        ModelDatabase db = ModelDatabase.Companion.getInstance(context);
        this.analysisDao = db.analysisDao();
    }

    /**
     * 新しい分析を追加するメソッド
     *
     * @param analysis 追加する分析オブジェクト
     */
    public void addAnalysis(Analysis analysis) {
        analysisDao.insert(analysis);
    }

    /**
     * 分析を更新するメソッド
     *
     * @param analysis 更新する分析オブジェクト
     */
    public void updateAnalysis(Analysis analysis) {
        analysisDao.update(analysis);
    }

    /**
     * 分析を削除するメソッド
     *
     * @param analysis 削除する分析オブジェクト
     */
    public void deleteAnalysis(Analysis analysis) {
        analysisDao.delete(analysis);
    }

    /**
     * 全ての分析を取得するメソッド
     *
     * @return 分析のリスト
     */
    public List<Analysis> getAllAnalyses() {
        return analysisDao.getAll();
    }

    /**
     * 商品名で分析結果を検索するメソッド
     *
     * @param productName 検索する商品名
     * @return 一致する商品名の分析結果
     */
    public Analysis findByProductName(String productName) {
        return analysisDao.findByProductName(productName);
    }

    /**
     * 全ての分析データを削除するメソッド
     */
    public void deleteAll() {
        analysisDao.deleteAll();
    }
}
