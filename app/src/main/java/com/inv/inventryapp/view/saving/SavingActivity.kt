package com.inv.inventryapp.view.saving

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inv.inventryapp.view.analysis.AnalysisActivity
import com.inv.inventryapp.R
import com.inv.inventryapp.view.home.HomeActivity
import com.inv.inventryapp.view.setting.SettingActivity

class SavingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saving)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.selectedItemId = R.id.navigation_savings

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
                it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(it)
            }
            true
        }
    }
}
