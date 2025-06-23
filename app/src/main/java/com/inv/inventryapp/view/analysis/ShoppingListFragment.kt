package com.inv.inventryapp.view.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.inv.inventryapp.databinding.FragmentAnalysisShoppingListBinding
import com.inv.inventryapp.di.Injector
import com.inv.inventryapp.viewmodel.ShoppingListViewModel

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentAnalysisShoppingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShoppingListViewModel by viewModels {
        Injector.provideShoppingListViewModelFactory(requireContext())
    }

    private lateinit var shoppingListAdapter: ShoppingListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        shoppingListAdapter = ShoppingListAdapter()

        // アダプターに長押しリスナーを実装
        shoppingListAdapter.onItemLongClickListener = { shoppingItem ->
            viewModel.delete(shoppingItem)
            Toast.makeText(context, "${shoppingItem.productName} をリストから削除しました", Toast.LENGTH_SHORT).show()
        }

        binding.shoppingListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shoppingListAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.shoppingList.observe(viewLifecycleOwner) { list ->
            list?.let {
                shoppingListAdapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.shoppingListRecyclerView.adapter = null
        _binding = null
    }
}
