package com.inv.inventryapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.inv.inventryapp.R;
import com.inv.inventryapp.utility.ManageCalendar;
import com.inv.inventryapp.viewmodels.AnalyticsViewModel;
import com.kizitonwose.calendar.view.CalendarView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AnalysisDashboardFragment extends Fragment {

    private static final String TAG = "AnalysisDashboardFrag";
    private AnalyticsViewModel analyticsViewModel;
    private CalendarView calendarView;
    private TextView consumptionPredictionTextView;
    private TextView monthTextView;
    private ManageCalendar calendarManager;

    private final DateTimeFormatter monthDisplayFormatter = DateTimeFormatter.ofPattern("yyyy年 MMMM", Locale.JAPANESE);
    private LocalDate selectedDate = LocalDate.now();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsViewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_dashboard, container, false);

        // UIコンポーネントの初期化
        calendarView = view.findViewById(R.id.calendarView);
        consumptionPredictionTextView = view.findViewById(R.id.consumptionPredictionTextView);
        monthTextView = view.findViewById(R.id.monthTextView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // カレンダー初期化処理
            calendarManager = ManageCalendar.getInstance();
            if (calendarView != null) {
                calendarManager.setupCalendar(calendarView, view);
                calendarManager.initializeCalendar(); // カレンダーのバインド処理を明示的に初期化

                // 日付選択リスナーを設定
                calendarManager.setOnDateSelectedListener(this::updateConsumptionPrediction);

                // 初期データの表示
                updateConsumptionPrediction(selectedDate);
            } else {
                Log.e(TAG, "CalendarView is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Calendar initialization error", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "カレンダーの初期化に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }

        // 消費傾向データの監視
        analyticsViewModel.consumptionTrendData.observe(getViewLifecycleOwner(), trendData -> {
            // 消費傾向データが更新されたときの処理
            updateDashboardDisplay();
        });

        // カテゴリ別消費データの監視
        analyticsViewModel.categoryConsumptionData.observe(getViewLifecycleOwner(), categoryData -> {
            // カテゴリ別消費データが更新されたときの処理
            updateDashboardDisplay();
        });

        // 廃棄率データの監視
        analyticsViewModel.wasteRateData.observe(getViewLifecycleOwner(), wasteRate -> {
            // 廃棄率データが更新されたときの処理
            updateDashboardDisplay();
        });
    }

    // 消費予測を更新するメソッド
    private void updateConsumptionPrediction(LocalDate date) {
        if (date == null) {
            date = LocalDate.now(); // nullの場合は現在の日付を使用
        }

        this.selectedDate = date;

        // 選択された日付に基づいて消費予測を更新
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        consumptionPredictionTextView.setText(dateStr + "の消費予測データを表示中...");

        // 日付に基づいた消費予測を取得して表示
        analyticsViewModel.getPredictedConsumptionForDate(date).observe(getViewLifecycleOwner(), prediction -> {
            if (prediction != null) {
                String formattedPrediction = String.format("%.1f", prediction);
                consumptionPredictionTextView.setText(dateStr + "の消費予測: " + formattedPrediction + "単位");
            } else {
                consumptionPredictionTextView.setText(dateStr + "の消費予測データはありません");
            }
        });

        // 選択期間（例：過去30日間）のデータを読み込む
        LocalDate startDate = date.minusDays(30);
        LocalDate endDate = date;

        // 消費傾向データをロード
        analyticsViewModel.loadConsumptionTrend(
                startDate,
                endDate,
                com.inv.inventryapp.repositories.AnalyticsRepository.TrendPeriod.DAILY);

        // カテゴリ別消費データをロード
        analyticsViewModel.loadCategoryConsumptionTrend(startDate, endDate);

        // 廃棄率データをロード
        analyticsViewModel.loadWasteRate(startDate, endDate);
    }

    // ダッシュボード表示を更新するメソッド
    private void updateDashboardDisplay() {
        // 既に取得したデータを使って、その他のダッシュボード要素を更新
        // 実際の実装では、グラフやチャートの更新などが行われます
    }
}
