package com.inv.inventryapp.view.saving

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inv.inventryapp.view.analysis.AnalysisActivity
import com.inv.inventryapp.R
import com.inv.inventryapp.view.chart.PieChartFragment
import com.inv.inventryapp.view.home.HomeActivity
import com.inv.inventryapp.view.setting.SettingActivity

class SavingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saving)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PieChartFragment())
                .commit()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.selectedItemId = R.id.navigation_savings

        val menuOrder = listOf(R.id.navigation_home, R.id.navigation_savings, R.id.navigation_analysis, R.id.navigation_settings)
        val currentIndex = menuOrder.indexOf(R.id.navigation_savings)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener false
            }

            val intent = when (item.itemId) {
                R.id.navigation_home -> Intent(this, HomeActivity::class.java)
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
}
