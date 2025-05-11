package com.inv.inventryapp.camera;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.inv.inventryapp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReceiptScannerActivity extends BaseCameraActivity {
    private TextView resultTextView;
    private TextRecognizer recognizer;
    private Button captureButton;

    @Override
    protected void setupCamera() {
        // ボタンの設定やUI要素の初期化など
// カメラ関連の処理は BaseCameraActivity の startCamera() と processImage() に任せる
        resultTextView = findViewById(R.id.receipt_text);
        android.widget.ImageButton captureButton = findViewById(R.id.capture_button);

        recognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());

        captureButton.setOnClickListener(v -> captureReceipt());
// 戻るボタンの設定
        setupBackButton(R.id.back_button);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_receipt_scanner;
    }

    @Override
    protected void processImage(ImageAnalysis imageAnalysis) {
        // リアルタイム処理は不要なのでここでは何もしない
    }

    private void captureReceipt() {
        String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.JAPAN)
                .format(System.currentTimeMillis());
        File outputDir = new File(getExternalCacheDir(), "receipts");
        if (!outputDir.exists()) outputDir.mkdirs();

        File outputFile = new File(outputDir, fileName + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // 画像保存後、OCR処理を行う
                        processReceiptImage(outputFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // エラー処理
                    }
                });
    }

    private void processReceiptImage(File imageFile) {
        try {
            InputImage image = InputImage.fromFilePath(this, android.net.Uri.fromFile(imageFile));
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        String resultText = text.getText();
                        resultTextView.setText(resultText);
                        // ここでレシートから取得した情報を処理する
                        parseReceiptData(text);
                    })
                    .addOnFailureListener(e -> {
                        // エラー処理
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseReceiptData(Text text) {
        // レシートデータの解析ロジックをここに実装
        // 例: 商品名、価格、日付などを抽出
    }
}