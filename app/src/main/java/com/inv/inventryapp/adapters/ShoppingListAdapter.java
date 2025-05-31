package com.inv.inventryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            holder.itemNameTextView.setText("不明な商品"); // MainItemがnullの場合のフォールバック
        }
        holder.quantityToBuyTextView.setText(String.format(Locale.getDefault(), "購入: %d個", currentItem.getQuantityToBuy()));
        holder.reasonTextView.setText(String.format("理由: %s", currentItem.getReason()));
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
        notifyDataSetChanged(); // 簡単のため notifyDataSetChanged を使用。DiffUtil の方が効率的。
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView quantityToBuyTextView;
        TextView reasonTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            quantityToBuyTextView = itemView.findViewById(R.id.quantityToBuyTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
        }
    }
}

