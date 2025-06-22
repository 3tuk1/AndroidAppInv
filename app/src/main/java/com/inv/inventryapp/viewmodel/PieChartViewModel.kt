package com.inv.inventryapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.inv.inventryapp.repository.HistoryRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.usecase.PieChartDataUseCase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PieChartViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(application)
    private val productRepository = ProductRepository(application)
    private val pieChartDataUseCase = PieChartDataUseCase(historyRepository, productRepository)

    private val _currentMonth = MutableLiveData<String>()

    val pieData: LiveData<List<Float>> = _currentMonth.switchMap { month ->
        pieChartDataUseCase.execute(month)
    }

    fun loadPieChartData() {
        val currentMonthValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        if (_currentMonth.value != currentMonthValue) {
            _currentMonth.value = currentMonthValue
        }
    }
}
