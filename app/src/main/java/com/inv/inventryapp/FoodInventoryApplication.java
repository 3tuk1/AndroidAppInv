package com.inv.inventryapp;

import android.app.Application;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

// この行を削除
// import com.google.firebase.FirebaseApp;

import java.util.concurrent.TimeUnit;

public class FoodInventoryApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // この行を削除
        // FirebaseApp.initializeApp(this);

        // 賞味期限通知ワーカーをスケジュール
        scheduleExpiryCheck();
    }

    private void scheduleExpiryCheck() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest expiryCheckRequest =
                new PeriodicWorkRequest.Builder(ExpiryCheckWorker.class, 24, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "expiry_check",
                ExistingPeriodicWorkPolicy.KEEP,
                expiryCheckRequest);
    }
}