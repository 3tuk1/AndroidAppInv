package com.inv.inventryapp.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inv.inventryapp.R
import com.inv.inventryapp.model.entity.Product

class ProductListAdapter(private var productList: List<Product>) :
    RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val nameTextView: TextView = itemView.findViewById(R.id.item_name)
        val expirationDateTextView: TextView = itemView.findViewById(R.id.item_expiration_date)
        val quantityTextView: TextView = itemView.findViewById(R.id.item_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_list_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.nameTextView.text = "商品名: ${product.productName}"
        holder.expirationDateTextView.text = "賞味期限: ${product.expirationDate?.toString() ?: "N/A"}"
        holder.quantityTextView.text = "個数: ${product.quantity?.toString() ?: "N/A"}"

        // 画像表示はGlideやPicassoなどのライブラリを使うのが一般的ですが、ここでは省略します。
        // 例: Glide.with(holder.itemView.context).load(product.imagePath).into(holder.imageView)
    }

    override fun getItemCount() = productList.size

    fun updateData(newProductList: List<Product>) {
        this.productList = newProductList
        notifyDataSetChanged()
    }
}
