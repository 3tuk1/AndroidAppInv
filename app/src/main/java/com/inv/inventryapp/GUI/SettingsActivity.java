package com.inv.inventryapp.GUI;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.SettingsFragment;

public class SettingsActivity extends commonActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nomain);

        // 設定画面のタイトルを設定
        setTitle("設定");
        settings();
        initCommonActivity(savedInstanceState);

        // SettingsFragmentをfragment_containerに追加
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment.newInstance())
                .commit();
        }
    }

    @Override
    public void onBackStackChanged() {
        // フラグメントバックスタックの変更時の処理
    }
}
