package com.inv.inventryapp.view.analysis

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inv.inventryapp.databinding.ListItemShoppingBinding
import com.inv.inventryapp.model.entity.ShoppingList

class ShoppingListAdapter : ListAdapter<ShoppingList, ShoppingListAdapter.ShoppingListViewHolder>(DiffCallback) {

    // 長押しリスナーのコールバックを定義
    var onItemLongClickListener: ((ShoppingList) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        val binding = ListItemShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShoppingListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        // itemViewに長押しリスナーを設定
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.invoke(item)
            true
        }
    }

    class ShoppingListViewHolder(private val binding: ListItemShoppingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShoppingList) {
            binding.shoppingItemName.text = item.productName
            binding.shoppingItemQuantity.text = item.quantity.toString()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ShoppingList>() {
        override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
            return oldItem.listId == newItem.listId
        }

        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
            return oldItem == newItem
        }
    }
}
