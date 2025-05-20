package com.inv.inventryapp.GUI;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.FoodItemFragment;
import com.inv.inventryapp.fragments.InventoryFragment;

public class InvHome extends commonActivity implements FragmentManager.OnBackStackChangedListener {
    private boolean isShowingScanButton = true;
    private boolean isShowingBackButton = false;
    private FloatingActionButton scanButton;
    private ImageButton backButton;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 前にsavedInstanceStateがnullでない場合は、前の状態を復元する
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scan_button);
        backButton = findViewById(R.id.back_button);
        settings();
        setBackButton();

        initCommonActivity(savedInstanceState);

        // バックスタック変更リスナーを登録
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (savedInstanceState == null) { // 初期表示時のみInventoryFragmentをロード
            loadFragment(new InventoryFragment());
        }

        // MainActivity内で
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            com.inv.inventryapp.GUI.menuButton popupHelper = new com.inv.inventryapp.GUI.menuButton(this);
            popupHelper.showPopupMenu(v);
        });
        // FABのクリックリスナー
        scanButton.setOnClickListener(v -> {
            // カメラ/スキャン画面を開く
            showCameraOptions();
        });
        updateButtonVisibility();

    }
    public void puntoswitcher() {
        if(isShowingScanButton) {
            scanButton.setVisibility(View.VISIBLE);
        } else {
            scanButton.setVisibility(View.GONE);
        }
    }
    public void setBackButton() {
        backButton.setVisibility(View.GONE);
        backButton.setOnClickListener(v -> {
            // 戻るボタンが押されたときの処理
            getSupportFragmentManager().popBackStack();
        });
    }

    @Override
    public void onBackStackChanged() {
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        FragmentManager fm = getSupportFragmentManager();
        // 現在表示されているフラグメントを取得 (コンテナIDを指定)
        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof FoodItemFragment) {
            scanButton.setVisibility(View.GONE);
            backButton.setVisibility(View.VISIBLE);
        } else if (currentFragment instanceof InventoryFragment || fm.getBackStackEntryCount() == 0) {
            // InventoryFragmentが表示されているか、バックスタックが空の場合
            // (バックスタックが空の時は最初にロードしたInventoryFragmentが表示されている想定)
            scanButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.GONE);
        } else {
            // その他のフラグメントが表示されている場合のデフォルト（必要に応じて調整）
            scanButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.GONE);
        }
    }

    // InvHomeが破棄されるときにリスナーを解除
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportFragmentManager().removeOnBackStackChangedListener( this);
    }


}
