package com.inv.inventryapp.camera;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.FoodItemFragment;
import com.inv.inventryapp.models.MainItemJoin;
import com.inv.inventryapp.room.AppDatabase;
// アプリのBarcodeクラスはインポートせず、完全修飾名で使用

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.core.content.ContentProviderCompat.requireContext;

public class BarcodeScannerActivity extends BaseCameraActivity {
    private TextView barcodeResultView;
    private BarcodeScanner scanner;
    ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void setupCamera() {
        // ボタンの設定やUI要素の初期化など
        // カメラ関連の処理は BaseCameraActivity の startCamera() と processImage() に任せる
        barcodeResultView = findViewById(R.id.barcode_result);
        scanner = BarcodeScanning.getClient();
        // 戻るボタンの設定
        setupBackButton(R.id.back_button);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_barcode_scanner;
    }

    @Override
    protected void processImage(ImageAnalysis imageAnalysis) {
        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeBarcodeImage);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeBarcodeImage(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes.size() > 0) {
                        for (Barcode barcode : barcodes) {
                            String barcodeValue = barcode.getRawValue();
                            runOnUiThread(() -> {
                                if (barcodeValue != null) {
                                    barcodeResultView.setText("バーコード: " + barcodeValue);
                                    // バーコードの値がデータベースに存在するか確認
                                    databaseExecutor.execute(() -> {
                                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                        final Bundle bundle = new Bundle();
                                        bundle.putString("barcode", barcodeValue); // ★常にバーコードをバンドルに追加
                                        android.util.Log.d("BarcodeScanner", "検索開始: バーコード = " + barcodeValue);

                                        // バーコードでアイテムを検索する前に全てのバーコードをデバッグ用にログ出力
                                        List<com.inv.inventryapp.models.Barcode> allBarcodes = db.barcodeDao().getAllBarcodes();

                                        // 同一バーコードに紐づく全てのアイテムIDを収集
                                        List<Integer> matchingItemIds = new ArrayList<>();
                                        List<String> matchingItemNames = new ArrayList<>();

                                        for (com.inv.inventryapp.models.Barcode b : allBarcodes) {
                                            android.util.Log.d("BarcodeScanner", "登録済みバーコード確認: " +
                                                    "値=" + b.getBarcodeValue() +
                                                    ", アイテムID=" + b.getItemId());

                                            // 一致するバーコードを見つけたらリストに追加
                                            if (barcodeValue.equals(b.getBarcodeValue())) {
                                                MainItemJoin item = db.mainItemDao().getMainItemWithImagesAndLocationById(b.getItemId());
                                                if (item != null && item.mainItem != null) {
                                                    matchingItemIds.add(b.getItemId());
                                                    matchingItemNames.add(item.mainItem.getName());
                                                }
                                            }
                                        }

                                        if (db.barcodeDao().existsBarcodeValue(barcodeValue)) {
                                            // 同じバーコードが複数のアイテムに紐づいている場合
                                            if (matchingItemIds.size() > 1) {
                                                android.util.Log.w("BarcodeScanner", "警告: バーコード " + barcodeValue +
                                                        " は複数のアイテムに紐づいています: " + matchingItemIds);

                                                // UIスレッドで選択ダイアログを表示
                                                runOnUiThread(() -> {
                                                    // 選択肢を作成（アイテム名とID）
                                                    final CharSequence[] options = new CharSequence[matchingItemIds.size()];
                                                    for (int i = 0; i < matchingItemIds.size(); i++) {
                                                        options[i] = matchingItemNames.get(i) + " (ID:" + matchingItemIds.get(i) + ")";
                                                    }

                                                    // ダイアログを表示
                                                    new android.app.AlertDialog.Builder(BarcodeScannerActivity.this)
                                                        .setTitle("複数のアイテムが見つかりました")
                                                        .setItems(options, (dialog, which) -> {
                                                            // 選択されたアイテムのIDを取得
                                                            int selectedItemId = matchingItemIds.get(which);
                                                            String selectedName = matchingItemNames.get(which);

                                                            // 選択されたアイテムの情報をバンドルに設定
                                                            final Bundle selectedBundle = new Bundle();
                                                            selectedBundle.putString("barcode", barcodeValue);
                                                            selectedBundle.putInt("itemId", selectedItemId);
                                                            selectedBundle.putString("itemName", selectedName);
                                                            selectedBundle.putBoolean("isNewItem", false);

                                                            android.util.Log.d("BarcodeScanner", "ユーザーが選択: " +
                                                                    "アイテムID=" + selectedItemId +
                                                                    ", アイテム名=" + selectedName);

                                                            setResult(BarcodeScannerActivity.RESULT_OK, new Intent().putExtras(selectedBundle));
                                                            finish();
                                                        })
                                                        .setNegativeButton("キャンセル", (dialog, which) -> {
                                                            setResult(BarcodeScannerActivity.RESULT_CANCELED);
                                                            finish();
                                                        })
                                                        .show();
                                                });
                                                return; // ダイアログ表示後は処理終了
                                            }

                                            // 単一のアイテムに紐づいている場合（従来の処理）
                                            // バーコードに紐づくアイテムを明示的に取得
                                            com.inv.inventryapp.models.Barcode directBarcode = db.barcodeDao().getBarcodeByValue(barcodeValue);

                                            if (directBarcode != null) {
                                                int itemId = directBarcode.getItemId();
                                                android.util.Log.d("BarcodeScanner", "バーコード直接検索結果: " +
                                                        "バーコード=" + barcodeValue +
                                                        ", アイテムID=" + itemId);

                                                // 念のため取得したアイテムIDでMainItemが存在するか確認
                                                MainItemJoin selectedItem = db.mainItemDao().getMainItemWithImagesAndLocationById(itemId);

                                                if (selectedItem != null && selectedItem.mainItem != null) {
                                                    android.util.Log.d("BarcodeScanner", "最終検出: " +
                                                            "バーコード=" + barcodeValue +
                                                            ", アイテムID=" + itemId +
                                                            ", アイテム名=" + selectedItem.mainItem.getName());

                                                    bundle.putInt("itemId", itemId);
                                                    bundle.putString("itemName", selectedItem.mainItem.getName()); // 名前も追加
                                                    bundle.putBoolean("isNewItem", false);
                                                } else {
                                                    android.util.Log.e("BarcodeScanner", "エラー: バーコードに紐づくアイテムID " +
                                                            itemId + " のMainItemが見つかりません");
                                                    bundle.putBoolean("isNewItem", true);
                                                }
                                            } else {
                                                android.util.Log.e("BarcodeScanner", "エラー: existsBarcodeValueはtrueだが、getBarcodeByValueがnullを返しました");
                                                bundle.putBoolean("isNewItem", true);
                                            }
                                        } else {
                                            android.util.Log.d("BarcodeScanner", "新規バーコード: " + barcodeValue);
                                            bundle.putBoolean("isNewItem", true);
                                        }

                                        android.util.Log.d("BarcodeScanner", "バンドル内容: " + bundle);
                                        setResult(BarcodeScannerActivity.RESULT_OK, new Intent().putExtras(bundle));
                                        finish();
                                    });
                                }
                            });
                            // バーコードが見つかったら画面を閉じるなどの処理
                            // setResult(Activity.RESULT_OK, new Intent().putExtra("barcode", barcodeValue));
                            // finish();
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // エラー処理
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }
}

