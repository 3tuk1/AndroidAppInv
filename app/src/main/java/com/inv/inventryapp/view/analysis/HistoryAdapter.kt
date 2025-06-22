package com.inv.inventryapp.view.analysis

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inv.inventryapp.databinding.ListItemHistoryBinding
import com.inv.inventryapp.model.dto.HistoryWithPrice // 新しいデータクラスをインポート
import java.time.format.DateTimeFormatter

// ListAdapterが扱う型を<HistoryWithPrice>に変更
class HistoryAdapter : ListAdapter<HistoryWithPrice, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(private val binding: ListItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("M/d")

        // bindメソッドの引数をHistoryWithPriceに変更
        fun bind(item: HistoryWithPrice) {
            binding.historyDate.text = item.date.format(dateFormatter)
            binding.historyProductName.text = item.productName
            binding.historyType.text = item.type
            binding.historyQuantity.text = item.quantity.toString()

            // 価格をTextViewにセット。nullの場合は仮表示
            binding.historyPrice.text = item.price?.let { "${it}円" } ?: "---円"
        }
    }

    // DiffUtilの型もHistoryWithPriceに変更
    companion object DiffCallback : DiffUtil.ItemCallback<HistoryWithPrice>() {
        override fun areItemsTheSame(oldItem: HistoryWithPrice, newItem: HistoryWithPrice): Boolean {
            return oldItem.historyId == newItem.historyId
        }

        override fun areContentsTheSame(oldItem: HistoryWithPrice, newItem: HistoryWithPrice): Boolean {
            return oldItem == newItem
        }
    }
}
