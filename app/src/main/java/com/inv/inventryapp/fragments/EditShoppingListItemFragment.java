package com.inv.inventryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
// import androidx.navigation.fragment.NavHostFragment; // Navigation Component用

import com.inv.inventryapp.R;
import com.inv.inventryapp.models.ShoppingListItem;
import com.inv.inventryapp.viewmodels.AnalyticsViewModel; // ViewModelのパスを確認
import com.inv.inventryapp.viewmodels.MainItemViewModel; // 作成したViewModel

public class EditShoppingListItemFragment extends Fragment {

    private AnalyticsViewModel analyticsViewModel;
    private MainItemViewModel mainItemViewModel; // MainItemViewModel を使用

    private TextView itemNameTextView;
    private TextView reasonTextView;
    private EditText quantityEditText;
    private Button saveButton;
    private Button cancelButton;

    private int editingItemId = -1;
    private ShoppingListItem currentShoppingListItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsViewModel = new ViewModelProvider(requireActivity()).get(AnalyticsViewModel.class);
        mainItemViewModel = new ViewModelProvider(requireActivity()).get(MainItemViewModel.class); // MainItemViewModelを初期化

        if (getArguments() != null) {
            editingItemId = getArguments().getInt("itemId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_shopping_list_item, container, false);

        itemNameTextView = view.findViewById(R.id.editItemNameTextView);
        reasonTextView = view.findViewById(R.id.editReasonTextView);
        quantityEditText = view.findViewById(R.id.editQuantityEditText);
        saveButton = view.findViewById(R.id.saveChangesButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (editingItemId != -1) {
            mainItemViewModel.getItemById(editingItemId).observe(getViewLifecycleOwner(), mainItem -> {
                if (mainItem != null) {
                    itemNameTextView.setText(mainItem.getName());
                    // ShoppingListItemの情報を取得して理由と現在の推奨数量を表示
                    analyticsViewModel.getShoppingListData().observe(getViewLifecycleOwner(), shoppingListItems -> {
                        if (shoppingListItems != null) {
                            for (ShoppingListItem sli : shoppingListItems) {
                                if (sli.getMainItem().getId() == editingItemId) {
                                    currentShoppingListItem = sli;
                                    reasonTextView.setText(sli.getReason());
                                    quantityEditText.setText(String.valueOf(sli.getQuantityToBuy()));
                                    break;
                                }
                            }
                        }
                        // currentShoppingListItem が null の場合のフォールバック処理は維持
                        if (currentShoppingListItem == null) {
                             reasonTextView.setText("N/A");
                             quantityEditText.setText("1");
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "アイテムが見つかりません", Toast.LENGTH_SHORT).show();
                    popBackStack();
                }
            });
        } else {
            Toast.makeText(getContext(), "編集するアイテムが指定されていません", Toast.LENGTH_SHORT).show();
            popBackStack();
        }

        saveButton.setOnClickListener(v -> saveChanges());
        cancelButton.setOnClickListener(v -> popBackStack());
    }

    private void saveChanges() {
        String quantityStr = quantityEditText.getText().toString();
        if (quantityStr.isEmpty()) {
            quantityEditText.setError("数量を入力してください");
            return;
        }
        int newQuantity;
        try {
            newQuantity = Integer.parseInt(quantityStr);
            if (newQuantity <= 0) {
                quantityEditText.setError("1以上の数量を入力してください");
                return;
            }
        } catch (NumberFormatException e) {
            quantityEditText.setError("有効な数値を入力してください");
            return;
        }

        if (currentShoppingListItem != null && editingItemId != -1) {
            analyticsViewModel.updateShoppingListItemQuantity(editingItemId, newQuantity);
            Toast.makeText(getContext(), "購入数量を更新しました", Toast.LENGTH_SHORT).show();
            popBackStack();
        } else {
            Toast.makeText(getContext(), "更新対象のアイテム情報がありません", Toast.LENGTH_SHORT).show();
        }
    }

    private void popBackStack() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}

