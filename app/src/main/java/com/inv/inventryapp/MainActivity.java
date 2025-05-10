package com.inv.inventryapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.FoodItemDao;

// MainActivity.java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // データベースの初期化
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // InvHomeを起動
        Intent intent = new Intent(MainActivity.this, com.inv.inventryapp.GUI.InvHome.class);
        startActivity(intent);

        // MainActivityを終了して戻らないようにする
        finish();
    }

}
