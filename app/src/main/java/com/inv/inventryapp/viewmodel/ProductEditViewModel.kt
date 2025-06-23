package com.inv.inventryapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inv.inventryapp.model.entity.Product
import com.inv.inventryapp.repository.AnalysisRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.repository.ShoppingListRepository
import com.inv.inventryapp.usecase.HistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

// クラス図に基づき、RepositoryとUseCaseを注入する
class ProductEditViewModel(
    private val productRepository: ProductRepository,
    private val historyUseCase: HistoryUseCase,
    private val shoppingListRepository: ShoppingListRepository,
    private val analysisRepository: AnalysisRepository // 追加
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
                // --- 履歴記録と購入リスト連携のため、更新前の商品情報を取得 ---
                val originalProduct = if (finalProduct.productId != 0) {
                    productRepository.findById(finalProduct.productId)
                } else {
                    null
                }

                // --- 商品の保存（新規または更新） ---
                if (finalProduct.productId == 0) {
                    Log.d("ProductEditViewModel", "Executing NEW product logic.")
                    productRepository.addProduct(finalProduct)
                    onQuantityChanged(finalProduct.productName, finalProduct.quantity ?: 0, "購入")
                } else {
                    Log.d("ProductEditViewModel", "Executing EXISTING product logic for productId: ${finalProduct.productId}")
                    originalProduct?.let {
                        val oldQuantity = it.quantity ?: 0
                        val newQuantity = finalProduct.quantity ?: 0
                        val quantityChange = newQuantity - oldQuantity

                        if (quantityChange > 0) {
                            onQuantityChanged(finalProduct.productName, quantityChange, "購入")
                        } else if (quantityChange < 0) {
                            onQuantityChanged(finalProduct.productName, -quantityChange, "消費")
                        }
                    }
                    productRepository.updateProduct(finalProduct)
                }

                // --- ★購入リスト連携ロジック（改）★ ---
                val oldQuantity = originalProduct?.quantity ?: 0
                val newQuantity = finalProduct.quantity ?: 0

                // 1. 在庫が0以下になった場合 (基本的なフォールバック)
                if (newQuantity <= 0 && oldQuantity > 0) {
                    shoppingListRepository.addShoppingList(finalProduct.productName, 1)
                }
                // 2. 在庫が補充された場合
                else if (newQuantity > 0) {
                    val shoppingListItem = shoppingListRepository.findByProductName(finalProduct.productName)
                    if(shoppingListItem != null) {
                        val analysis = analysisRepository.findByProductName(finalProduct.productName)
                        var shouldDeleteFromList = true // 基本的には削除する

                        if (analysis != null) {
                            // ただし、分析結果があり、現在の在庫が「推奨在庫量」に達していない場合は、まだ購入が必要なのでリストから削除しない
                            if (newQuantity <= analysis.recommendedQuantity) {
                                shouldDeleteFromList = false
                            }
                        }

                        if (shouldDeleteFromList) {
                            shoppingListRepository.delete(shoppingListItem)
                        }
                    }
                }
            }
        }
    }

    /**
     * 削除ボタンが押されたときに呼び出される。
     */
    fun onDelete(productId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val productToDelete = productRepository.findById(productId)
            productToDelete?.let {
                if ((it.quantity ?: 0) > 0) {
                    onQuantityChanged(it.productName, it.quantity ?: 0, "消費")
                }
                val updatedProduct = it.copy(quantity = 0)
                productRepository.updateProduct(updatedProduct)

                // 削除時に購入リストへ追加
                shoppingListRepository.addShoppingList(it.productName, 1)
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
