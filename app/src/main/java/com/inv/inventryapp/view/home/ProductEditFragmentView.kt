package com.inv.inventryapp.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.inv.inventryapp.databinding.FragmentProductEditBinding
import com.inv.inventryapp.di.Injector
import com.inv.inventryapp.model.ModelDatabase
import com.inv.inventryapp.repository.HistoryRepository
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.usecase.HistoryUseCase
import com.inv.inventryapp.viewmodel.ProductEditViewModel
import com.inv.inventryapp.viewmodel.ProductEditViewModelFactory

class ProductEditFragmentView : Fragment() {
    private var _binding: FragmentProductEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductEditViewModel by viewModels {
        Injector.provideProductEditViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("barcode")?.let {
            binding.editTextBarcode.setText(it)
        }

        arguments?.getInt("PRODUCT_ID")?.let { productId ->
            if (productId != 0) {
                viewModel.loadProduct(productId)
            }
        }

        // LiveData監視例
        viewModel.product.observe(viewLifecycleOwner, Observer { product ->
            binding.editTextProductName.setText(product.productName)
            binding.editTextPrice.setText(product.price?.toString() ?: "")
            binding.editTextQuantity.setText(product.quantity?.toString() ?: "")
            binding.editTextLocation.setText(product.location)
            binding.editTextExpirationDate.setText(product.expirationDate?.toString() ?: "")
            binding.editTextPurchaseDate.setText(product.purchaseDate?.toString() ?: "")
            binding.editTextBarcode.setText(product.barcode?.toString() ?: "")
        })

        // 保存ボタン押下時のみViewModelへ反映
        binding.buttonSave.setOnClickListener {
            val productName = binding.editTextProductName.text.toString()
            val price = binding.editTextPrice.text.toString().toIntOrNull()
            val quantity = binding.editTextQuantity.text.toString().toIntOrNull()
            val location = binding.editTextLocation.text.toString()
            val expirationDate = binding.editTextExpirationDate.text.toString() // 日付変換は要実装
            val purchaseDate = binding.editTextPurchaseDate.text.toString() // 日付変換は要実装
            val barcode = binding.editTextBarcode.text.toString().toIntOrNull()
            // 必要に応じて他の項目も取得

            viewModel.updateProduct(
                productName,
                price ?: 0,
                quantity ?: 0,
                location,
                barcode ?: 0
            )
            viewModel.onInputComplete()

            parentFragmentManager.popBackStack()
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.product.value?.let {
                if (it.productId != 0) {
                    viewModel.onDelete(it.productId)
                }
            }
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
