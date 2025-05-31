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

                                        if (db.barcodeDao().existsBarcodeValue(barcodeValue)) {
                                            MainItemJoin selectedItem = db.barcodeDao().getItemByBarcodeValue(barcodeValue);
                                            if (selectedItem != null && selectedItem.mainItem != null) {
                                                int itemId = selectedItem.mainItem.getId();
                                                android.util.Log.d("BarcodeScanner", "バーコード: " + barcodeValue +
                                                        ", アイテムID: " + itemId +
                                                        ", アイテム名: " + selectedItem.mainItem.getName());
                                                bundle.putInt("itemId", itemId);
                                                bundle.putBoolean("isNewItem", false);
                                            } else {
                                                android.util.Log.e("BarcodeScanner", "MainItem not found for barcode: " + barcodeValue);
                                                // bundle.putString("barcode", barcodeValue); // ★既に上部で追加済み
                                                bundle.putBoolean("isNewItem", true);
                                            }
                                        } else {
                                            android.util.Log.d("BarcodeScanner", "新規バーコード: " + barcodeValue);
                                            // bundle.putString("barcode", barcodeValue); // ★既に上部で追加済み
                                            bundle.putBoolean("isNewItem", true);
                                        }
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

