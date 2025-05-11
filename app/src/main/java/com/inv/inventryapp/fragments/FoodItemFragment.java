package com.inv.inventryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.inv.inventryapp.R;
import com.inv.inventryapp.models.FoodItem;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.FoodItemDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FoodItemFragment extends Fragment {
    private EditText nameEditText;
    private EditText quantityEditText;
    private Spinner categorySpinner;
    private EditText expiryEditText;

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
        

        return view;
    }
}