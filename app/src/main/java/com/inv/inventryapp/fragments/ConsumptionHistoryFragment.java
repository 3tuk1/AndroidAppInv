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
import com.inv.inventryapp.adapters.ConsumptionHistoryAdapter;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.MainItemDao;
import com.inv.inventryapp.viewmodels.AnalyticsViewModel;

import java.util.List;

public class ConsumptionHistoryFragment extends Fragment {

    private ConsumptionHistoryAdapter consumptionHistoryAdapter;
    private RecyclerView consumptionHistoryRecyclerView;
    private TextView emptyHistoryTextView;
    private MainItemDao mainItemDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainItemDao = AppDatabase.getInstance(requireContext()).mainItemDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consumption_history, container, false);
        consumptionHistoryRecyclerView = view.findViewById(R.id.consumptionHistoryRecyclerView);
        emptyHistoryTextView = view.findViewById(R.id.emptyHistoryTextView);
        setupRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModelの初期化
        AnalyticsViewModel analyticsViewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);

        // 全消費履歴を観察してUIを更新
        analyticsViewModel.getAllConsumptionHistory().observe(getViewLifecycleOwner(), historyList -> {
            if (historyList != null && !historyList.isEmpty()) {
                emptyHistoryTextView.setVisibility(View.GONE);
                consumptionHistoryRecyclerView.setVisibility(View.VISIBLE);
                consumptionHistoryAdapter.submitList((List) historyList); // 型キャストを追加
            } else {
                emptyHistoryTextView.setText("消費履歴がありません");
                emptyHistoryTextView.setVisibility(View.VISIBLE);
                consumptionHistoryRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        consumptionHistoryAdapter = new ConsumptionHistoryAdapter(mainItemDao);
        consumptionHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        consumptionHistoryRecyclerView.setAdapter(consumptionHistoryAdapter);
    }
}

