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

                                        if (db.barcodeDao().existsBarcodeValue(barcodeValue)) {
                                            MainItemJoin selectedItem = db.barcodeDao().getItemByBarcodeValue(barcodeValue);
                                            bundle.putInt("itemId", selectedItem.mainItem.getId());
                                        } else {
                                            bundle.putString("barcode", barcodeValue);
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