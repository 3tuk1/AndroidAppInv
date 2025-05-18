package com.inv.inventryapp.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.inv.inventryapp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleCameraActivity extends BaseCameraActivity {

    private ImageButton captureButton;
    private static final String TAG = "SimpleCameraActivity";
    private static final String KEY_PHOTO_PATH = "photo_path"; // 実際のファイルパスのキー

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_simple_camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 基本的な初期化は親クラスで行われる
    }

    @Override
    protected void setupCamera() {
        captureButton = findViewById(R.id.capture_button);
        if (captureButton != null) {
            captureButton.setOnClickListener(v -> capturePhoto());
        } else {
            Log.e(TAG, "撮影ボタンが見つかりません");
            Toast.makeText(this, "UIエラー: ボタンが見つかりません", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupBackButton(R.id.back_button);
    }

    @Override
    protected void processImage(ImageAnalysis imageAnalysis) {
        // 単純な撮影なので画像分析は不要
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "カメラが初期化されていません", Toast.LENGTH_SHORT).show();
            return;
        }

        // アプリ専用ディレクトリに画像を保存
        File photoDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Camera");
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";
        File photoFile = new File(photoDir, fileName);

        Log.d(TAG, "撮影ファイルパス: " + photoFile.getAbsolutePath());

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Log.d(TAG, "写真保存成功: " + photoFile.getAbsolutePath());

                        // FileProviderを使用してUriを生成
                        Uri photoUri = FileProvider.getUriForFile(
                                SimpleCameraActivity.this,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                photoFile);

                        // 結果インテントに実際のファイルパスとURIの両方を含める
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("photo_uri", photoUri.toString());
                        resultIntent.putExtra(KEY_PHOTO_PATH, photoFile.getAbsolutePath());
                        setResult(RESULT_OK, resultIntent);

                        // 成功メッセージを表示
                        Toast.makeText(SimpleCameraActivity.this,
                                "写真を保存しました", Toast.LENGTH_SHORT).show();

                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "写真撮影失敗: " + exception.getMessage(), exception);
                        Toast.makeText(SimpleCameraActivity.this,
                                "写真の撮影に失敗しました", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}