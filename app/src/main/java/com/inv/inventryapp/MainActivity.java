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
                // 在庫一覧表示
                // loadFragment(new InventoryFragment());
                return true;
            } else if (itemId == R.id.navigation_settings) {
                // 設定画面表示
                // loadFragment(new SettingsFragment());
                return true;
            }

            return false;
        });
        // メニューボタンのクリックリスナー
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            showPopupMenu(v);
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
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(new ContextThemeWrapper(this, R.style.AppTheme_PopupOverlay), view);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());


        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_settings) {
                // 設定画面を開く
                // startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (itemId == R.id.action_about) {
                // アプリ情報ダイアログを表示
                // showAboutDialog();
                return true;
            } else if (itemId == R.id.action_logout) {
                // ログアウト処理
                // performLogout();
                return true;
            }
            return false;
        });

        popup.show();
    }
}
