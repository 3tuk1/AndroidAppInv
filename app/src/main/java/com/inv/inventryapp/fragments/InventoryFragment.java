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
import com.inv.inventryapp.models.MainItem;
import com.inv.inventryapp.models.MainItemJoin;
import com.inv.inventryapp.models.Category;
import com.inv.inventryapp.models.HiddenItem;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.MainItemDao;
import com.inv.inventryapp.room.CategoryDao;
import com.inv.inventryapp.room.HiddenItemDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.util.Log;

public class InventoryFragment extends Fragment {

    private static final String TAG = "InventoryFragment";
    private MainItemDao mainItemDao;
    private HiddenItemDao hiddenItemDao;
    private RecyclerView recyclerView; // 大量のデータのスクロール表示のためのRecyclerView
    private FoodItemAdapter adapter;
    private List<MainItemJoin> allItems;
    private List<MainItemJoin> filteredItems;
    private List<Category> categoryList = new ArrayList<>();
    private HashSet<Integer> hiddenItemIdSet = new HashSet<>();

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Contextが利用可能なタイミングでデータベースインスタンスを取得
        AppDatabase db = AppDatabase.getInstance(requireContext());
        mainItemDao = db.mainItemDao();
        CategoryDao categoryDao = db.categoryDao();
        hiddenItemDao = db.hiddenItemDao();
        executor.execute(() -> {
            categoryList.clear();
            categoryList.addAll(categoryDao.getAllCategories());
            // 非表示IDリストを取得
            hiddenItemIdSet.clear();
            for (HiddenItem hi : hiddenItemDao.getAll()) {
                hiddenItemIdSet.add(hi.itemId);
            }
            getActivity().runOnUiThread(() -> {
                adapter = new FoodItemAdapter(new ArrayList<>(), categoryList);
                recyclerView.setAdapter(adapter);
                // アイテムをタップした時の処理
                adapter.setOnItemClickListener((parent, itemView, position, id) -> {
                    // 長押し
                    // parentはRecyclerViewのインスタンス,itemViewはタップされたアイテムのView
                    // positionはタップされたアイテムの位置,long idはアイテムのID
                    if (position >= 0 && position < adapter.getItemCount()) {// アイテムがホルダーの中にあるか確認
                        MainItemJoin selectedItem = adapter.getItem(position); // 選択されたアイテムを取得
                        if (selectedItem != null && selectedItem.mainItem != null) { // 選択されたアイテムがnullでないか確認
                            // FoodItemFragmentのインスタンスを作成
                            FoodItemFragment fragment = new FoodItemFragment();

                            Bundle bundle = new Bundle();// アイテムの情報を渡すためのBundleを作成
                            bundle.putInt("itemId", selectedItem.mainItem.getId());
                            fragment.setArguments(bundle);

                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            Log.e(TAG, "選択されたアイテムまたはメインアイテムがnullです");
                        }
                    } else {
                        Log.e(TAG, "無効なポジション: " + position);
                    }
                });
                // アイテムを長押しした時の処理
                adapter.setOnItemLongClickListener((parent, itemView, position, id) -> {
                    if (position >= 0 && position < adapter.getItemCount()) {
                        MainItemJoin selectedItem = adapter.getItem(position);
                        if (selectedItem != null && selectedItem.mainItem != null) {
                            executor.execute(() -> {
                                db.DeleateId(selectedItem.mainItem.getId());
                                if (getActivity() != null) {
                                    // UIスレッドでリストを再読み込みして更新
                                    getActivity().runOnUiThread(this::loadItems);
                                }
                            });
                        } else {
                            Log.e(TAG, "長押しされたアイテムまたはメインアイテムがnullです。削除できません。");
                        }
                    } else {
                        Log.e(TAG, "長押しされたアイテムのポジションが無効です: " + position);
                    }
                    return true; // 長押しイベントを消費したことを示す
                });
                init();
            });
        });

