package com.inv.inventryapp.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
    // REQUIRED_PERMISSIONS は checkPermissions メソッド内で動的に決定されます。
    // private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA}; // こちらは直接使用しない

    private ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        previewView = findViewById(R.id.viewFinder);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (checkPermissions()) {
            startCamera();
        }
        // 権限がない場合は checkPermissions() の中でリクエストが呼ばれます。
        // setupCamera() は startCamera() のコールバック内で呼び出されるように変更します。
    }

    protected abstract int getLayoutResource();

    protected abstract void processImage(ImageAnalysis imageAnalysis);

    protected abstract void setupCamera();

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 既存のバインドをクリアします。
                // これにより、再設定時にクリーンな状態から開始できます。
                cameraProvider.unbindAll();

                processImage(imageAnalysis);

                cameraProvider.bindToLifecycle(this, cameraSelector,
                        preview, imageCapture, imageAnalysis);

                // カメラリソースの準備ができた後に setupCamera を呼び出します。
                setupCamera();

            } catch (ExecutionException | InterruptedException e) {
                Log.e("BaseCameraActivity", "カメラの初期化に失敗しました", e);
                Toast.makeText(this, "カメラの初期化に失敗しました: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish(); // 初期化失敗時はアクティビティを終了
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // 再度権限を確認し、すべて許可されていればカメラを開始
            if (checkPermissions()) {
                startCamera();
            } else {
                // 権限が付与されなかった場合
                boolean allGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) { // grantResults がすべて許可でも checkPermissions が false の場合 (ロジックの不整合など)
                    startCamera();
                } else {
                    Toast.makeText(this, "カメラの権限が許可されませんでした。", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private boolean checkPermissions() {
        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean readMediaImagesPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;

            if (!cameraPermissionGranted || !readMediaImagesPermissionGranted) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_PERMISSIONS);
                return false;
            }
        } else {
            if (!cameraPermissionGranted) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // カメラのバインドを解除
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown(); // ExecutorService をシャットダウン
        }
        Log.d("BaseCameraActivity", "onDestroy: カメラリソースを解放し、Executorをシャットダウン");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setupBackButton(int buttonId) {
        View backButton = findViewById(buttonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }
    // setupCamera は startCamera のコールバックに移動しました。
}