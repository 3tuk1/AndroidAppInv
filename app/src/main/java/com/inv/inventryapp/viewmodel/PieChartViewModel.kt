package com.inv.inventryapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inv.inventryapp.repository.HistoryRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.usecase.PieChartDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PieChartViewModel(application: Application) : AndroidViewModel(application) {

    private val _pieData = MutableLiveData<List<Float>>()
    val pieData: LiveData<List<Float>> = _pieData

    private val historyRepository = HistoryRepository(application)
    private val productRepository = ProductRepository(application)
    private val pieChartDataUseCase = PieChartDataUseCase(historyRepository, productRepository)

    fun loadPieChartData() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val data = pieChartDataUseCase.execute(currentMonth)
            _pieData.postValue(data)
        }
    }
}
