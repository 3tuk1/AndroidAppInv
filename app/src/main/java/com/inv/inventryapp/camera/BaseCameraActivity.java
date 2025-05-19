package com.inv.inventryapp.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.inv.inventryapp.R;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseCameraActivity extends AppCompatActivity {
    protected PreviewView previewView;
    protected ImageCapture imageCapture;
    protected ExecutorService cameraExecutor;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        previewView = findViewById(R.id.viewFinder);
        // アクションバーに戻るボタンを表示
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // 権限の確認
        if (checkPermissions()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();// シングルスレッドのExecutorを作成
        setupCamera();
    }

    protected abstract int getLayoutResource();

    protected abstract void processImage(ImageAnalysis imageAnalysis);

    private void startCamera() {
        // カメラの初期化、非同期処理、終了時のリスナーを設定
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // カメラのバインドを解除
                cameraProvider.unbindAll();

                processImage(imageAnalysis);

                // カメラのバインド
                cameraProvider.bindToLifecycle(this, cameraSelector,
                        preview, imageCapture, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "カメラの初期化に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /*private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkPermissions()) {
                startCamera();
            } else {
                Toast.makeText(this, "カメラの権限がありません", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13以降
            // READ_MEDIA_IMAGES権限を確認
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                // READ_MEDIA_IMAGES権限がない場合、リクエスト
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.CAMERA},
                        REQUEST_CODE_PERMISSIONS);
                return false;
            }
        }

        // カメラ権限チェック
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSIONS);
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
    // アクティビティクラス
    // 戻るボタンを押したときの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 戻るボタンが押されたときの処理
        if (item.getItemId() == android.R.id.home) {
            finish(); // アクティビティを終了して前の画面に戻る
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 子クラスで実装する抽象メソッド
    protected void setupBackButton(int buttonId) {
        // Buttonへのキャストをやめて、Viewとして扱う
        View backButton = findViewById(buttonId);
        backButton.setOnClickListener(v -> finish());
    }
    protected abstract void setupCamera();
}