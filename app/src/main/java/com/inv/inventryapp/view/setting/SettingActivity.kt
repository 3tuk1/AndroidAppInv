package com.inv.inventryapp.view.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inv.inventryapp.view.analysis.AnalysisActivity
import com.inv.inventryapp.R
import com.inv.inventryapp.di.Injector
import com.inv.inventryapp.model.entity.History
import com.inv.inventryapp.model.entity.Product
import com.inv.inventryapp.view.home.HomeActivity
import com.inv.inventryapp.view.saving.SavingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // ▼▼▼ テストボタンのロジックをここに追加 ▼▼▼
        findViewById<Button>(R.id.button_run_analysis_test).setOnClickListener {
            Toast.makeText(this, "分析テストを実行します...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch(Dispatchers.IO) {
                runAnalysisTest()
                runOnUiThread {
                    Toast.makeText(this@SettingActivity, "分析が完了しました。購入リストや履歴を再表示して結果を確認してください。", Toast.LENGTH_LONG).show()
                }
            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.selectedItemId = R.id.navigation_settings

        val menuOrder = listOf(R.id.navigation_home, R.id.navigation_savings, R.id.navigation_analysis, R.id.navigation_settings)
        val currentIndex = menuOrder.indexOf(R.id.navigation_settings)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener false
            }

            val intent = when (item.itemId) {
                R.id.navigation_home -> Intent(this, HomeActivity::class.java)
                R.id.navigation_savings -> Intent(this, SavingActivity::class.java)
                R.id.navigation_analysis -> Intent(this, AnalysisActivity::class.java)
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

    /**
     * 分析機能のテストを実行するためのメソッド
     */
    private fun runAnalysisTest() {
        // Injectorから各コンポーネントを取得 (contextとしてthisを渡す)
        val productRepo = Injector.provideProductRepository(this)
        val historyRepo = Injector.provideHistoryRepository(this)
        val analysisRepo = Injector.provideAnalysisRepository(this)
        val useCase = Injector.provideConsumptionAnalysisUseCase(this)

        // 1. 既存のテスト関連データをクリーンアップ
        analysisRepo.deleteAll()
        historyRepo.deleteAll()
        productRepo.deleteAll()

        // 2. テストデータを投入
        val today = LocalDate.now()
        val testProduct = Product()
        testProduct.productName = "テストヨーグルト"
        testProduct.quantity = 2

        productRepo.addProduct(testProduct)

        val history1 = History("テストヨーグルト", "消費", today.minusDays(10), 2)
        val history2 = History("テストヨーグルト", "消費", today, 2)
        historyRepo.addHistory(history1)
        historyRepo.addHistory(history2)

        // 3. 分析ユースケースを実行
        useCase.analyzeAndSave()
    }
}
