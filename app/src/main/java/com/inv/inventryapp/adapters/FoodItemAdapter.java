package com.inv.inventryapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.models.Category;
import com.inv.inventryapp.models.ItemImage;
import com.inv.inventryapp.models.MainItem;
import com.inv.inventryapp.models.MainItemJoin;
import com.inv.inventryapp.utility.ConvertDate;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.inv.inventryapp.room.Converters.compressImage;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {

    /**
     * FoodItemAdapterは、RecyclerViewのアダプタークラスで、食品アイテムのリストを表示します。
     *
     * このクラスは、RecyclerViewで表示を行うためにアイテムのリスト化を行う
     */
    private List<MainItemJoin> mainItemJoins;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;

    private long itemId = 0;
    private HashMap<Integer, String> categoryIdNameMap = new HashMap<>();

    public FoodItemAdapter(List<MainItemJoin> mainItemJoins, List<Category> categories) {
        this.mainItemJoins = mainItemJoins;
        for (Category c : categories) {
            categoryIdNameMap.put(c.id, c.name);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(List<MainItemJoin> newItems) {
        this.mainItemJoins = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodItemViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        MainItemJoin itemJoin = mainItemJoins.get(position);
        MainItem item = itemJoin.mainItem;

        // メインアイテム情報を設定
        holder.nameTextView.setText(item.getName());

        // カテゴリ名の表示
        String categoryName = categoryIdNameMap.get(item.getCategoryId());
        if (categoryName != null) {
            holder.categoryTextView.setText("カテゴリ: " + categoryName);
            holder.categoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }

        // 画像を表示（結合データから）
        if (itemJoin.images != null && !itemJoin.images.isEmpty()) {
            ItemImage firstImage = itemJoin.images.get(0);
            String imagePath = firstImage.getImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    bitmap = compressImage(bitmap);
                    holder.foodImageView.setImageBitmap(bitmap);
                } else {
                    holder.foodImageView.setImageResource(R.drawable.default_food_image);
                }
            } else {
                holder.foodImageView.setImageResource(R.drawable.default_food_image);
            }
        } else {
            holder.foodImageView.setImageResource(R.drawable.default_food_image);
        }

        // 賞味期限をフォーマット
        try {
            // LocalDate をフォーマット
            if (item.getExpirationDate() != null) {
                String formattedDate = ConvertDate.localDateToString(item.getExpirationDate());
                holder.expiryDateTextView.setText("期限: " + formattedDate);
            } else {
                holder.expiryDateTextView.setText("期限: 不明");
            }
        } catch (Exception e) {
            // 変換に失敗した場合
            holder.expiryDateTextView.setText("期限: 不明");
        }

        holder.quantityTextView.setText("数量: " + item.getQuantity());

        // 場所情報を表示（結合データから）
        if (itemJoin.location != null) {
            holder.locationTextView.setText("場所: " + itemJoin.location.getLocation());
            holder.locationTextView.setVisibility(View.VISIBLE);
        } else {
            holder.locationTextView.setVisibility(View.GONE);
        }

        // クリックリスナー（短押し）
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(null, v, position, item.getId());
            }
        });

        // 長押しリスナーを追加
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(null, v, position, item.getId());
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mainItemJoins != null ? mainItemJoins.size() : 0;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public MainItemJoin getItem(int position) {
        return mainItemJoins.get(position);
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImageView;
        TextView nameTextView;
        TextView expiryDateTextView;
        TextView quantityTextView;
        TextView locationTextView;
        TextView categoryTextView;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            expiryDateTextView = itemView.findViewById(R.id.expiryDateTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }
    }
}

