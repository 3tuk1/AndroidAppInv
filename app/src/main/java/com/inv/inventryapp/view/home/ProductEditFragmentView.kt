package com.inv.inventryapp.view.home

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.inv.inventryapp.databinding.FragmentProductEditBinding
import com.inv.inventryapp.di.Injector
import com.inv.inventryapp.model.entity.Product
import com.inv.inventryapp.viewmodel.ProductEditViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ProductEditFragmentView : Fragment() {
    private var _binding: FragmentProductEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductEditViewModel by viewModels {
        Injector.provideProductEditViewModelFactory(requireContext())
    }

    // バーコードスキャナの結果を受け取るランチャー
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { barcode ->
            updateBarcode(barcode)
        }
    }

    // 画像トリミングの結果を受け取るランチャー
    private val imageCropperLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                saveImageAndUpdateViewModel(uri)
            }
        } else {
            // エラー処理（任意）
            val exception = result.error
            exception?.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getInt("PRODUCT_ID")?.let { productId ->
            if (productId != 0) {
                viewModel.loadProduct(productId)
            } else {
                // 新規作成の場合、空のProductオブジェクトをセット
                viewModel.product.value = Product()
            }
        }
        // 新規追加時にバーコードが渡された場合
        arguments?.getString("barcode")?.let {
            updateBarcode(it)
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // 賞味期限入力欄のクリックリスナー
        binding.editTextExpirationDate.setOnClickListener {
            showDatePickerDialog()
        }
        // バーコード入力欄のクリックリスナー
        binding.editTextBarcode.setOnClickListener {
            launchBarcodeScanner()
        }
        // 画像ビューのクリックリスナー
        binding.imageViewProduct.setOnClickListener {
            launchImageCropper()
        }
        // 保存ボタン
        binding.buttonSave.setOnClickListener {
            saveProduct()
        }
        // 削除ボタン
        binding.buttonDelete.setOnClickListener {
            viewModel.product.value?.let {
                if (it.productId != 0) {
                    viewModel.onDelete(it.productId)
                }
            }
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.product.observe(viewLifecycleOwner, Observer { product ->
            product ?: return@Observer
            binding.editTextProductName.setText(product.productName)
            binding.editTextPrice.setText(product.price?.toString() ?: "")
            binding.editTextQuantity.setText(product.quantity?.toString() ?: "")
            binding.editTextLocation.setText(product.location)
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            binding.editTextExpirationDate.setText(product.expirationDate?.format(formatter) ?: "")
            binding.editTextBarcode.setText(product.barcode.barcodeNumber.let { if(it == 0L) "" else it.toString() })

            if (product.imagePath.isNotBlank()) {
                binding.imageViewProduct.setImageURI(Uri.fromFile(File(product.imagePath)))
            } else {
                // 画像がない場合のプレースホルダーなどを設定（任意）
                // binding.imageViewProduct.setImageResource(R.drawable.placeholder)
            }
        })
    }

    /**
     * 【重要】UIの現在の入力内容をViewModelに反映させるヘルパー関数
     */
    private fun updateViewModelFromUi() {
        val currentProduct = viewModel.product.value ?: Product()
        val updatedProduct = currentProduct.copy(
            productName = binding.editTextProductName.text.toString(),
            price = binding.editTextPrice.text.toString().toIntOrNull(),
            quantity = binding.editTextQuantity.text.toString().toIntOrNull(),
            location = binding.editTextLocation.text.toString(),
            barcode = currentProduct.barcode.copy(
                // ★ toIntOrNull() を toLongOrNull() に変更
                barcodeNumber = binding.editTextBarcode.text.toString().toLongOrNull() ?: 0L
            )
        )
        // LiveDataを更新するが、Observerは再帰的に呼ばれない
        if (viewModel.product.value != updatedProduct) {
            viewModel.product.value = updatedProduct
        }
    }

    private fun launchBarcodeScanner() {
        updateViewModelFromUi() // ★実行前にUIの情報をViewModelに同期
        val options = ScanOptions()
        options.setPrompt("バーコードをスキャンしてください")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        barcodeLauncher.launch(options)
    }

    private fun launchImageCropper() {
        updateViewModelFromUi() // ★実行前にUIの情報をViewModelに同期
        val cropOptions = CropImageOptions().apply {
            guidelines = CropImageView.Guidelines.ON
            aspectRatioX = 1
            aspectRatioY = 1
            fixAspectRatio = true
            outputCompressQuality = 50
        }
        val contractOptions = CropImageContractOptions(null, cropOptions)
        imageCropperLauncher.launch(contractOptions)
    }

    private fun showDatePickerDialog() {
        updateViewModelFromUi() // ★実行前にUIの情報をViewModelに同期
        val today = LocalDate.now()
        val initialDate = viewModel.product.value?.expirationDate ?: today
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateExpirationDate(selectedDate)
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).show()
    }

    private fun updateBarcode(barcode: String){
        // ★ toIntOrNull() を toLongOrNull() に変更
        val barcodeLong = barcode.toLongOrNull() ?: 0L
        val currentProduct = viewModel.product.value ?: Product()
        viewModel.product.value = currentProduct.copy(
            barcode = currentProduct.barcode.copy(barcodeNumber = barcodeLong)
        )
    }

    private fun updateExpirationDate(date: LocalDate) {
        // この時点の`viewModel.product.value`はUIと同期済みなので安全
        val currentProduct = viewModel.product.value ?: Product()
        viewModel.product.value = currentProduct.copy(expirationDate = date)
    }

    private fun saveImageAndUpdateViewModel(uri: Uri) {
        // この時点の`viewModel.product.value`はUIと同期済みなので安全
        val context = requireContext().applicationContext
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "cropped_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)

        val currentProduct = viewModel.product.value ?: Product()
        viewModel.product.value = currentProduct.copy(imagePath = file.absolutePath)
    }

    private fun saveProduct() {
        updateViewModelFromUi() // ★保存直前に最終的なUI情報をViewModelに同期
        viewModel.onInputComplete()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
