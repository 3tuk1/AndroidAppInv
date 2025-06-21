package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.ModelDatabase;
import com.inv.inventryapp.model.dao.ShoppingListDao;
import com.inv.inventryapp.model.entity.ShoppingList;
import java.util.List;

public class ShoppingListRepository {
    private final ShoppingListDao shoppingListDao;

    public ShoppingListRepository(Context context) {
        ModelDatabase db = ModelDatabase.Companion.getInstance(context);
        this.shoppingListDao = db.shoppingListDao();
    }

    public void addShoppingList(String productName, int quantity) {
        ShoppingList shoppingList = new ShoppingList(getprioritymax() + 1, productName, quantity);
        shoppingListDao.insert(shoppingList);
    }

    public void removeFromShoppingList(int priority) {
        List<ShoppingList> list = shoppingListDao.getAll();
        for (ShoppingList s : list) {
            if (s.getPriority() == priority) {
                shoppingListDao.delete(s);
            }
        }
    }

    public List<ShoppingList> getShoppingList() {
        return shoppingListDao.getAll();
    }

    public void setShoppingList(ShoppingList shoppingList) {
        shoppingListDao.update(shoppingList);
    }

    public int getprioritymax() {
        List<ShoppingList> list = shoppingListDao.getAll();
        int maxPriority = 0;
        for (ShoppingList s : list) {
            if (s.getPriority() > maxPriority) {
                maxPriority = s.getPriority();
            }
        }
        return maxPriority;
    }

    // exportShoppingListは用途に応じて実装してください
}
