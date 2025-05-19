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
                Objects.requireNonNull(imageProxy.getImage()),// nullチェック
                imageProxy.getImageInfo().getRotationDegrees());// 回転角度を取得

        recognizer.process(image)
                .addOnSuccessListener(text -> { // テキストを取得
                    // 取得したテキストが消費期限や賞味期限を含むか確認
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
                "(消費期限|賞味期限|期限)\\D*" +
                        "(" +
                        // 年月日のパターン（例: 2023年12月31日, 2023/12/31）
                        "\\d{2,4}[年/.\\s-]\\s*\\d{1,2}[月/.\\s-]\\s*\\d{1,2}日?|" +
                        // 日月年のパターン（例: 31/12/2023, 31.12.23）
                        "\\d{1,2}[/.\\s-]\\d{1,2}[/.\\s-]\\d{2,4}|" +
                        // 年月のみのパターン（例: 2026.02, 2026年02月, 26/02）
                        "\\d{2,4}[年/.\\s-]\\s*\\d{1,2}月?" +
                        ")"
        );

        Matcher matcher = datePattern.matcher(text);
        if (matcher.find()) {
            // マッチした部分を取得
            if(Objects.equals(matcher.group(1), "消費期限") || Objects.equals(matcher.group(1), "賞味期限") || Objects.equals(matcher.group(1), "期限")) {
                return matcher.group(1)+" "+matcher.group(2);
            }else {
                return matcher.group(2);
            }
        }
        return null;
    }

    private Date parseDate(String dateStr) {
        // 抽出した日付文字列をDateオブジェクトに変換
        // 様々なフォーマットに対応する必要がある
        String[] formats = {
                "yyyy年MM月dd日", "yyyy/MM/dd", "yyyy-MM-dd",
                "yy年MM月dd日", "yy/MM/dd", "yy.MM.dd",
                "yyyy年MM月", "yyyy/MM", "yyyy.MM", "yy/MM", "yy.MM"
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