package com.inv.inventryapp.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inv.inventryapp.R
import com.inv.inventryapp.databinding.FragmentProductListBinding
import com.inv.inventryapp.model.ModelDatabase
import com.inv.inventryapp.model.entity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productListAdapter: ProductListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productListAdapter = ProductListAdapter(emptyList())
        productListAdapter.onItemLongClickListener = { product, view ->
            showContextMenu(product, view)
        }
        binding.productRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productListAdapter
        }
    }

    private fun showContextMenu(product: Product, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.product_item_context_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    editProduct(product)
                    true
                }
                R.id.menu_delete -> {
                    deleteProduct(product)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun editProduct(product: Product) {
        val fragment = ProductEditFragmentView().apply {
            arguments = Bundle().apply {
                putInt("PRODUCT_ID", product.productId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            val productDao = ModelDatabase.getInstance(requireContext()).productDao()
            withContext(Dispatchers.IO) {
                productDao.delete(product)
            }
            loadProducts()
        }
    }

    private fun loadProducts() {
        // ViewModel経由でデータを取得するのがベストプラクティスですが、
        // ここでは簡潔にするため直接データベースから読み込みます。
        lifecycleScope.launch {
            val productDao = ModelDatabase.getInstance(requireContext()).productDao()
            val productList = withContext(Dispatchers.IO) {
                productDao.getAll()
            }
            productListAdapter.updateData(productList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
