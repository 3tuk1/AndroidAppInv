package com.inv.inventryapp.camera;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.inv.inventryapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpiryDateScannerActivity extends BaseCameraActivity {
    private TextView dateResultView;
    private TextRecognizer recognizer;

    @Override
    protected void setupCamera() {
        // ボタンの設定やUI要素の初期化など
        // カメラ関連の処理は BaseCameraActivity の startCamera() と processImage() に任せる
        dateResultView = findViewById(R.id.date_result);
        recognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
        // 戻るボタンの設定
        setupBackButton(R.id.back_button);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_expiry_date_scanner;
    }

    @Override
    protected void processImage(ImageAnalysis imageAnalysis) {
        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeExpiryDate);
    }

    @SuppressLint({"UnsafeOptInUsageError", "SetTextI18n"})
    private void analyzeExpiryDate(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(
                Objects.requireNonNull(imageProxy.getImage()),
                imageProxy.getImageInfo().getRotationDegrees());

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    String extractedDate = extractDateFromText(text.getText());
                    if (extractedDate != null) {
                        runOnUiThread(() -> dateResultView.setText("賞味/消費期限: " + extractedDate));
                    }
                })
                .addOnFailureListener(e -> {
                    // エラー処理
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private String extractDateFromText(String text) {
        // 日付を抽出するための正規表現パターン
        // 例: "賞味期限: 2023年12月31日" や "消費期限 23.12.31" など様々なフォーマットに対応
        Pattern datePattern = Pattern.compile(
                "(消費期限|賞味期限|期限)\\D*(\\d{2,4}[年/.\\s-]\\s*\\d{1,2}[月/.\\s-]\\s*\\d{1,2}日?|\\d{1,2}[/.\\s-]\\d{1,2}[/.\\s-]\\d{2,4})"
        );

        Matcher matcher = datePattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private Date parseDate(String dateStr) {
        // 抽出した日付文字列をDateオブジェクトに変換
        // 様々なフォーマットに対応する必要がある
        String[] formats = {
                "yyyy年MM月dd日", "yyyy/MM/dd", "yyyy-MM-dd",
                "yy年MM月dd日", "yy/MM/dd", "yy.MM.dd"
        };

        for (String format : formats) {
            try {
                return new SimpleDateFormat(format, Locale.JAPAN).parse(dateStr);
            } catch (ParseException e) {
                // このフォーマットでは解析できなかった場合、次のフォーマットを試す
            }
        }
        return null;
    }
}