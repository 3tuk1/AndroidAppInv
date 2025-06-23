package com.inv.inventryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inv.inventryapp.repository.AnalysisRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.repository.ShoppingListRepository
import com.inv.inventryapp.usecase.HistoryUseCase

class ProductEditViewModelFactory(
    private val productRepository: ProductRepository,
    private val historyUseCase: HistoryUseCase,
    private val shoppingListRepository: ShoppingListRepository,
    private val analysisRepository: AnalysisRepository // 追加
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductEditViewModel(productRepository, historyUseCase, shoppingListRepository, analysisRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
