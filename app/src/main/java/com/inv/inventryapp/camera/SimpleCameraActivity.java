package com.inv.inventryapp.camera;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.content.ContextCompat;

import com.inv.inventryapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleCameraActivity extends BaseCameraActivity {
    private static final String TAG = "SimpleCameraActivity";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_simple_camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 撮影ボタンの設定
        Button captureButton = findViewById(R.id.capture_button);
        if (captureButton != null) {
            captureButton.setOnClickListener(v -> capturePhoto());
        } else {
            Log.e(TAG, "撮影ボタンが見つかりません");
            Toast.makeText(this, "UIエラー: ボタンが見つかりません", Toast.LENGTH_SHORT).show();
            finish();
        }
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

        // 画像保存先の設定
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        Uri imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri == null) {
            Toast.makeText(this, "ファイル作成エラー", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(), imageUri, contentValues).build();

        Log.d(TAG, "写真撮影を開始します");
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Log.d(TAG, "写真の保存に成功: " + imageUri);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("photo_uri", imageUri.toString());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "写真の撮影に失敗: ", exception);
                        Toast.makeText(SimpleCameraActivity.this,
                                "写真の撮影に失敗しました", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}