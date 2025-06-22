package com.inv.inventryapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inv.inventryapp.model.entity.Product
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.usecase.HistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

// クラス図に基づき、RepositoryとUseCaseを注入する
class ProductEditViewModel(
    private val productRepository: ProductRepository,
    private val historyUseCase: HistoryUseCase
) : ViewModel() {

    // クラス図の xmlLiveData に相当。Fragmentのコードから 'product' と推測
    val product = MutableLiveData<Product>()

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            val loadedProduct = withContext(Dispatchers.IO) {
                productRepository.findById(productId)
            }
            loadedProduct?.let {
                product.value = it
            }
        }
    }

    fun updateProduct(
        productName: String,
        price: Int?,
        quantity: Int?,
        location: String,
        barcode: Int?
    ) {
        val currentProduct = product.value ?: Product()
        val updatedProduct = currentProduct.copy(
            productName = productName,
            price = price,
            quantity = quantity,
            location = location,
            barcode = currentProduct.barcode.copy(barcodeNumber = barcode ?: currentProduct.barcode.barcodeNumber)
        )
        product.value = updatedProduct
    }

    /**
     * 保存ボタンが押されたときに呼び出される。
     * 現在のproduct LiveDataの値をリポジトリに保存する。
     */
    fun onInputComplete() {
        product.value?.let { productToSave ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    if (productToSave.productId == 0) { // 新規商品の場合
                        productRepository.addProduct(productToSave)
                        if ((productToSave.quantity ?: 0) > 0) {
                            onQuantityChanged(productToSave.productName, productToSave.quantity ?: 0, "購入")
                        }
                    } else { // 既存商品の場合
                        val originalProduct = productRepository.findById(productToSave.productId)
                        originalProduct?.let {
                            val oldQuantity = it.quantity ?: 0
                            val newQuantity = productToSave.quantity ?: 0
                            val quantityChange = newQuantity - oldQuantity

                            if (quantityChange > 0) {
                                onQuantityChanged(productToSave.productName, quantityChange, "購入")
                            } else if (quantityChange < 0) {
                                onQuantityChanged(productToSave.productName, -quantityChange, "消費")
                            }
                        }
                        productRepository.updateProduct(productToSave)
                    }
                }
            }
        }
    }

    /**
     * 削除ボタンが押されたときに呼び出される。
     */
    fun onDelete(productId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val productToUpdate = productRepository.findById(productId)
                productToUpdate?.let {
                    if ((it.quantity ?: 0) > 0) {
                        onQuantityChanged(it.productName, it.quantity ?: 0, "消費")
                    }
                    val updatedProduct = it.copy(quantity = 0)
                    productRepository.updateProduct(updatedProduct)
                }
            }
        }
    }

    /**
     * 数量が変更された際の履歴を記録する。
     * クラス図ではprivate
     */
    private fun onQuantityChanged(productName: String, quantity: Int, type: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                historyUseCase.addHistory(productName, type, LocalDate.now(), quantity)
            }
        }
    }
}
