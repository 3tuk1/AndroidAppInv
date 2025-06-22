package com.inv.inventryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inv.inventryapp.model.dto.HistoryWithPrice
import com.inv.inventryapp.repository.HistoryRepository
import com.inv.inventryapp.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// AndroidViewModelからViewModelに変更し、Repositoryをコンストラクタで受け取る
class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _historiesWithPrice = MutableLiveData<List<HistoryWithPrice>>()
    val historiesWithPrice: LiveData<List<HistoryWithPrice>> = _historiesWithPrice

    // Fragmentからこのメソッドを呼び出してデータを読み込む
    fun loadHistories() {
        viewModelScope.launch {
            // withContextでワーカースレッドに切り替え
            val result = withContext(Dispatchers.IO) {
                val histories = historyRepository.getAllHistories()
                histories.map { history ->
                    // 履歴の商品名から商品を検索
                    val product = productRepository.findByName(history.productName)
                    // 商品が見つかればその価格を、なければnullを設定
                    val price = product?.price

                    HistoryWithPrice(
                        historyId = history.historyId,
                        productName = history.productName,
                        type = history.type,
                        date = history.date,
                        quantity = history.quantity,
                        price = price?.times(history.quantity) //単価 * 個数
                    )
                }
            }
            // メインスレッドでLiveDataに値をセット
            _historiesWithPrice.value = result
        }
    }
}
