package com.inv.inventryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.adapters.FoodItemAdapter;
import com.inv.inventryapp.models.FoodItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> allFoodItems;
    private List<FoodItem> filteredFoodItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // データの初期化
        allFoodItems = getDummyData();
        filteredFoodItems = new ArrayList<>(allFoodItems);

        adapter = new FoodItemAdapter(filteredFoodItems);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // カテゴリでフィルタリングするメソッド
    public void filterByCategory(String category) {
        if (category == null || category.isEmpty() || category.equals("すべて")) {
            filteredFoodItems = new ArrayList<>(allFoodItems);
        } else {
            filteredFoodItems = allFoodItems.stream()
                    .filter(item -> category.equals(item.getCategory()))
                    .collect(Collectors.toList());
        }

        adapter.updateItems(filteredFoodItems);
    }

    // アイテムを並び替えるメソッド
    public void sortItems(String sortBy) {
        switch (sortBy) {
            case "名前":
                Collections.sort(filteredFoodItems, Comparator.comparing(FoodItem::getName));
                break;
            case "賞味期限":
                Collections.sort(filteredFoodItems, Comparator.comparing(FoodItem::getExpiryDate));
                break;
            case "数量":
                Collections.sort(filteredFoodItems, Comparator.comparing(FoodItem::getQuantity));
                break;
        }

        adapter.notifyDataSetChanged();
    }

    private List<FoodItem> getDummyData() {
        List<FoodItem> items = new ArrayList<>();
        items.add(new FoodItem("りんご", "4901234567890", new Date(), 3, "果物"));
        items.add(new FoodItem("バナナ", "4901234567891", new Date(), 5, "果物"));
        items.add(new FoodItem("牛乳", "4901234567892", new Date(), 1, "乳製品"));
        items.add(new FoodItem("ヨーグルト", "4901234567893", new Date(), 2, "乳製品"));
        items.add(new FoodItem("トマト", "4901234567894", new Date(), 4, "野菜"));
        items.add(new FoodItem("レタス", "4901234567895", new Date(), 1, "野菜"));
        return items;
    }
}