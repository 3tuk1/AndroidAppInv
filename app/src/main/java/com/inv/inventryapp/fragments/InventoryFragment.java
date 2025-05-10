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
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.FoodItemDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class InventoryFragment extends Fragment {

    private FoodItemDao foodItemDao;
    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> allFoodItems;
    private List<FoodItem> filteredFoodItems;

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Adapterの初期化
        adapter = new FoodItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Contextが利用可能なタイミングでデータベースインスタンスを取得
        foodItemDao = AppDatabase.getInstance(requireContext()).foodItemDao();

        init();
        return view;
    }

    // InventoryFragment.java の init メソッド内
    private void init() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // データベース操作
            List<FoodItem> items = foodItemDao.getAllFoodItems();

            // UIスレッドでの更新
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allFoodItems = items;
                    filteredFoodItems = new ArrayList<>(allFoodItems);
                    adapter.updateItems(filteredFoodItems);
                });
            }
        });
    }

    private void loadFoodItems() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                allFoodItems = foodItemDao.getAllFoodItems();
                filteredFoodItems = new ArrayList<>(allFoodItems);
                adapter.updateItems(filteredFoodItems);
            });
        }
    }

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

    private void DemoData() {
        FoodItem foodItem1 = new FoodItem("りんご", "1234567890123", "2024/12/31", 5, "果物");
        FoodItem foodItem2 = new FoodItem("牛乳", "1234567890124", "2024/11/30", 2, "乳製品");
        FoodItem foodItem3 = new FoodItem("パン", "1234567890125", "2024/10/15", 10, "穀物");
        FoodItem foodItem4 = new FoodItem("卵", "1234567890126", "2024/09/20", 12, "卵");
        FoodItem foodItem5 = new FoodItem("チーズ", "1234567890127", "2024/08/25", 8, "乳製品");

        executor .execute(() -> {
            foodItemDao.insert(foodItem1);
            foodItemDao.insert(foodItem2);
            foodItemDao.insert(foodItem3);
            foodItemDao.insert(foodItem4);
            foodItemDao.insert(foodItem5);
        });
    }
}