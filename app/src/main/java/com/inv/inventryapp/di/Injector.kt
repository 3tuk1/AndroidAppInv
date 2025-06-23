package com.inv.inventryapp.di

import android.content.Context
import com.inv.inventryapp.repository.AnalysisRepository
import com.inv.inventryapp.repository.HistoryRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.repository.ShoppingListRepository
import com.inv.inventryapp.usecase.ConsumptionAnalysisUseCase
import com.inv.inventryapp.usecase.HistoryUseCase
import com.inv.inventryapp.viewmodel.HistoryViewModelFactory
import com.inv.inventryapp.viewmodel.ProductEditViewModelFactory
import com.inv.inventryapp.viewmodel.ShoppingListViewModelFactory

object Injector {

    // --- Repositories ---
    // private を削除して public (または internal) に変更
    fun provideProductRepository(context: Context): ProductRepository {
        return ProductRepository(context.applicationContext)
    }

    fun provideHistoryRepository(context: Context): HistoryRepository {
        return HistoryRepository(context.applicationContext)
    }

    fun provideShoppingListRepository(context: Context): ShoppingListRepository {
        return ShoppingListRepository(context.applicationContext)
    }

    fun provideAnalysisRepository(context: Context): AnalysisRepository {
        return AnalysisRepository(context.applicationContext)
    }

    // --- UseCases ---
    // private を削除
    fun provideHistoryUseCase(context: Context): HistoryUseCase {
        val historyRepository = provideHistoryRepository(context)
        return HistoryUseCase(historyRepository)
    }

    fun provideConsumptionAnalysisUseCase(context: Context): ConsumptionAnalysisUseCase {
        return ConsumptionAnalysisUseCase(
            provideProductRepository(context),
            provideHistoryRepository(context),
            provideAnalysisRepository(context),
            provideShoppingListRepository(context)
        )
    }

    // --- ViewModelFactories ---
    fun provideProductEditViewModelFactory(context: Context): ProductEditViewModelFactory {
        val productRepository = provideProductRepository(context)
        val historyUseCase = provideHistoryUseCase(context)
        val shoppingListRepository = provideShoppingListRepository(context)
        val analysisRepository = provideAnalysisRepository(context)
        return ProductEditViewModelFactory(productRepository, historyUseCase, shoppingListRepository, analysisRepository)
    }

    fun provideHistoryViewModelFactory(context: Context): HistoryViewModelFactory {
        val historyRepository = provideHistoryRepository(context)
        val productRepository = provideProductRepository(context)
        return HistoryViewModelFactory(historyRepository, productRepository)
    }

    fun provideShoppingListViewModelFactory(context: Context): ShoppingListViewModelFactory {
        val shoppingListRepository = provideShoppingListRepository(context)
        return ShoppingListViewModelFactory(shoppingListRepository)
    }
}
