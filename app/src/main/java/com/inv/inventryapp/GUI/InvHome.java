package com.inv.inventryapp.GUI;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.InventoryFragment;

public class InvHome extends commonActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 前にsavedInstanceStateがnullでない場合は、前の状態を復元する
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton scanButton = findViewById(R.id.scan_button);

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

        initCommonActivity(savedInstanceState);

        // MainActivity内で
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            com.inv.inventryapp.GUI.menuButton popupHelper = new com.inv.inventryapp.GUI.menuButton(this);
            popupHelper.showPopupMenu(v);
        });
        // FABのクリックリスナー
        scanButton.setOnClickListener(v -> {
            // カメラ/スキャン画面を開く
            // Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
            // startActivity(intent);
            showCameraOptions();
        });
    }


}
