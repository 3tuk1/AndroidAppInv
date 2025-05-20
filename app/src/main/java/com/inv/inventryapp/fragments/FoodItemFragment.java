package com.inv.inventryapp.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.inv.inventryapp.R;
import com.inv.inventryapp.camera.SimpleCameraActivity;
import com.inv.inventryapp.models.*;
import com.inv.inventryapp.room.*;
import com.inv.inventryapp.utility.CalendarSetup;
import com.inv.inventryapp.utility.SelectCalendar;
import com.kizitonwose.calendar.view.CalendarView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.inv.inventryapp.room.Converters.compressImage;


public class FoodItemFragment extends Fragment {
    /**
     * FoodItemFragmentは、食品アイテムの詳細を編集するためのフラグメントです。
     * 食品アイテムの名前、数量、カテゴリー、賞味期限、場所、画像を管理します。
     */
    private ActivityResultLauncher<Intent> galleryLauncher;
    private EditText nameEditText;
    private EditText quantityEditText;
    private Spinner categorySpinner;
    private EditText expiryEditText;
    private EditText locationEditText;
    private ImageView foodImageView;

    private String imagePath; // 画像ファイルのパス
    private String barcode; // バーコードの変数

    private ActivityResultLauncher<Intent> cameraLauncher;

    private MainItemDao mainItemDao;
    private ItemImageDao itemImageDao;
    private LocationDao locationDao;
    private BarcodeDao BarcodeDao; // バーコードDAOの変数
    private Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_food, container, false);



        // 各Viewを取得
        nameEditText = view.findViewById(R.id.nameEditText);
        quantityEditText = view.findViewById(R.id.quantityEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        expiryEditText = view.findViewById(R.id.expiryEditText);
        locationEditText = view.findViewById(R.id.locationEditText); // 場所入力欄
        foodImageView = view.findViewById(R.id.foodImageView);

        // Spinnerの設定
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // DBアクセス準備
        AppDatabase db = AppDatabase.getInstance(requireContext());
        mainItemDao = db.mainItemDao();
        itemImageDao = db.itemImageDao();
        locationDao = db.locationDao();
        BarcodeDao = db.barcodeDao();

        // 引数からIDを取得し、アイテム情報を取得
        if (getArguments() != null) { // バンドルがnullでない場合
            // 引数からアイテムIDを取得 何もない状態を-1と設定する
            int itemId = getArguments().getInt("itemId", -1);
            if (itemId != -1) {
                // アイテム情報を取得
                executor.execute(() -> {
                    MainItemJoin allJoin = mainItemDao.getMainItemWithImagesAndLocationById(itemId);
                    if (allJoin != null && getActivity() != null) {
                        MainItem item = allJoin.mainItem;
                        getActivity().runOnUiThread(() -> {
                            nameEditText.setText(item.getName());
                            quantityEditText.setText(String.valueOf(item.getQuantity()));
                            expiryEditText.setText(item.getExpirationDate());

                            // 場所情報を設定
                            if (allJoin.location != null) {
                                locationEditText.setText(allJoin.location.getLocation());
                            }

                            // 画像の設定
                            if (allJoin.images != null && !allJoin.images.isEmpty()) {
                                ItemImage firstImage = allJoin.images.get(0);
                                imagePath = firstImage.getImagePath();

                                if (imagePath != null && !imagePath.isEmpty()) {
                                    File imgFile = new File(imagePath);
                                    if (imgFile.exists()) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                        bitmap = compressImage(bitmap);
                                        foodImageView.setImageBitmap(bitmap);
                                    } else {
                                        foodImageView.setImageResource(R.drawable.default_food_image);
                                    }
                                }
                            } else {
                                foodImageView.setImageResource(R.drawable.default_food_image);
                            }

                            // カテゴリーの設定
                            String[] categories = getResources().getStringArray(R.array.categories);
                            String itemCategory = item.getCategory();
                            int categoryPosition = 0;

                            for (int i = 0; i < categories.length; i++) {
                                if (categories[i].equals(itemCategory)) {
                                    categoryPosition = i;
                                    break;
                                }
                            }

                            categorySpinner.setSelection(categoryPosition);
                        });
                    }
                });
            }
        }
        expiryEditText.setOnClickListener(v -> {
            // カレンダーダイアログ用のレイアウトをインフレート
            View calendarDialogView = getLayoutInflater().inflate(R.layout.calendar_dialog, null);

            // レイアウトからCalendarViewを取得
            CalendarView calendarView = calendarDialogView.findViewById(R.id.calendarView);

            // カレンダーのセットアップ
            SelectCalendar selectCalendar = SelectCalendar.getInstance();
            selectCalendar.setupCalendar(calendarView,calendarDialogView);

            // ダイアログの作成と表示
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(calendarDialogView)
                    .setPositiveButton("OK", (dialog, which) -> {
                        String selectedDate = selectCalendar.getSelectedDate();
                        if (selectedDate != null) {
                            expiryEditText.setText(selectedDate);
                        }
                    })
                    .setNegativeButton("キャンセル", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // 保存ボタンの設定
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            // バリデーション
            String name = nameEditText.getText().toString();
            String quantityStr = quantityEditText.getText().toString();
            String expiryDate = expiryEditText.getText().toString();
            String locationStr = locationEditText.getText().toString();

            if (name.isEmpty() || quantityStr.isEmpty() || expiryDate.isEmpty()) {
                Toast.makeText(getContext(), "すべての必須項目を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(getContext(), "数量は1以上を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = categorySpinner.getSelectedItem().toString();

            executor.execute(() -> {
                try {
                    MainItem mainItem;
                    int itemId = -1;

                    // 更新または新規作成の処理
                    if (getArguments() != null) {
                        itemId = getArguments().getInt("itemId", -1);
                        if(getArguments().getBoolean("isNewItem", false)) {
                            barcode = getArguments().getString("barcode", null);
                        }
                        //barcode = getArguments().getString("barcode", null);
                    }

                    if (itemId != -1) {
                        // 更新の場合
                        mainItem = mainItemDao.getMainItemById(itemId);
                        mainItem.setName(name);
                        mainItem.setQuantity(quantity);
                        mainItem.setCategory(category);
                        mainItem.setExpirationDate(expiryDate);
                        mainItemDao.update(mainItem);
                    } else {
                        // 新規作成の場合
                        MainItem newItem = new MainItem(
                                quantity,
                                category,
                                name,
                                expiryDate
                        );
                        long newItemId = mainItemDao.insert(newItem); // 保存してIDを取得
                        itemId = (int) newItemId; // 新しいIDを設定
                    }

                    // 場所情報の保存
                    final int finalItemId = itemId;
                    if (!locationStr.isEmpty()) {
                        Location existingLocation = locationDao.getLocationByItemId(finalItemId);

                        if (existingLocation != null) {
                            existingLocation.setLocation(locationStr);
                            locationDao.update(existingLocation);
                        } else {
                            Location newLocation = new Location(
                                    finalItemId,
                                    locationStr
                            );
                            locationDao.insert(newLocation);
                        }
                    }

                    // 画像の保存
                    if (imagePath != null && !imagePath.isEmpty()) {
                        // 既存の画像を確認
                        ItemImage existingImage = null;
                        MainItemJoin itemWithImages = itemImageDao.getItemWithItemImageById(finalItemId);

                        if (itemWithImages != null && itemWithImages.images != null && !itemWithImages.images.isEmpty()) {
                            existingImage = itemWithImages.images.get(0);
                        }

                        if (existingImage != null) {
                            // 既存の画像を更新
                            if (!existingImage.getImagePath().equals(imagePath)) {
                                existingImage.setImagePath(imagePath);
                                existingImage.setTimestamp(System.currentTimeMillis());
                                itemImageDao.update(existingImage);
                            }
                        } else {
                            // 新しい画像を追加
                            ItemImage newImage = new ItemImage(
                                    finalItemId,
                                    imagePath
                            );
                            itemImageDao.insert(newImage);
                        }
                    }
                    if(barcode != null && !barcode.isEmpty()) {

                        // バーコード情報の保存
                        Barcode existingBarcode = BarcodeDao.getBarcodesForItem(finalItemId);
                        if (existingBarcode != null) {
                            existingBarcode.setBarcodeValue(barcode);
                            BarcodeDao.update(existingBarcode);
                        } else {
                            Barcode newBarcode = new Barcode(
                                    finalItemId,
                                    barcode
                            );
                            BarcodeDao.insert(newBarcode);
                        }

                    }

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "保存しました", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                }  catch (Exception e) {
                    e.printStackTrace();
                    String errorMsg = (e.getMessage() != null) ? e.getMessage() : "不明なエラー";
                    if (getContext() != null && requireActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "保存に失敗しました: " + errorMsg, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });

        // ギャラリーからの写真選択用ランチャー
        galleryLauncher = registerForActivityResult(//　Android標準のAPIに接続
                new ActivityResultContracts.StartActivityForResult(),//　結果を受け取るためのAPI
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            // URIから画像をコピーして保存
                            imagePath = saveImageFromUri(selectedImageUri);

                            // 画像を表示
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            bitmap = compressImage(bitmap);
                            foodImageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "画像の読み込みに失敗しました", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // カメラからの写真撮影用ランチャー
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {// 正常に実効されたかと結果がnullでないか
                        // 実際のファイルパスを取得
                        String photoPath = result.getData().getStringExtra("photo_path");
                        if (photoPath != null) {
                            // 直接ファイルパスを使用
                            imagePath = photoPath;

                            // 画像を表示
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            bitmap = compressImage(bitmap);
                            foodImageView.setImageBitmap(bitmap);

                            Log.d("FoodItemFragment", "写真のパスをデータベースに保存: " + imagePath);
                        } else {
                            // photoPathが取得できない場合はURIから取得する（従来の方法）
                            String photoUriString = result.getData().getStringExtra("photo_uri");
                            if (photoUriString != null) {
                                Uri photoUri = Uri.parse(photoUriString);
                                try {
                                    File photoFile = new File(photoUri.getPath());
                                    imagePath = photoFile.getAbsolutePath();

                                    // 画像を表示
                                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                    bitmap = compressImage(bitmap);
                                    foodImageView.setImageBitmap(bitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getContext(), "写真の取得に失敗しました", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
        );

        // 写真選択ボタンの設定
        Button selectPhotoButton = view.findViewById(R.id.selectImageButton);
        selectPhotoButton.setOnClickListener(v -> {
            showImageSourceDialog();
        });

        return view;
    }

    // 画像ソース選択ダイアログ
    private void showImageSourceDialog() {
        String[] options = {"カメラで撮影", "ギャラリーから選択"};

        new AlertDialog.Builder(requireContext())
                .setTitle("画像の取得方法")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // カメラ起動
                        Intent intent = new Intent(getActivity(), SimpleCameraActivity.class);
                        cameraLauncher.launch(intent);
                    } else {
                        // ギャラリー起動
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        galleryLauncher.launch(intent);
                    }
                })
                .show();
    }

    // URIから画像を保存してパスを返す
    private String saveImageFromUri(Uri uri) throws IOException {
        // アプリ専用ディレクトリに画像を保存
        File photoDir = new File(requireContext().getExternalFilesDir(null), "Images");
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";
        File outputFile = new File(photoDir, fileName);

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return outputFile.getAbsolutePath();
    }
}