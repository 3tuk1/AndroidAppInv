package com.inv.inventryapp.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.productRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productListAdapter
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
