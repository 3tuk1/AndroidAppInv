package com.inv.inventryapp.di

import android.content.Context
import com.inv.inventryapp.model.ModelDatabase
import com.inv.inventryapp.repository.HistoryRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.usecase.HistoryUseCase
import com.inv.inventryapp.viewmodel.ProductEditViewModelFactory

object Injector {

    private fun provideProductRepository(context: Context): ProductRepository {
        return ProductRepository(context.applicationContext)
    }

    private fun provideHistoryRepository(context: Context): HistoryRepository {
        return HistoryRepository(context.applicationContext)
    }

    private fun provideHistoryUseCase(context: Context): HistoryUseCase {
        val historyRepository = provideHistoryRepository(context)
        return HistoryUseCase(historyRepository)
    }

    fun provideProductEditViewModelFactory(context: Context): ProductEditViewModelFactory {
        val productRepository = provideProductRepository(context)
        val historyUseCase = provideHistoryUseCase(context)
        return ProductEditViewModelFactory(productRepository, historyUseCase)
    }
}
