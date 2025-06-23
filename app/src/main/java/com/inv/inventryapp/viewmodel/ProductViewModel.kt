package com.inv.inventryapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.inv.inventryapp.model.ModelDatabase
import com.inv.inventryapp.model.entity.History
import com.inv.inventryapp.model.entity.Product
import com.inv.inventryapp.repository.HistoryRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    private val historyRepository: HistoryRepository
    private val shoppingListRepository: ShoppingListRepository
    val allProducts: LiveData<List<Product>>

    init {
        val productDao = ModelDatabase.getInstance(application).productDao()
        repository = ProductRepository(application)
        historyRepository = HistoryRepository(application)
        shoppingListRepository = ShoppingListRepository(application)
        allProducts = repository.getAllProducts()
    }

    fun delete(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteProduct(product)
    }

    fun setQuantityToZero(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        if (product.quantity?:0 > 0) {
            val history = History(
                productName = product.productName,
                quantity = product.quantity?: 0,
                date = LocalDate.now(),
                type = "削除"
            )

            historyRepository.addHistory(history)
        }
        val updatedProduct = product.copy(quantity = 0)
        repository.updateProduct(updatedProduct)

        shoppingListRepository.addShoppingList(product.productName, 1)
    }
}