        return view;
    }

    private void init() {
        // データベースからデータを取得して表示
        loadItems();
    }

    // 外部からデータ更新を要求するためのメソッド
    public void loadItems() {
        executor.execute(() -> {
            try {
                List<MainItemJoin> items = mainItemDao.getMainItemWithImagesAndLocationOnlyPositive();
                // 非表示IDリストを再取得
                hiddenItemIdSet.clear();
                for (HiddenItem hi : hiddenItemDao.getAll()) {
                    hiddenItemIdSet.add(hi.itemId);
                }
                // 非表示IDに含まれていないものだけを表示
                List<MainItemJoin> visibleItems = new ArrayList<>();
                for (MainItemJoin item : items) {
                    if (item != null && item.mainItem != null && !hiddenItemIdSet.contains(item.mainItem.getId())) {
                        visibleItems.add(item);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allItems = visibleItems;
                        filteredItems = new ArrayList<>(allItems);
                        adapter.updateItems(filteredItems);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "データ読み込み中にエラーが発生しました", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // エラー発生時は空のリストを表示
                        allItems = new ArrayList<>();
                        filteredItems = new ArrayList<>();
                        adapter.updateItems(filteredItems);
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 画面に戻ってきた時にデータを更新
        loadItems();
    }

    // カテゴリでフィルタリングするメソッド
    public void filterByCategory(int categoryId) {
        if (allItems == null) {
            return;
        }
        if (categoryId == -1) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = new ArrayList<>();
            for (MainItemJoin item : allItems) {
                if (item != null && item.mainItem != null &&
                        item.mainItem.getCategoryId() == categoryId) {
                    filteredItems.add(item);
                }
            }
        }
        adapter.updateItems(filteredItems);
    }

    // 名前で検索するメソッド
    public void searchByName(String query) {
        if (allItems == null) {
            return;
        }

        if (query == null || query.isEmpty()) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (MainItemJoin item : allItems) {
                if (item != null && item.mainItem != null &&
                        item.mainItem.getName() != null &&
                        item.mainItem.getName().toLowerCase().contains(lowerQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        adapter.updateItems(filteredItems);
    }

    // データを並べ替えるメソッド
    public void sortItems(SortOrder order) {
        if (filteredItems == null || filteredItems.isEmpty()) {
            return;
        }

        try {
            switch (order) {
                case NAME_ASC:
                    filteredItems.sort((o1, o2) -> {
                        if (o1 == null || o1.mainItem == null || o1.mainItem.getName() == null) return 1;
                        if (o2 == null || o2.mainItem == null || o2.mainItem.getName() == null) return -1;
                        return o1.mainItem.getName().compareTo(o2.mainItem.getName());
                    });
                    break;
                case NAME_DESC:
                    filteredItems.sort((o1, o2) -> {
                        if (o1 == null || o1.mainItem == null || o1.mainItem.getName() == null) return 1;
                        if (o2 == null || o2.mainItem == null || o2.mainItem.getName() == null) return -1;
                        return o2.mainItem.getName().compareTo(o1.mainItem.getName());
                    });
                    break;
                case EXPIRY_ASC:
                    filteredItems.sort((o1, o2) -> {
                        if (o1 == null || o1.mainItem == null || o1.mainItem.getExpirationDate() == null) return 1;
                        if (o2 == null || o2.mainItem == null || o2.mainItem.getExpirationDate() == null) return -1;
                        return o1.mainItem.getExpirationDate().compareTo(o2.mainItem.getExpirationDate());
                    });
                    break;
                case EXPIRY_DESC:
                    filteredItems.sort((o1, o2) -> {
                        if (o1 == null || o1.mainItem == null || o1.mainItem.getExpirationDate() == null) return 1;
                        if (o2 == null || o2.mainItem == null || o2.mainItem.getExpirationDate() == null) return -1;
                        return o2.mainItem.getExpirationDate().compareTo(o1.mainItem.getExpirationDate());
                    });
                    break;
            }

            adapter.updateItems(filteredItems);
        } catch (Exception e) {
            Log.e(TAG, "並べ替え中にエラーが発生しました", e);
        }
    }



    // 並べ替え順の列挙型
    public enum SortOrder {
        NAME_ASC,
        NAME_DESC,
        EXPIRY_ASC,
        EXPIRY_DESC
    }
}

