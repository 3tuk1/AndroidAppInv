package com.inv.inventryapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExpiryCheckWorker extends Worker {

    private static final String TAG = "ExpiryCheckWorker";
    private static final String CHANNEL_ID = "expiry_notification_channel";
    private static final int NOTIFICATION_ID = 1;

    public ExpiryCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        Log.d(TAG, "賞味期限チェック処理を開始");
        FoodRepository repository = FoodRepository.getInstance(getApplicationContext());

        final CountDownLatch latch = new CountDownLatch(1);
        final Result[] result = new Result[1];

        repository.getNearExpiryFood(items -> {
            if (!items.isEmpty()) {
                createNotificationChannel();
                showNotification(items);
                result[0] = Result.success();
            } else {
                result[0] = Result.success();
            }
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            return Result.failure();
        }

        return result[0];
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "賞味期限通知";
            String description = "食品の賞味期限が近づいた時の通知";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(List<FoodItem> expiringItems) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        StringBuilder contentText = new StringBuilder();
        int count = Math.min(expiringItems.size(), 3); // 最大3つまで表示

        for (int i = 0; i < count; i++) {
            contentText.append(expiringItems.get(i).getName()).append("、");
        }

        if (expiringItems.size() > 3) {
            contentText.append("他").append(expiringItems.size() - 3).append("点");
        } else {
            // 最後の「、」を削除
            contentText.delete(contentText.length() - 1, contentText.length());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("賞味期限が近い食品があります")
                .setContentText(contentText.toString())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}