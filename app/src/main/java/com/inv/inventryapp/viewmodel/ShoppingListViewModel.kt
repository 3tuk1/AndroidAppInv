package com.inv.inventryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.inv.inventryapp.model.entity.ShoppingList
import com.inv.inventryapp.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers

// AndroidViewModelからViewModelに変更し、Repositoryをコンストラクタで受け取る
class ShoppingListViewModel(
    private val repository: ShoppingListRepository
) : ViewModel() {

    /**
     * ShoppingListRepositoryから購入リストのデータを取得します。
     * I/O処理のため、Dispatchers.IOコンテキストで実行します。
     */
    val shoppingList: LiveData<List<ShoppingList>> = liveData(Dispatchers.IO) {
        emit(repository.shoppingList)
    }
}
