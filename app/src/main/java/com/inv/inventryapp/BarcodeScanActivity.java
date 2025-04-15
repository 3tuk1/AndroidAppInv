package com.inv.inventryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BarcodeScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);

        // バーコードスキャナーを起動
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("バーコードをスキャンしてください");
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "スキャンがキャンセルされました", Toast.LENGTH_LONG).show();
                finish();
            } else {
                String barcode = result.getContents();

                // バーコードで商品を検索または新規追加画面へ遷移
                FoodRepository repository = FoodRepository.getInstance(this);
                repository.getFoodByBarcode(barcode, items -> {
                    if (items.isEmpty()) {
                        // 新規登録画面へ（バーコード情報を渡す）
                        Intent intent = new Intent(this, AddFoodActivity.class);
                        intent.putExtra("barcode", barcode);
                        startActivity(intent);
                    } else {
                        // 既存商品の表示（例：詳細画面へ）
                        Intent intent = new Intent(this, FoodDetailActivity.class);
                        intent.putExtra("foodId", items.get(0).getId());
                        startActivity(intent);
                    }
                    finish();
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}