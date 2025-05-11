package com.inv.inventryapp.GUI;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.InventoryFragment;

public class commonActivity extends AppCompatActivity {
    // init common activity
    protected void initCommonActivity(Bundle savedInstanceState) {
        // Initialize common components here
        // For example, set up toolbar, navigation drawer, etc.
        // デフォルトタブ選択
        FloatingActionButton scanButton = findViewById(R.id.scan_button);
        if(savedInstanceState == null) {
            loadFragment(new InventoryFragment());
        }
        // FABのクリックリスナー
        scanButton.setOnClickListener(v -> {
            // カメラ/スキャン画面を開く
            // Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
            // startActivity(intent);
        });
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_inventory);
        // タブ選択リスナー
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_inventory) {
                // Inventoryタブが選択された場合
                loadFragment(new InventoryFragment());
                return true;
            } else if (itemId == R.id.navigation_settings) {
                // Settingsタブが選択された場合
                //loadFragment();
                return true;
            }else if (itemId == R.id.navigation_analysis) {
                // Analysisタブが選択された場合
                //loadFragment();
                return true;
            }else if (itemId == R.id.navigation_saving){
                // Savingタブが選択された場合
                //loadFragment();
                return true;
            }
            return false;
        });
    }
    private void loadFragment(InventoryFragment inventoryFragment) {
        // フラグメントのトランザクションを開始
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, inventoryFragment)
                .commit();
    }
}
