package com.inv.inventryapp.view.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inv.inventryapp.R
import com.inv.inventryapp.databinding.ProductListItemBinding
import com.inv.inventryapp.model.entity.Product
import java.io.File

class ProductListAdapter : ListAdapter<Product, ProductListAdapter.ProductViewHolder>(ProductDiffCallback()) {

    var onItemLongClickListener: ((Product, View) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.invoke(product, it)
            true
        }
    }

    class ProductViewHolder(private val binding: ProductListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.itemName.text = product.productName
            binding.itemQuantity.text = "個数: ${product.quantity}"

            // ★★★ ここからが追加された画像表示処理 ★★★
            if (product.imagePath.isNotBlank()) {
                val imageFile = File(product.imagePath)
                if (imageFile.exists()) {
                    // 画像ファイルが存在すれば、ImageViewに設定
                    binding.itemImage.setImageURI(Uri.fromFile(imageFile))
                } else {
                    // ファイルパスはあるがファイルが見つからない場合（任意でプレースホルダー画像などを設定）
                    binding.itemImage.setImageResource(R.drawable.ic_menu) // 仮のアイコン
                }
            } else {
                // 画像パスが保存されていない場合（任意でプレースホルダー画像などを設定）
                binding.itemImage.setImageResource(R.drawable.ic_menu) // 仮のアイコン
            }
            // ★★★ ここまで ★★★
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
