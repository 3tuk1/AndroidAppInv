package com.inv.inventryapp.fragments;// FoodItemFragment.java

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import com.inv.inventryapp.utility.ConvertDate;
import com.inv.inventryapp.utility.SelectCalendar;
import com.kizitonwose.calendar.view.CalendarView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    private int selectedCategoryId = -1; // 選択中のカテゴリID
    private List<Category> categoryList = new ArrayList<>();

    private ActivityResultLauncher<Intent> cameraLauncher;

    private MainItemDao mainItemDao;
    private ItemImageDao itemImageDao;
    private LocationDao locationDao;
    private BarcodeDao BarcodeDao; // バーコードDAOの変数
    private HistoryDao historyDao; // 履歴DAOの変数
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

        // Spinnerの設定（Categoryテーブルから取得）
        AppDatabase db = AppDatabase.getInstance(requireContext());
        mainItemDao = db.mainItemDao();
        itemImageDao = db.itemImageDao();
        locationDao = db.locationDao();
        BarcodeDao = db.barcodeDao();
        historyDao = db.historyDao();
        CategoryDao categoryDao = db.categoryDao();

        executor.execute(() -> {
            categoryList.clear();
            categoryList = categoryDao.getAllCategories(); // カテゴリーを取得
            // カテゴリーが空の場合はデフォルトを追加
            if (categoryList.isEmpty()) {
                //categoryList.add(new Category( "未分類"));
            }
            List<String> categoryNames = new ArrayList<>();
            for (Category c : categoryList) {
                categoryNames.add(c.name);
            }
            if (getActivity() == null) return; // Activityがnullの場合は処理を中断
            getActivity().runOnUiThread(() -> {
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(spinnerAdapter);
                // 既存データがある場合は選択状態を復元 (この部分は後続のデータ読み込みロジックでカバーされる)
            });
        });


        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < categoryList.size()) {
                    selectedCategoryId = categoryList.get(position).id;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = -1;
            }
        });

        // 引数からIDとバーコードを取得し、アイテム情報を取得
        if (getArguments() != null) {
            int bundleItemId = getArguments().getInt("itemId", -1);
            boolean isNewItemFromBundle = getArguments().getBoolean("isNewItem", false);
            String receivedBarcode = getArguments().getString("barcode", null); // ★修正: バーコードを常に受け取る

            Log.d("FoodItemFragment", "バンドルから取得: itemId=" + bundleItemId + ", isNewItem=" + isNewItemFromBundle + ", barcode=" + receivedBarcode);

            executor.execute(() -> {
                MainItemJoin itemToDisplay = null;
                int finalItemIdToLoad = bundleItemId; // 初期値としてバンドルのitemIdを使用
                boolean barcodeItemMismatch = false; // バーコードとIDの不一致フラグ

                // 1. 受け取ったバーコードでアイテムを検索
                if (receivedBarcode != null && !receivedBarcode.isEmpty()) {
                    itemToDisplay = BarcodeDao.getItemByBarcodeValue(receivedBarcode);
                    if (itemToDisplay != null && itemToDisplay.mainItem != null) {
                        // バーコードから見つかったアイテムIDがバンドルのIDと異なる場合は警告フラグを立てる
                        if (bundleItemId != -1 && bundleItemId != itemToDisplay.mainItem.getId()) {
                            barcodeItemMismatch = true;
                            Log.w("FoodItemFragment", "警告: バーコード " + receivedBarcode +
                                  " から取得したアイテムID (" + itemToDisplay.mainItem.getId() +
                                  ") がバンドルのアイテムID (" + bundleItemId + ") と一致しません。");
                        }

                        finalItemIdToLoad = itemToDisplay.mainItem.getId(); // バーコードから得たIDを優先
                        this.barcode = receivedBarcode; // Fragmentのメンバー変数にセット
                        Log.d("FoodItemFragment", "バーコード '" + receivedBarcode + "' からアイテム取得: ID=" + finalItemIdToLoad + ", 名前=" + itemToDisplay.mainItem.getName());
                    } else {
                        Log.d("FoodItemFragment", "バーコード '" + receivedBarcode + "' に紐づくアイテムなし。");
                        // 新規アイテムの場合、このバーコードを保持
                        if (isNewItemFromBundle) {
                            this.barcode = receivedBarcode;
                        }
                    }
                }

                // 2. バーコード検索で見つからず、かつバンドルに有効な itemId があり、新規アイテム指定でない場合にそれで検索
                if (itemToDisplay == null && finalItemIdToLoad != -1 && !isNewItemFromBundle) {
                    itemToDisplay = mainItemDao.getMainItemWithImagesAndLocationById(finalItemIdToLoad);
                    if (itemToDisplay != null && itemToDisplay.mainItem != null) {
                        Log.d("FoodItemFragment", "バンドルID " + finalItemIdToLoad + " からアイテム取得: 名前=" + itemToDisplay.mainItem.getName());
                        // このアイテムに紐づくバーコードがあれば、それも設定
                        if (this.barcode == null) { // バーコードがまだ設定されていなければ
                            Barcode existingBarcodeForLoadedItem = BarcodeDao.getBarcodesForItem(itemToDisplay.mainItem.getId());
                            if (existingBarcodeForLoadedItem != null) {
                                this.barcode = existingBarcodeForLoadedItem.getBarcodeValue();
                                Log.d("FoodItemFragment", "ロードしたアイテム ID " + itemToDisplay.mainItem.getId() + " からバーコード取得: " + this.barcode);
                            }
                        }
                    } else {
                        Log.d("FoodItemFragment", "バンドルID " + finalItemIdToLoad + " のアイテムは見つかりませんでした。");
                    }
                }

                // UI更新処理
                if (itemToDisplay != null && itemToDisplay.mainItem != null && getActivity() != null) {
                    final MainItem item = itemToDisplay.mainItem;
                    final MainItemJoin finalItemToDisplay = itemToDisplay; // effectively final for lambda
                    final boolean finalBarcodeItemMismatch = barcodeItemMismatch; // ラムダ式用のfinal変数

                    getActivity().runOnUiThread(() -> {
                        // バーコードとアイテムIDの不一致が検出された場合は警告を表示
                        if (finalBarcodeItemMismatch) {
                            Toast.makeText(getContext(),
                                "警告: バーコードと紐づくアイテムが異なります。バーコードの登録を確認してください。",
                                Toast.LENGTH_LONG).show();
                        }

                        nameEditText.setText(item.getName());
                        quantityEditText.setText(String.valueOf(item.getQuantity()));
                        if (item.getExpirationDate() != null) {
                            expiryEditText.setText(ConvertDate.localDateToString(item.getExpirationDate()));
                        }

                        // 場所情報を設定
                        if (finalItemToDisplay.location != null) {
                            locationEditText.setText(finalItemToDisplay.location.getLocation());
                        }

                        // 画像の設定
                        if (finalItemToDisplay.images != null && !finalItemToDisplay.images.isEmpty()) {
                            ItemImage firstImage = finalItemToDisplay.images.get(0);
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
                            } else {
                                foodImageView.setImageResource(R.drawable.default_food_image);
                            }
                        } else {
                            foodImageView.setImageResource(R.drawable.default_food_image);
                        }

                        // カテゴリーの設定
                        int categoryPosition = 0;
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (categoryList.get(i).id == item.getCategoryId()) {
                                categoryPosition = i;
                                break;
                            }
                        }
                        categorySpinner.setSelection(categoryPosition);
                    });
                } else if (isNewItemFromBundle && this.barcode != null) {
                    // 新規アイテムとしてUIを初期化 (バーコードは保持)
                    Log.d("FoodItemFragment", "新規アイテムとしてUIを初期化。バーコード: " + this.barcode);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            nameEditText.setText(""); // 名前をクリア
                            quantityEditText.setText("1"); // 数量をデフォルトに
                            expiryEditText.setText(""); // 賞味期限をクリア
                            locationEditText.setText(""); // 場所をクリア
                            foodImageView.setImageResource(R.drawable.default_food_image); // 画像をデフォルトに
                            categorySpinner.setSelection(0); // カテゴリを先頭に (または「未分類」など適切なデフォルト)
                        });
                    }
                } else {
                    Log.w("FoodItemFragment", "表示するアイテム情報が見つかりませんでした。itemId: " + finalItemIdToLoad + ", barcode: " + this.barcode);
                    // 必要であれば、新規登録画面としてUIを完全に初期化する処理をここに入れる
                    if(getActivity() != null){
                        getActivity().runOnUiThread(()-> {
                            nameEditText.setText("");
                            quantityEditText.setText("1");
                            expiryEditText.setText("");
                            locationEditText.setText("");
                            foodImageView.setImageResource(R.drawable.default_food_image);
                            if (!categoryList.isEmpty()) categorySpinner.setSelection(0);
                        });
                    }
                }
            });
        } else {
            // バンドルがnullの場合 (通常は手動入力フロー)、UIを初期状態にする
            Log.d("FoodItemFragment", "バンドルがnullです。手動入力としてUIを初期化します。");
            if (getActivity() != null) {
                getActivity().runOnUiThread(()-> {
                    nameEditText.setText("");
                    quantityEditText.setText("1");
                    expiryEditText.setText("");
                    locationEditText.setText("");
                    foodImageView.setImageResource(R.drawable.default_food_image);
                    if (!categoryList.isEmpty()) categorySpinner.setSelection(0);
                });
            }
        }


        expiryEditText.setOnClickListener(v -> {
            // カレンダーダイアログ用のレイアウトをインフレート
            View calendarDialogView = getLayoutInflater().inflate(R.layout.calendar_dialog, null);

            // レイアウトからCalendarViewを取得
            CalendarView calendarView = calendarDialogView.findViewById(R.id.calendarView);

            // カレンダーのセットアップ（初期化順序を修正）
            SelectCalendar SC = SelectCalendar.getInstance();
            SC.setupCalendar(calendarView, calendarDialogView);
            SC.initializeCalendar(); // CalendarViewが設定された後にバインドを初期化

            // ダイアログの作成と表示
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(calendarDialogView)
                    .setPositiveButton("OK", (dialog, which) -> {
                        java.time.LocalDate selectedDate = SC.getSelectedDate(); // String から java.time.LocalDate に変更
                        if (selectedDate != null) {
                            expiryEditText.setText(ConvertDate.localDateToString(selectedDate)); // LocalDate を String に変換
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
            String expiryDateStr = expiryEditText.getText().toString();
            String locationStr = locationEditText.getText().toString();

            if (name.isEmpty() || quantityStr.isEmpty() || expiryDateStr.isEmpty()) {
                Toast.makeText(getContext(), "すべての必須項目を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "数量には数値を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantity <= 0) {
                Toast.makeText(getContext(), "数量は1以上を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            // String を LocalDate に変換

            final java.time.LocalDate finalExpiryDate = ConvertDate.stringToLocalDate(expiryDateStr);
            if (finalExpiryDate == null) {
                Toast.makeText(getContext(), "賞味期限の形式が正しくありません", Toast.LENGTH_SHORT).show();
                return;
            }


            executor.execute(() -> {
                try {
                    MainItem mainItem;
                    int currentItemId = -1; // 更新・新規作成対象のID
                    int oldQuantity = 0;
                    boolean isUpdate = false;

                    // 更新か新規作成かを判断
                    // 1. バーコードがあり、それに紐づくアイテムが存在すれば、それを更新対象とする
                    if (this.barcode != null && !this.barcode.isEmpty()) {
                        MainItemJoin itemFromBarcode = BarcodeDao.getItemByBarcodeValue(this.barcode);
                        if (itemFromBarcode != null && itemFromBarcode.mainItem != null) {
                            currentItemId = itemFromBarcode.mainItem.getId();
                            isUpdate = true;
                            Log.d("FoodItemFragment_Save", "バーコードに紐づく既存アイテムを更新対象に: ID=" + currentItemId);
                        }
                    }

                    // 2. バーコードで見つからない or バーコードがない場合で、バンドルから有効なIDが渡されていれば、それを更新対象とする
                    // ただし、isNewItemFromBundleがtrueの場合は新規扱い
                    if (!isUpdate && getArguments() != null) {
                        int bundleItemIdForSave = getArguments().getInt("itemId", -1);
                        boolean isNewItemFromBundleForSave = getArguments().getBoolean("isNewItem", false);
                        if (bundleItemIdForSave != -1 && !isNewItemFromBundleForSave) {
                            // 既存のアイテムか確認
                            MainItem checkItem = mainItemDao.getMainItemById(bundleItemIdForSave);
                            if(checkItem != null) {
                                currentItemId = bundleItemIdForSave;
                                isUpdate = true;
                                Log.d("FoodItemFragment_Save", "バンドルIDの既存アイテムを更新対象に: ID=" + currentItemId);
                            }
                        }
                    }


                    if (isUpdate && currentItemId != -1) {
                        // 更新の場合
                        mainItem = mainItemDao.getMainItemById(currentItemId);
                        if (mainItem == null) { // 念のためnullチェック
                            Log.e("FoodItemFragment_Save", "更新対象のアイテムが見つかりません: ID=" + currentItemId);
                            // この場合、新規作成にフォールバックするかエラーとするか検討
                            // ここではエラーメッセージを表示して中断する例
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "更新対象アイテムが見つかりません", Toast.LENGTH_SHORT).show());
                            }
                            return;
                        }
                        oldQuantity = mainItem.getQuantity();
                        mainItem.setName(name);
                        mainItem.setQuantity(quantity);
                        mainItem.setCategoryId(selectedCategoryId);
                        mainItem.setExpirationDate(finalExpiryDate);
                        mainItemDao.update(mainItem);
                        Log.d("FoodItemFragment_Save", "アイテム更新: ID=" + currentItemId);
                    } else {
                        // 新規作成の場合
                        MainItem newItem = new MainItem(
                                quantity,
                                selectedCategoryId,
                                name,
                                finalExpiryDate
                        );
                        long newItemId = mainItemDao.insert(newItem); // 保存してIDを取得
                        currentItemId = (int) newItemId; // 新しいIDを設定
                        oldQuantity = 0;
                        isUpdate = false; // 新規作成なのでisUpdateはfalse
                        Log.d("FoodItemFragment_Save", "アイテム新規作成: ID=" + currentItemId);
                    }

                    // 数量変更履歴の記録
                    if (isUpdate) { // 更新の場合のみ旧数量と比較
                        if (oldQuantity != quantity) {
                            String type = (quantity > oldQuantity) ? "input" : "output";
                            int diff = Math.abs(quantity - oldQuantity);
                            LocalDate date = LocalDate.now();
                            // outputの場合の理由は "消費" 以外も考慮できる（例: 数量修正など）
                            String reason = (type.equals("output")) ? "消費" : "追加";
                            History history = new History(currentItemId, diff, type, date, reason);
                            historyDao.insert(history);
                        }
                    } else { // 新規作成時はinputとして記録
                        LocalDate date = LocalDate.now();
                        History history = new History(currentItemId, quantity, "input", date, "新規登録");
                        historyDao.insert(history);
                    }


                    //場所情報の保存
                    final int finalCurrentItemId = currentItemId; // Effectively final for lambda
                    if (!locationStr.isEmpty()) {
                        Location existingLocation = locationDao.getLocationByItemId(finalCurrentItemId);

                        if (existingLocation != null) {
                            existingLocation.setLocation(locationStr);
                            locationDao.update(existingLocation);
                        } else {
                            Location newLocation = new Location(
                                    finalCurrentItemId,
                                    locationStr
                            );
                            locationDao.insert(newLocation);
                        }
                    }

                    // 画像の保存
                    if (imagePath != null && !imagePath.isEmpty()) {
                        ItemImage existingImage = itemImageDao.getImageByItemId(finalCurrentItemId); // 単一の画像を取得

                        if (existingImage != null) {
                            if (!existingImage.getImagePath().equals(imagePath)) {
                                existingImage.setImagePath(imagePath);
                                existingImage.setTimestamp(System.currentTimeMillis());
                                itemImageDao.update(existingImage);
                            }
                        } else {
                            ItemImage newImage = new ItemImage(
                                    finalCurrentItemId,
                                    imagePath
                            );
                            itemImageDao.insert(newImage);
                        }
                    }
                    // バーコード情報の保存
                    if(this.barcode != null && !this.barcode.isEmpty()) {
                        // バーコード値を正規化（トリムして余分な空白を除去）
                        String normalizedBarcode = this.barcode.trim();

                        // まず、このバーコード値が他のアイテムで使用されていないか確認
                        if (BarcodeDao.existsBarcodeValue(normalizedBarcode)) {
                            // 既存のバーコードを取得
                            Barcode existingBarcodeByValue = BarcodeDao.getBarcodeByValue(normalizedBarcode);

                            // 別のアイテムに同じバーコードが登録されている場合は警告
                            if (existingBarcodeByValue != null && existingBarcodeByValue.getItemId() != finalCurrentItemId) {
                                Log.w("FoodItemFragment_Save", "警告: バーコード " + normalizedBarcode + " は既に別のアイテム(ID:" +
                                     existingBarcodeByValue.getItemId() + ")に登録されています");

                                // 既存のバーコードを削除
                                BarcodeDao.delete(existingBarcodeByValue);
                                Log.d("FoodItemFragment_Save", "重複バーコードを削除: ItemID=" +
                                     existingBarcodeByValue.getItemId() + ", Value=" + normalizedBarcode);
                            }
                        }

                        // このアイテム用のバーコードがあるか確認
                        Barcode currentItemBarcode = BarcodeDao.getBarcodesForItem(finalCurrentItemId);

                        if (currentItemBarcode != null) {
                            // 既存のバーコードがある場合、値が異なれば更新
                            if (!normalizedBarcode.equals(currentItemBarcode.getBarcodeValue())) {
                                currentItemBarcode.setBarcodeValue(normalizedBarcode);
                                BarcodeDao.update(currentItemBarcode);
                                Log.d("FoodItemFragment_Save", "バーコード更新: ItemID=" + finalCurrentItemId +
                                      ", 旧値=" + currentItemBarcode.getBarcodeValue() +
                                      ", 新値=" + normalizedBarcode);
                            } else {
                                Log.d("FoodItemFragment_Save", "バーコードに変更なし: ItemID=" + finalCurrentItemId +
                                      ", Value=" + normalizedBarcode);
                            }
                        } else {
                            // 新規バーコード登録
                            Barcode newBarcode = new Barcode(
                                    finalCurrentItemId,
                                    normalizedBarcode
                            );
                            BarcodeDao.insert(newBarcode);
                            Log.d("FoodItemFragment_Save", "バーコード新規登録: ItemID=" + finalCurrentItemId +
                                  ", Value=" + normalizedBarcode);
                        }
                    } else {
                        // バーコードが入力されていない場合、もし既存のバーコードがあれば削除する
                        Barcode existingBarcode = BarcodeDao.getBarcodesForItem(finalCurrentItemId);
                        if (existingBarcode != null) {
                            BarcodeDao.delete(existingBarcode);
                            Log.d("FoodItemFragment_Save", "バーコード削除: ItemID=" + finalCurrentItemId);
                        }
                    }


                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "保存しました", Toast.LENGTH_SHORT).show();
                            if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    }
                }  catch (Exception e) {
                    Log.e("FoodItemFragment_Save", "保存処理中にエラー", e);
                    String errorMsg = (e.getMessage() != null) ? e.getMessage() : "不明なエラー";
                    if (getContext() != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
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
                                    // File photoFile = new File(photoUri.getPath()); // URIのパスは直接ファイルパスではない場合がある
                                    // URIから画像をコピーして保存する方が確実
                                    imagePath = saveImageFromUri(photoUri); // ★修正: URIから安全にパスを取得・保存

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
        if (getContext() == null) throw new IOException("Context is null"); // Contextチェック
        // アプリ専用ディレクトリに画像を保存
        File photoDir = new File(requireContext().getExternalFilesDir(null), "Images"); //
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";
        File outputFile = new File(photoDir, fileName);

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            if (inputStream == null) throw new IOException("Unable to open InputStream for URI: " + uri); // InputStreamチェック

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return outputFile.getAbsolutePath();
    }
}

