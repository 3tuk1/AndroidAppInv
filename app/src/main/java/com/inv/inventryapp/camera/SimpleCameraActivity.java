package com.inv.inventryapp.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
// Buttonをインポートしている行を削除または変更
import android.widget.ImageButton;  // 追加

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
        // ImageButtonとして正しく取得
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

        // アプリ専用ディレクトリに画像を保存する方法に変更
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

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("photo_uri", photoUri.toString());
                        setResult(RESULT_OK, resultIntent);
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