package com.inv.inventryapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.inv.inventryapp.R;
import com.inv.inventryapp.utility.BarcodeCleanupUtility;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // 必須の空のコンストラクタ
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_settingsレイアウトをインフレート
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        setupSettingsView(view);
        return view;
    }

    /**
     * 設定画面のUIとイベントリスナーを設定
     */
    private void setupSettingsView(View view) {
        // 重複バーコード検出ボタン
        Button btnDetectDuplicates = view.findViewById(R.id.btn_detect_duplicate_barcodes);
        btnDetectDuplicates.setOnClickListener(v -> {
            // 重複バーコードを検出
            Toast.makeText(requireContext(), "重複バーコードを検出しています...", Toast.LENGTH_SHORT).show();

            BarcodeCleanupUtility.detectDuplicateBarcodes(requireContext(), new BarcodeCleanupUtility.OnCleanupCompletedListener() {
                @Override
                public void onDetectionCompleted(int duplicateCount, String report) {
                    // 検出結果をダイアログで表示
                    showReportDialog("重複バーコード検出結果",
                            String.format("%d 個の重複バーコードが検出されました。", duplicateCount),
                            report);
                }

                @Override
                public void onCleanupCompleted(int fixedCount, String report) {
                    // このメソッドは使用しない
                }
            });
        });

        // 重複バーコード修正ボタン
        Button btnCleanupDuplicates = view.findViewById(R.id.btn_cleanup_duplicate_barcodes);
        btnCleanupDuplicates.setOnClickListener(v -> {
            // 確認ダイアログを表示
            new AlertDialog.Builder(requireContext())
                    .setTitle("重複バーコード修正")
                    .setMessage("重複バーコードを修正します。同じバーコードが複数のアイテムに紐づいている場合、最新のアイテムのバーコードを残し、他は削除されます。\n\n続行しますか？")
                    .setPositiveButton("修正する", (dialog, which) -> {
                        // ユーザーが確認したので修正を実行
                        Toast.makeText(requireContext(), "重複バーコードを修正しています...", Toast.LENGTH_SHORT).show();

                        BarcodeCleanupUtility.cleanupDuplicateBarcodes(requireContext(), new BarcodeCleanupUtility.OnCleanupCompletedListener() {
                            @Override
                            public void onDetectionCompleted(int duplicateCount, String report) {
                                // このメソッドは使用しない
                            }

                            @Override
                            public void onCleanupCompleted(int fixedCount, String report) {
                                // 修正結果をダイアログで表示
                                showReportDialog("重複バーコード修正結果",
                                        String.format("%d 個の重複バーコードを修正しました。", fixedCount),
                                        report);
                            }
                        });
                    })
                    .setNegativeButton("キャンセル", null)
                    .show();
        });
    }

    /**
     * レポート内容を表示するダイアログ
     */
    private void showReportDialog(String title, String summary, String detailedReport) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_report, null);

        TextView tvSummary = dialogView.findViewById(R.id.tv_report_summary);
        TextView tvDetails = dialogView.findViewById(R.id.tv_report_details);

        tvSummary.setText(summary);
        tvDetails.setText(detailedReport);
        tvDetails.setMovementMethod(new ScrollingMovementMethod()); // スクロール可能に

        builder.setTitle(title)
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }
}
