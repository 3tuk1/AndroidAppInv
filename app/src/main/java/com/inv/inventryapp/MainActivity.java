package com.inv.inventryapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.InventoryFragment;

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        FloatingActionButton scanButton = findViewById(R.id.scan_button);
        // カスタムツールバーを設定
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ステータスバーの色を黒に設定
        getWindow().setStatusBarColor(getResources().getColor(R.color.background_dark, getTheme()));

        // タイトルを非表示（カスタムタイトルを使用するため）
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        // FABのクリックリスナー
        scanButton.setOnClickListener(v -> {
            // カメラ/スキャン画面を開く
            // Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
            // startActivity(intent);
        });

        // タブ選択リスナー
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_inventory) {
                loadFragment(new InventoryFragment());
                return true;
            }
            // 必要に応じて他のタブの処理を追加

            return false;
        });

        if (savedInstanceState == null) {
            loadFragment(new InventoryFragment());
            bottomNav.setSelectedItemId(R.id.navigation_inventory);
        }

        // MainActivity内で
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            com.inv.inventryapp.GUI.menuButton popupHelper = new com.inv.inventryapp.GUI.menuButton(this);
            popupHelper.showPopupMenu(v);
        });


        // デフォルトタブ選択
        bottomNav.setSelectedItemId(R.id.navigation_inventory);
    }

    // フラグメント切り替えヘルパーメソッド
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


}
