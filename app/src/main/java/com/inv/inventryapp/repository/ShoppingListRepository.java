package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.ModelDatabase;
import com.inv.inventryapp.model.dao.ShoppingListDao;
import com.inv.inventryapp.model.entity.ShoppingList;
import java.util.List;
import androidx.lifecycle.LiveData;

public class ShoppingListRepository {
    private final ShoppingListDao shoppingListDao;

    public ShoppingListRepository(Context context) {
        ModelDatabase db = ModelDatabase.Companion.getInstance(context);
        this.shoppingListDao = db.shoppingListDao();
    }

    public void addShoppingList(String productName, int quantity) {
        // 重複チェックを追加
        ShoppingList existingItem = shoppingListDao.findByProductName(productName);
        if (existingItem == null) {
            ShoppingList shoppingList = new ShoppingList(getprioritymax() + 1, productName, quantity);
            shoppingListDao.insert(shoppingList);
        }
    }

    public void delete(ShoppingList shoppingList) {
        shoppingListDao.delete(shoppingList);
    }

    public LiveData<List<ShoppingList>> getShoppingList() {
        return shoppingListDao.getAll();
    }

    public int getprioritymax() {
        List<ShoppingList> list = shoppingListDao.getAllList();
        int maxPriority = 0;
        for (ShoppingList s : list) {
            if (s.getPriority() > maxPriority) {
                maxPriority = s.getPriority();
            }
        }
        return maxPriority;
    }

    // 追加: ViewModelから商品名で検索するためのメソッド
    public ShoppingList findByProductName(String productName) {
        return shoppingListDao.findByProductName(productName);
    }
}
