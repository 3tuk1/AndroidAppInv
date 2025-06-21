package com.inv.inventryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PieChartViewModel : ViewModel() {

    private val _pieData = MutableLiveData<List<Float>>()
    val pieData: LiveData<List<Float>> = _pieData

    fun loadPieChartData() {
        // ここでデータベースやリポジトリからデータを取得します
        // 例としてダミーデータを設定
        _pieData.value = listOf(10f, 20f, 30f, 40f)
    }
}

