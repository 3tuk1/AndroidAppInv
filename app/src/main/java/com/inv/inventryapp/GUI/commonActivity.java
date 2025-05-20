package com.inv.inventryapp.GUI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.camera.BarcodeScannerActivity;
import com.inv.inventryapp.camera.ExpiryDateScannerActivity;
import com.inv.inventryapp.camera.ReceiptScannerActivity;
import com.inv.inventryapp.fragments.FoodItemFragment;
import com.inv.inventryapp.fragments.InventoryFragment;

public abstract class commonActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> barcodeScannerLauncher;

    private static int currentTab = 0;
    protected void initCommonActivity(Bundle savedInstanceState) {
        // Initialize common components here
        // For example, set up toolbar, navigation drawer, etc.
        // デフォルトタブ選択


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 現在のアクティビティに基づいてナビゲーションの選択状態を設定
        if (this instanceof InvHome) {
            bottomNav.setSelectedItemId(R.id.navigation_inventory);
            currentTab = 0;
        } else if (this instanceof SettingsActivity) {
            bottomNav.setSelectedItemId(R.id.navigation_settings);
            currentTab = 1;
        } else if (this instanceof AnalysisActivity) {
            bottomNav.setSelectedItemId(R.id.navigation_analysis);
            currentTab = 2;
        } else if (this instanceof SavingManeyActivity) {
            bottomNav.setSelectedItemId(R.id.navigation_saving);
            currentTab = 3;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int selectedTab = -1;
            Class<?> targetActivity = null;

            if (itemId == R.id.navigation_inventory) {
                selectedTab = 0;
                targetActivity = com.inv.inventryapp.GUI.InvHome.class;
            } else if (itemId == R.id.navigation_settings) {
                selectedTab = 1;
                targetActivity = com.inv.inventryapp.GUI.SettingsActivity.class;
            } else if (itemId == R.id.navigation_analysis) {
                selectedTab = 2;
                targetActivity = com.inv.inventryapp.GUI.AnalysisActivity.class;
            } else if (itemId == R.id.navigation_saving) {
                selectedTab = 3;
                targetActivity = com.inv.inventryapp.GUI.SavingManeyActivity.class;
            }

            // 同じタブが選択された場合は何もしない
            if (selectedTab == currentTab) {
                if(this instanceof InvHome) {
                    // InventoryFragmentが表示されている場合は、スキャンボタンを表示
                    getSupportFragmentManager().popBackStack();
                }
                return true;
            }

            // 新しいタブの場合
            if (targetActivity != null) {
                currentTab = selectedTab;

                Intent intent = new Intent(this, targetActivity);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }

            return true;
        });

        // ActivityResultLauncherの初期化
        barcodeScannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Bundle bdata = result.getData().getExtras();
                        if (bdata != null) {
                            FoodItemFragment fragment = new FoodItemFragment();
                            fragment.setArguments(bdata);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                });
    }
    void settings(){
        // ツールバーの設定
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // カスタムタイトルを設定
        TextView titleTextView = findViewById(R.id.toolbar_title);
        titleTextView.setText(R.string.app_name); // 必要に応じてタイトルを変更

        // ステータスバーの色を設定
        getWindow().setStatusBarColor(getResources().getColor(R.color.background_dark, getTheme()));

        // デフォルトのタイトルを非表示
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
    protected void loadFragment(InventoryFragment inventoryFragment) {
        // フラグメントのトランザクションを開始
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, inventoryFragment)
                .commit();
    }

    void showCameraOptions() {
        String[] options = {"バーコードスキャン", "レシート読み取り", "賞味期限スキャン", "手動入力"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("カメラ機能を選択")
                .setItems(options, (dialog, which) -> {
                    Intent intent = null;
                    switch (which) {
                        case 0: // バーコードスキャン
                            intent = new Intent(this, BarcodeScannerActivity.class);
                            barcodeScannerLauncher.launch(intent); // 初期化済みのランチャーを使用
                            return; // startActivityを呼ばないためreturn
                        case 1: // レシート読み取り
                            intent = new Intent(this, ReceiptScannerActivity.class);
                            break;
                        case 2: // 賞味期限スキャン
                            intent = new Intent(this, ExpiryDateScannerActivity.class);
                            break;
                        case 3: // 手動入力
                            FoodItemFragment fragment = new FoodItemFragment();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                            return; // startActivityを呼ばないためreturn
                    }
                    if (intent != null) {
                        startActivity(intent);
                    }
                })
                .show();
    }

    public abstract void onBackStackChanged();
}