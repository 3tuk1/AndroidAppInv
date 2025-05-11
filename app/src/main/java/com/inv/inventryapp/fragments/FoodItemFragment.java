package com.inv.inventryapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.inv.inventryapp.models.FoodItem;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.FoodItemDao;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.inv.inventryapp.room.Converters.fromBitmap;
import static com.inv.inventryapp.room.Converters.toBitmap;

public class FoodItemFragment extends Fragment {
    private ActivityResultLauncher<Intent> galleryLauncher;
    private EditText nameEditText;
    private EditText quantityEditText;
    private Spinner categorySpinner;
    private EditText expiryEditText;

    private ActivityResultLauncher<Intent> cameraLauncher;

    private FoodItemDao foodItemDao;
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

        // Spinnerの設定
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.categories,  // categories.xmlで定義したカテゴリー配列
                android.R.layout.simple_spinner_item  // デフォルトのレイアウト
        );
        // ドロップダウンのレイアウトを設定
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アダプターをSpinnerに設定
        categorySpinner.setAdapter(adapter);

        // カテゴリー選択リスナー（必要に応じて）
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                // 選択されたカテゴリーを使った処理を行う場合はここに記述
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 何も選択されなかった場合の処理（通常は不要）
            }
        });
        // DBアクセス準備
        foodItemDao = AppDatabase.getInstance(requireContext()).foodItemDao();

        // 引数からIDを取得し、食材情報を取得
        if (getArguments() != null) {
            long itemId = getArguments().getLong("foodItemId", -1);
            if (itemId != -1) {
                executor.execute(() -> {
                    FoodItem item = foodItemDao.getFoodItemById((int) itemId);
                    if (item != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            nameEditText.setText(item.getName());
                            quantityEditText.setText(String.valueOf(item.getQuantity()));
                            // カテゴリや日付など必要に応じて設定
                            expiryEditText.setText(item.getExpiryDate());
                            // 写真の設定
                            // 画像を表示する場合は、ImageViewを使って表示
                            ImageView foodImageView = view.findViewById(R.id.foodImageView);
                            if (item.getImage() != null) {
                                foodImageView.setImageBitmap(item.getImage());
                            } else {
                                foodImageView.setImageResource(R.drawable.default_food_image); // デフォルト画像
                            }
                            // カテゴリの設定
                            // ここでは、Spinnerの選択肢を設定する必要があります
                            // カテゴリーの設定（以前のコードを置き換え）
                            String[] categories = getResources().getStringArray(R.array.categories);
                            String itemCategory = item.getCategory();
                            int categoryPosition = 0; // デフォルトは最初の項目

                            // 食品の持つカテゴリーと一致する位置を探す
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
        // 保存ボタンの設定
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            int quantity = Integer.parseInt(quantityEditText.getText().toString());
            String category = categorySpinner.getSelectedItem().toString();
            String expiryDate = expiryEditText.getText().toString();
            // 画像を表示する場合は、ImageViewを使って表示
            ImageView foodImageView = view.findViewById(R.id.foodImageView);
            byte[] foodImage = fromBitmap(foodImageView.getDrawingCache());

            // FoodItemオブジェクトを作成
            FoodItem foodItem = new FoodItem(name, null, expiryDate, quantity, category);
            // 写真の取得
            if (foodImageView.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) foodImageView.getDrawable()).getBitmap();
                foodItem.setImage(bitmap);
            } else {
                foodItem.setImage(null); // デフォルト画像を使用する場合
            }

            // DBに保存
            executor.execute(() -> {
                if (getArguments() != null) {
                    long itemId = getArguments().getLong("foodItemId", -1);
                    if (itemId != -1) {
                        foodItem.setId((int) itemId);
                        foodItemDao.update(foodItem);
                    } else {
                        foodItemDao.insert(foodItem);
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "保存しました", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            });
        });

        // ギャラリーからの写真選択用ランチャー
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        ImageView foodImageView = view.findViewById(R.id.foodImageView);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(),
                                    selectedImageUri
                            );
                            foodImageView.setImageBitmap(bitmap);
                            foodImageView.setDrawingCacheEnabled(true);
                            foodImageView.buildDrawingCache();
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
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // BaseCameraActivityから返されたURIを取得
                        String photoUriString = result.getData().getStringExtra("photo_uri");
                        if (photoUriString != null) {
                            Uri photoUri = Uri.parse(photoUriString);
                            ImageView foodImageView = view.findViewById(R.id.foodImageView);
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        requireActivity().getContentResolver(),
                                        photoUri
                                );
                                foodImageView.setImageBitmap(bitmap);
                                foodImageView.setDrawingCacheEnabled(true);
                                foodImageView.buildDrawingCache();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "写真の取得に失敗しました", Toast.LENGTH_SHORT).show();
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
                        // カメラ起動（BaseCameraActivityを使用）
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
}