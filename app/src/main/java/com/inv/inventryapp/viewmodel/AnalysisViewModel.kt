package com.inv.inventryapp.viewmodel;

public class AnalysisViewModel {
    /**
     * AnalysisViewModel
     * 分析画面のViewModelクラス
     * データの取得や更新を行うためのメソッドを提供します。
     * ここでは、消費分析のユースケースを使用してデータを取得します。
     */

    private ConsumptionAnalysisUseCase consumptionAnalysisUseCase;

    public AnalysisViewModel(ConsumptionAnalysisUseCase useCase) {
        this.consumptionAnalysisUseCase = useCase;
    }

    // データ取得メソッドなどを追加することができます
}

