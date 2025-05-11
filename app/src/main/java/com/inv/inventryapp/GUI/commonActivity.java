package com.inv.inventryapp.GUI;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inv.inventryapp.MainActivity;
import com.inv.inventryapp.R;
import com.inv.inventryapp.camera.BarcodeScannerActivity;
import com.inv.inventryapp.camera.BaseCameraActivity;
import com.inv.inventryapp.camera.ExpiryDateScannerActivity;
import com.inv.inventryapp.camera.ReceiptScannerActivity;
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
            showCameraOptions();
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
    private void showCameraOptions() {
        String[] options = {"バーコードスキャン", "レシート読み取り", "賞味期限スキャン"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("カメラ機能を選択")
                .setItems(options, (dialog, which) -> {
                    Intent intent = null;
                    switch (which) {
                        case 0: // バーコードスキャン
                            intent = new Intent(this, BarcodeScannerActivity.class);
                            break;
                        case 1: // レシート読み取り
                            intent = new Intent(this, ReceiptScannerActivity.class);
                            break;
                        case 2: // 賞味期限スキャン
                            intent = new Intent(this, ExpiryDateScannerActivity.class);
                            break;
                    }
                    if (intent != null) {
                        startActivity(intent);
                    }
                })
                .show();
    }
}
