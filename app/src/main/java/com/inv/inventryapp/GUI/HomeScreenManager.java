package com.inv.inventryapp.GUI;

import android.content.Context;
import android.view.View;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.InventoryFragment;

public class HomeScreenManager {

    private Context context;
    private FragmentManager fragmentManager;

    public HomeScreenManager(FragmentActivity activity) {
        this.context = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
    }

    // ホーム画面（在庫一覧）を表示するメソッド
    public void showHomeScreen() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new InventoryFragment())
                .commit();
    }

    // アイテムをフィルタリングするメソッド
    public void filterItems(String category) {
        InventoryFragment currentFragment =
                (InventoryFragment) fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment != null) {
            currentFragment.filterByCategory(category);
        }
    }

    // 在庫アイテムの並び替えメソッド
    public void sortItems(String sortBy) {
        InventoryFragment currentFragment =
                (InventoryFragment) fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment != null) {
            currentFragment.sortItems(sortBy);
        }
    }
}