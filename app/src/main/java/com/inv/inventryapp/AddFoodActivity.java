package com.inv.inventryapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddFoodActivity extends AppCompatActivity {

    private EditText nameEditText, expiryEditText, quantityEditText;
    private Spinner categorySpinner;
    private Button saveButton;

    private String[] categories = {"果物", "野菜", "肉", "飲み物", "お菓子", "その他"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        nameEditText = findViewById(R.id.nameEditText);
        expiryEditText = findViewById(R.id.expiryEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);

        // カテゴリ Spinner 設定
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // 賞味期限にDatePickerDialogを表示
        expiryEditText.setOnClickListener(v -> showDatePicker());

        // 保存ボタンの処理（後でFirebaseやMainActivityに戻す処理追加）
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String category = categorySpinner.getSelectedItem().toString();
            String expiry = expiryEditText.getText().toString();
            int quantity = Integer.parseInt(quantityEditText.getText().toString());

            Intent data = new Intent();
            data.putExtra("name", name);
            data.putExtra("category", category);
            data.putExtra("expiry", expiry);
            data.putExtra("quantity", quantity);
            setResult(RESULT_OK, data);
            finish();
        });

    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    expiryEditText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
