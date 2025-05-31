package com.inv.inventryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ViewModelを使用する場合
import com.inv.inventryapp.R;
import com.inv.inventryapp.viewmodels.AnalyticsViewModel; // ViewModelを使用する場合

public class AnalysisDashboardFragment extends Fragment {

    private AnalyticsViewModel analyticsViewModel; // ViewModelを使用する場合

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ViewModelの初期化 (ActivityスコープのViewModelを共有)
        // analyticsViewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analysis_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ここでViewModelのLiveDataを監視したり、UIの初期設定を行う
    }
}

