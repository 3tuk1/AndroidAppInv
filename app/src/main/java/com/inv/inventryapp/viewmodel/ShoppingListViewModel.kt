package com.inv.inventryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inv.inventryapp.model.entity.ShoppingList
import com.inv.inventryapp.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// AndroidViewModelからViewModelに変更し、Repositoryをコンストラクタで受け取る
class ShoppingListViewModel(
    private val repository: ShoppingListRepository
) : ViewModel() {

    // LiveDataがリポジトリのメソッドを直接参照するように変更
    val shoppingList: LiveData<List<ShoppingList>> = repository.getShoppingList()

    /**
     * 引数で受け取ったアイテムを削除します。
     * I/O処理のため、Dispatchers.IOコンテキストで実行します。
     */
    fun delete(item: ShoppingList) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(item)
    }
}
