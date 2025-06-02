package com.inv.inventryapp.utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.inv.inventryapp.models.Barcode;
import com.inv.inventryapp.models.MainItem;
import com.inv.inventryapp.models.MainItemJoin;
import com.inv.inventryapp.room.AppDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * バーコードデータの重複を検出・修正するユーティリティクラス
 */
public class BarcodeCleanupUtility {

    private static final String TAG = "BarcodeCleanupUtility";
    private static Executor executor = Executors.newSingleThreadExecutor();

    /**
     * 重複するバーコードを検出してログ出力する
     * @param context アプリケーションコンテキスト
     * @param listener 完了リスナー
     */
    public static void detectDuplicateBarcodes(Context context, OnCleanupCompletedListener listener) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<Barcode> allBarcodes = db.barcodeDao().getAllBarcodes();

            // バーコード値ごとに紐づくアイテムIDのリストを作成
            Map<String, List<Integer>> barcodeToItemIdsMap = new HashMap<>();

            for (Barcode barcode : allBarcodes) {
                String barcodeValue = barcode.getBarcodeValue();
                if (barcodeValue != null && !barcodeValue.isEmpty()) {
                    if (!barcodeToItemIdsMap.containsKey(barcodeValue)) {
                        barcodeToItemIdsMap.put(barcodeValue, new ArrayList<>());
                    }
                    barcodeToItemIdsMap.get(barcodeValue).add(barcode.getItemId());
                }
            }

            // 重複するバーコードを検出
            int duplicateCount = 0;
            StringBuilder report = new StringBuilder("重複バーコード検出結果:\n");

            for (Map.Entry<String, List<Integer>> entry : barcodeToItemIdsMap.entrySet()) {
                String barcodeValue = entry.getKey();
                List<Integer> itemIds = entry.getValue();

                if (itemIds.size() > 1) {
                    duplicateCount++;
                    report.append("バーコード '").append(barcodeValue).append("' は ");
                    report.append(itemIds.size()).append(" つのアイテムに紐づいています: ");

                    // 各アイテムの情報を取得
                    for (int i = 0; i < itemIds.size(); i++) {
                        int itemId = itemIds.get(i);
                        MainItem item = db.mainItemDao().getMainItemById(itemId);
                        if (item != null) {
                            report.append("ID:").append(itemId)
                                  .append("(").append(item.getName()).append(")");
                        } else {
                            report.append("ID:").append(itemId).append("(存在しないアイテム)");
                        }

                        if (i < itemIds.size() - 1) {
                            report.append(", ");
                        }
                    }
                    report.append("\n");
                }
            }

            if (duplicateCount == 0) {
                report.append("重複するバーコードは見つかりませんでした。\n");
            } else {
                report.append("合計 ").append(duplicateCount).append(" 個の重複バーコードが見つかりました。\n");
            }

            // 結果をログに出力
            Log.i(TAG, report.toString());

            // リスナーに結果を通知（UIスレッドで）
            if (listener != null) {
                final int finalDuplicateCount = duplicateCount;
                final String finalReport = report.toString();

                // UIスレッドでコールバック
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onDetectionCompleted(finalDuplicateCount, finalReport);
                });
            }
        });
    }

    /**
     * 重複するバーコードを修正する。最新のアイテムのバーコードを残し、他を削除する。
     * @param context アプリケーションコンテキスト
     * @param listener 完了リスナー
     */
    public static void cleanupDuplicateBarcodes(Context context, OnCleanupCompletedListener listener) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<Barcode> allBarcodes = db.barcodeDao().getAllBarcodes();

            // バーコード値ごとに紐づくバーコードオブジェクトのリストを作成
            Map<String, List<Barcode>> barcodeValueMap = new HashMap<>();

            for (Barcode barcode : allBarcodes) {
                String barcodeValue = barcode.getBarcodeValue();
                if (barcodeValue != null && !barcodeValue.isEmpty()) {
                    if (!barcodeValueMap.containsKey(barcodeValue)) {
                        barcodeValueMap.put(barcodeValue, new ArrayList<>());
                    }
                    barcodeValueMap.get(barcodeValue).add(barcode);
                }
            }

            // 重複するバーコードを修正
            int fixedCount = 0;
            StringBuilder report = new StringBuilder("重複バーコード修正結果:\n");

            for (Map.Entry<String, List<Barcode>> entry : barcodeValueMap.entrySet()) {
                String barcodeValue = entry.getKey();
                List<Barcode> barcodes = entry.getValue();

                if (barcodes.size() > 1) {
                    fixedCount++;
                    report.append("バーコード '").append(barcodeValue).append("' の重複を修正: ");

                    // 最も大きいIDのアイテムを残し、他は削除
                    Barcode barcodeToKeep = null;
                    for (Barcode barcode : barcodes) {
                        if (barcodeToKeep == null || barcode.getItemId() > barcodeToKeep.getItemId()) {
                            barcodeToKeep = barcode;
                        }
                    }

                    // 削除対象のバーコード
                    List<Barcode> barcodesToRemove = new ArrayList<>();
                    for (Barcode barcode : barcodes) {
                        if (barcode.getItemId() != barcodeToKeep.getItemId()) {
                            barcodesToRemove.add(barcode);
                        }
                    }

                    // 保持するアイテム情報を取得
                    MainItem keepItem = db.mainItemDao().getMainItemById(barcodeToKeep.getItemId());
                    String keepItemName = (keepItem != null) ? keepItem.getName() : "不明なアイテム";

                    report.append("保持: ID:").append(barcodeToKeep.getItemId())
                          .append("(").append(keepItemName).append(")");

                    // 削除するバーコードを処理
                    report.append(", 削除: ");
                    for (int i = 0; i < barcodesToRemove.size(); i++) {
                        Barcode barcodeToRemove = barcodesToRemove.get(i);
                        db.barcodeDao().delete(barcodeToRemove);

                        MainItem removeItem = db.mainItemDao().getMainItemById(barcodeToRemove.getItemId());
                        String removeItemName = (removeItem != null) ? removeItem.getName() : "不明なアイテム";

                        report.append("ID:").append(barcodeToRemove.getItemId())
                              .append("(").append(removeItemName).append(")");

                        if (i < barcodesToRemove.size() - 1) {
                            report.append(", ");
                        }
                    }
                    report.append("\n");
                }
            }

            if (fixedCount == 0) {
                report.append("修正が必要な重複バーコードは見つかりませんでした。\n");
            } else {
                report.append("合計 ").append(fixedCount).append(" 個の重複バーコードを修正しました。\n");
            }

            // 結果をログに出力
            Log.i(TAG, report.toString());

            // リスナーに結果を通知（UIスレッドで）
            if (listener != null) {
                final int finalFixedCount = fixedCount;
                final String finalReport = report.toString();

                // UIスレッドでコールバック
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onCleanupCompleted(finalFixedCount, finalReport);
                });
            }
        });
    }

    /**
     * クリーンアップ完了リスナーインターフェース
     */
    public interface OnCleanupCompletedListener {
        /**
         * 重複検出が完了したときに呼ばれる
         * @param duplicateCount 検出された重複数
         * @param report 詳細レポート
         */
        void onDetectionCompleted(int duplicateCount, String report);

        /**
         * クリーンアップが完了したときに呼ばれる
         * @param fixedCount 修正された重複数
         * @param report 詳細レポート
         */
        void onCleanupCompleted(int fixedCount, String report);
    }
}
