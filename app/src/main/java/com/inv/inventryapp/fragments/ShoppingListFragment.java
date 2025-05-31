package com.inv.inventryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.adapters.ShoppingListAdapter;
import com.inv.inventryapp.viewmodels.AnalyticsViewModel;

public class ShoppingListFragment extends Fragment {

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

        // 買い物リストのデータをロード開始
        analyticsViewModel.loadShoppingList();
    }

    private void setupRecyclerView() {
        shoppingListAdapter = new ShoppingListAdapter();
        shoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        shoppingListRecyclerView.setAdapter(shoppingListAdapter);
    }
}

