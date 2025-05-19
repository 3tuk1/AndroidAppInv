package com.inv.inventryapp.GUI;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.material.appbar.MaterialToolbar;
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

        settings();

        initCommonActivity(savedInstanceState);

        loadFragment(new InventoryFragment());

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
    }


}
