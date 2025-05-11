package com.inv.inventryapp.GUI;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.PopupMenu;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.InventoryFragment;

public class menuButton {
    private Context context;

    // コンストラクタでContextを受け取る
    public menuButton(Context context) {
        this.context = context;
    }

    // ポップアップメニューを表示するメソッド
    public void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(new ContextThemeWrapper(context, R.style.AppTheme_PopupOverlay), view);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_settings) {
                // 設定画面を開く
                return true;
            } else if(itemId == R.id.action_update){
                InventoryFragment fragment = new InventoryFragment();
                fragment.loadFoodItems();
                return true;
            }else if(itemId == R.id.action_delete_all){
                return true;
            }
            return false;
        });

        popup.show();
    }
}