package com.inv.inventryapp.view.analysis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.inv.inventryapp.R
import com.inv.inventryapp.databinding.ActivityAnalysisBinding
import com.inv.inventryapp.view.home.HomeActivity
import com.inv.inventryapp.view.saving.SavingActivity
import com.inv.inventryapp.view.setting.SettingActivity

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
        setupBottomNavigation()
    }

    private fun setupTabs() {
        val adapter = AnalysisFragmentAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "購入リスト"
                1 -> "カレンダー"
                2 -> "履歴"
                else -> null
            }
        }.attach()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.navigation_analysis

        val menuOrder = listOf(R.id.navigation_home, R.id.navigation_savings, R.id.navigation_analysis, R.id.navigation_settings)
        val currentIndex = menuOrder.indexOf(R.id.navigation_analysis)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == binding.bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener false
            }

            val intent = when (item.itemId) {
                R.id.navigation_home -> Intent(this, HomeActivity::class.java)
                R.id.navigation_savings -> Intent(this, SavingActivity::class.java)
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

