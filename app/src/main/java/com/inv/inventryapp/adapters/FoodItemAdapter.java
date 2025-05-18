package com.inv.inventryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.models.FoodItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {

    /**
     * FoodItemAdapterは、RecyclerViewのアダプタークラスで、食品アイテムのリストを表示します。
     *
     */
    private List<FoodItem> foodItems;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
    private AdapterView.OnItemClickListener onItemClickListener;

    private long itemId = 0;
    public FoodItemAdapter(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    public void updateItems(List<FoodItem> newItems) {
        this.foodItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);
        holder.nameTextView.setText(item.getName());
        // 画像を表示する場合
        if (item.getImage() != null) {
            holder.foodImageView.setImageBitmap(item.getImage());
        } else {
            holder.foodImageView.setImageResource(R.drawable.default_food_image); // デフォルト画像
        }
        // 賞味期限をフォーマット
        try {
            // String を Date に変換
            Date expiryDate = dateFormat.parse(item.getExpiryDate());
            String formattedDate = DateFormat.getDateInstance().format(expiryDate);
            holder.expiryDateTextView.setText("賞味期限: " + formattedDate);
        } catch (Exception e) {
            // 変換に失敗した場合
            holder.expiryDateTextView.setText("賞味期限: 不明");
        }
        holder.quantityTextView.setText("数量: " + item.getQuantity());
        // カテゴリも表示する場合
        // holder.categoryTextView.setText("カテゴリ: " + item.getCategory());
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(null, v, position, itemId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImageView;
        TextView nameTextView;
        TextView expiryDateTextView;
        TextView quantityTextView;
        // TextView categoryTextView;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            expiryDateTextView = itemView.findViewById(R.id.expiryDateTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            // categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }
    }
}