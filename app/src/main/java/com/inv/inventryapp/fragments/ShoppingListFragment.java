package com.inv.inventryapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.adapters.ShoppingListAdapter;
import com.inv.inventryapp.models.ShoppingListItem;
import com.inv.inventryapp.viewmodels.AnalyticsViewModel;

public class ShoppingListFragment extends Fragment implements ShoppingListAdapter.OnShoppingListItemInteractionListener {

    private AnalyticsViewModel analyticsViewModel;
    private ShoppingListAdapter shoppingListAdapter;
    private RecyclerView shoppingListRecyclerView;
    private TextView emptyShoppingListTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsViewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        shoppingListRecyclerView = view.findViewById(R.id.shoppingListRecyclerView);
        emptyShoppingListTextView = view.findViewById(R.id.emptyShoppingListTextView);

        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        analyticsViewModel.getShoppingListData().observe(getViewLifecycleOwner(), shoppingListItems -> {
            if (shoppingListItems == null || shoppingListItems.isEmpty()) {
                shoppingListRecyclerView.setVisibility(View.GONE);
                emptyShoppingListTextView.setVisibility(View.VISIBLE);
            } else {
                shoppingListRecyclerView.setVisibility(View.VISIBLE);
                emptyShoppingListTextView.setVisibility(View.GONE);
                shoppingListAdapter.submitList(shoppingListItems);
            }
        });

        analyticsViewModel.loadShoppingList();
    }

    private void setupRecyclerView() {
        shoppingListAdapter = new ShoppingListAdapter();
        shoppingListAdapter.setOnShoppingListItemInteractionListener(this);
        shoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        shoppingListRecyclerView.setAdapter(shoppingListAdapter);
    }

    @Override
    public void onEditItem(ShoppingListItem item) {
        if (item != null && item.getMainItem() != null) {
            EditShoppingListItemFragment editFragment = new EditShoppingListItemFragment();
            Bundle args = new Bundle();
            args.putInt("itemId", item.getMainItem().getId());
            editFragment.setArguments(args);

            if (getActivity() != null) {
                // fragment_container は、Activityのレイアウトファイルに定義されたFragmentをホストするコンテナのIDに置き換えてください。
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment) // 例: R.id.fragment_container
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            Toast.makeText(getContext(), "編集対象のアイテム情報がありません。", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteItem(ShoppingListItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("アイテム削除")
                .setMessage(item.getMainItem().getName() + " を買い物リストから削除しますか？")
                .setPositiveButton("削除", (dialog, which) -> {
                    analyticsViewModel.deleteShoppingListItem(item);
                    Toast.makeText(getContext(), item.getMainItem().getName() + " を買い物リストの推奨から削除しました", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }
}
