package com.inv.inventryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu; // PopupMenu をインポート
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.models.ShoppingListItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<ShoppingListItem> shoppingList = new ArrayList<>();
    private OnShoppingListItemInteractionListener listener; // リスナーのフィールドを追加

    // リスナーインターフェースの定義
    public interface OnShoppingListItemInteractionListener {
        void onEditItem(ShoppingListItem item);
        void onDeleteItem(ShoppingListItem item);
    }

    // リスナーを設定するメソッド
    public void setOnShoppingListItemInteractionListener(OnShoppingListItemInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingListItem currentItem = shoppingList.get(position);
        if (currentItem.getMainItem() != null) {
            holder.itemNameTextView.setText(currentItem.getMainItem().getName());
        } else {
            holder.itemNameTextView.setText("不明な商品");
        }
        holder.quantityToBuyTextView.setText(String.format(Locale.getDefault(), "購入: %d個", currentItem.getQuantityToBuy()));
        holder.reasonTextView.setText(String.format("理由: %s", currentItem.getReason()));

        // 長押しリスナーの設定
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null && currentItem != null) { // currentItem も null でないことを確認
                showPopupMenu(v, currentItem);
            }
            return true; // trueを返すと、通常のクリックイベントは消費される
        });
    }

    private void showPopupMenu(View view, ShoppingListItem item) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.shopping_list_item_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_edit_shopping_list_item) {
                if (listener != null) {
                    listener.onEditItem(item);
                }
                return true;
            } else if (itemId == R.id.action_delete_shopping_list_item) {
                if (listener != null) {
                    listener.onDeleteItem(item);
                }
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public void submitList(List<ShoppingListItem> newShoppingList) {
        shoppingList.clear();
        if (newShoppingList != null) {
            shoppingList.addAll(newShoppingList);
        }
        notifyDataSetChanged();
    }

    // ViewHolderクラス名はそのまま ViewHolder を使用
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView quantityToBuyTextView; // XMLに合わせて修正
        TextView reasonTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            quantityToBuyTextView = itemView.findViewById(R.id.quantityToBuyTextView); // XMLのIDに合わせる
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
        }
    }
}
