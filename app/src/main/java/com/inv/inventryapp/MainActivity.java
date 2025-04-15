package com.inv.inventryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_FOOD_REQUEST = 1;
    private RecyclerView foodRecyclerView;
    private Spinner categorySpinner;
    private Button addButton, scanButton;
    private FoodAdapter adapter;
    private ArrayList<FoodItem> foodList;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // 通知の許可が得られた
                } else {
                    // 通知が拒否された場合の処理

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 13以上の場合は通知権限をリクエスト
        if (Build.VERSION.SDK_INT >= 33) { // Android 13 (TIRAMISU)
            try {
                String postNotificationPermission = "android.permission.POST_NOTIFICATIONS";
                if (ContextCompat.checkSelfPermission(this, postNotificationPermission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(postNotificationPermission);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // XMLのViewを取得
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        categorySpinner = findViewById(R.id.categorySpinner);
        addButton = findViewById(R.id.addButton);
        scanButton = findViewById(R.id.scanButton);

        // 仮のデータ（後でRoomやFirebaseと連携）
        foodList = new ArrayList<>();
        foodList.add(new FoodItem("りんご", "果物", "2025-05-01", 3, ""));
        foodList.add(new FoodItem("牛乳", "飲み物", "2025-04-18", 1, ""));

        // RecyclerViewのセットアップ
        adapter = new FoodAdapter(foodList);
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodRecyclerView.setAdapter(adapter);

        // 追加ボタン - 重複しているので1つだけにする
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
            startActivityForResult(intent, ADD_FOOD_REQUEST);
        });

        // スキャンボタンのリスナーが未設定
        scanButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BarcodeScanActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_FOOD_REQUEST && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            String category = data.getStringExtra("category");
            String expiry = data.getStringExtra("expiry");
            int quantity = data.getIntExtra("quantity", 1);

            FoodItem newItem = new FoodItem(name, category, expiry, quantity);
            foodList.add(newItem);
            adapter.notifyItemInserted(foodList.size() - 1);
        }
    }
}