package com.inv.inventryapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inv.inventryapp.model.entity.Product
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.usecase.HistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

// クラス図に基づき、RepositoryとUseCaseを注入する
class ProductEditViewModel(
    private val productRepository: ProductRepository,
    private val historyUseCase: HistoryUseCase
) : ViewModel() {

    // クラス図の xmlLiveData に相当。Fragmentのコードから 'product' と推測
    val product = MutableLiveData<Product>()

    fun loadProduct(productId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedProduct = productRepository.findById(productId)
            loadedProduct?.let {
                product.postValue(it)
            }
        }
    }

    /**
     * 保存ボタンが押されたときに呼び出される。
     */
    fun onInputComplete() {
        // ���★★ デバッグ用ログ：保存処理が呼ばれた時点での商品データを確認します ★★★
        Log.d("ProductEditViewModel", "onInputComplete triggered. Product value: ${product.value}")

        product.value?.let { productToSave ->
            val finalProduct = productToSave.copy(purchaseDate = LocalDate.now())

            viewModelScope.launch(Dispatchers.IO) {
                // `productId`が0の場合は「新規商品」と判断します
                if (finalProduct.productId == 0) {

                    // ---【新規商品の処理ブロック】---
                    Log.d("ProductEditViewModel", "Executing NEW product logic.")

                    productRepository.addProduct(finalProduct)

                    // 新規作成なので、入力された数量そのものを「購入」として履歴に記録します。
                    onQuantityChanged(
                        finalProduct.productName,
                        finalProduct.quantity ?: 0,
                        "購入"
                    )

                } else {

                    // ---【既存商品の更新処理ブロック】---
                    Log.d("ProductEditViewModel", "Executing EXISTING product logic for productId: ${finalProduct.productId}")

                    val originalProduct = productRepository.findById(finalProduct.productId)
                    originalProduct?.let {
                        val oldQuantity = it.quantity ?: 0
                        val newQuantity = finalProduct.quantity ?: 0
                        // 既存商品なので、数量の「差分」を計算します。
                        val quantityChange = newQuantity - oldQuantity

                        if (quantityChange > 0) {
                            // 数量が増えた場合、その増加分を「購入」として記録します。
                            onQuantityChanged(finalProduct.productName, quantityChange, "購入")
                        } else if (quantityChange < 0) {
                            // 数量が減った場合、その減少分を「消費」として記録します。
                            onQuantityChanged(finalProduct.productName, -quantityChange, "消費")
                        }
                        // 数量に変化がない場合は、履歴に記録しません。
                    }
                    productRepository.updateProduct(finalProduct)
                }
            }
        }
    }

    /**
     * 削除ボタンが押されたときに呼び出される。
     */
    fun onDelete(productId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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

    /**
     * 数量が変更された際の履歴を記録する。
     */
    private suspend fun onQuantityChanged(productName: String, quantity: Int, type: String) {
        historyUseCase.addHistory(productName, type, LocalDate.now(), quantity)
        // ★★★ デバッグ用ログ：履歴が追加されたことを確認します ★★★
        Log.d("ProductEditViewModel", "History added: Name=$productName, Type=$type, Qty=$quantity")
    }
}
