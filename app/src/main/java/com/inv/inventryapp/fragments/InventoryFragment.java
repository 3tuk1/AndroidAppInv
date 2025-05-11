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

        // アイテムをタップした時の処理
        // InventoryFragment.java の、アダプターのクリックリスナー設定例
        adapter.setOnItemClickListener((parent, itemView, position, id) -> {
            FoodItem selectedItem = filteredFoodItems.get(position);
            FoodItemFragment fragment = new FoodItemFragment();

            Bundle bundle = new Bundle();
            bundle.putLong("foodItemId", selectedItem.getId());
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
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

    public void loadFoodItems() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                allFoodItems = foodItemDao.getAllFoodItems();
                filteredFoodItems = new ArrayList<>(allFoodItems);
                adapter.updateItems(filteredFoodItems);
            });
        }
    }
}