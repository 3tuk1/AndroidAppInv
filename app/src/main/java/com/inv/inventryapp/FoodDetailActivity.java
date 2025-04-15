package com.inv.inventryapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FoodDetailActivity extends AppCompatActivity {

    private FoodItem foodItem;
    private FoodRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // アクションバーに戻るボタンを表示
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = FoodRepository.getInstance(this);

        // 商品IDを取得
        String foodId = getIntent().getStringExtra("foodId");
        if (foodId == null) {
            Toast.makeText(this, "商品情報が見つかりません", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // IDから商品を検索して表示
        repository.getFoodById(foodId, item -> {
            if (item != null) {
                foodItem = item;
                displayFoodDetails();
            } else {
                Toast.makeText(this, "商品情報が見つかりません", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 消費ボタンの設定
        Button consumeButton = findViewById(R.id.button_consume);
        consumeButton.setOnClickListener(v -> consumeItem());
    }

    private void displayFoodDetails() {
        TextView nameTextView = findViewById(R.id.text_food_name);
        TextView categoryTextView = findViewById(R.id.text_food_category);
        TextView expiryTextView = findViewById(R.id.text_expiry_date);
        TextView quantityTextView = findViewById(R.id.text_quantity);
        TextView barcodeTextView = findViewById(R.id.text_barcode);

        nameTextView.setText(foodItem.getName());
        categoryTextView.setText("カテゴリ: " + foodItem.getCategory());
        expiryTextView.setText("賞味期限: " + foodItem.getExpiryDate());
        quantityTextView.setText("数量: " + foodItem.getQuantity());
        barcodeTextView.setText("バーコード: " + foodItem.getBarcode());

        // 賞味期限の近い商品に警告表示
        if (foodItem.isNearExpiry()) {
            expiryTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void consumeItem() {
        // 消費処理（例: 数量を1減らす）
        if (foodItem.consume(1)) {
            repository.updateFood(foodItem);
            displayFoodDetails();
            Toast.makeText(this, "消費しました", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "在庫がありません", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}