package com.inv.inventryapp.view.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inv.inventryapp.R
import com.inv.inventryapp.repository.ProductRepository
import com.inv.inventryapp.view.analysis.AnalysisActivity
import com.inv.inventryapp.view.saving.SavingActivity
import com.inv.inventryapp.view.setting.SettingActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity(), AddOptionsBottomSheet.AddOptionsListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductListFragment())
                .commit()
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add).setOnClickListener {
            AddOptionsBottomSheet().show(supportFragmentManager, AddOptionsBottomSheet.TAG)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.selectedItemId = R.id.navigation_home

        val menuOrder = listOf(R.id.navigation_home, R.id.navigation_savings, R.id.navigation_analysis, R.id.navigation_settings)
        val currentIndex = menuOrder.indexOf(R.id.navigation_home)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener false
            }

            val intent = when (item.itemId) {
                R.id.navigation_savings -> Intent(this, SavingActivity::class.java)
                R.id.navigation_analysis -> Intent(this, AnalysisActivity::class.java)
                R.id.navigation_settings -> Intent(this, SettingActivity::class.java)
                else -> null
            }

            intent?.let {
                val nextIndex = menuOrder.indexOf(item.itemId)
                startActivity(it)
                if (nextIndex > currentIndex) {
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
                finish()
            }
            true
        }
    }

    override fun onBarcodeAddSelected() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("バーコードをスキャンしてください")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        barcodeLauncher.launch(options)
    }

    override fun onManualAddSelected() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProductEditFragmentView())
            .addToBackStack(null)
            .commit()
    }

    // ▼▼▼ `barcodeLauncher`の処理を全面的に書き換え ▼▼▼
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { barcode ->
            val barcodeLong = barcode.toLongOrNull()
            if (barcodeLong == null) {
                Toast.makeText(this, "無効なバーコードです", Toast.LENGTH_SHORT).show()
                return@let
            }

            // データベース検索はバックグラウンドで行う
            lifecycleScope.launch {
                val repository = ProductRepository(applicationContext)
                val existingProduct = withContext(Dispatchers.IO) {
                    repository.findByBarcode(barcodeLong)
                }

                val fragment = ProductEditFragmentView().apply {
                    arguments = Bundle().apply {
                        if (existingProduct != null) {
                            // 商品が見つかった場合：編集モードで起動
                            putInt("PRODUCT_ID", existingProduct.productId)
                        } else {
                            // 商品が見つからない場合：新規作成モードで起動
                            putString("barcode", barcode)
                        }
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
    // ▲▲▲ ここまで ▲▲▲
}
