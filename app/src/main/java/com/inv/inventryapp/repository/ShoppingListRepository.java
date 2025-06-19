package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.ModelDatabase;
import com.inv.inventryapp.model.dao.ShoppingListDao;
import com.inv.inventryapp.model.entity.ShoppingList;
import java.util.List;

public class ShoppingListRepository {
    private final ShoppingListDao shoppingListDao;

    public ShoppingListRepository(Context context) {
        ModelDatabase db = ModelDatabase.getInstance(context);
        this.shoppingListDao = db.shoppingListDao();
    }

    public void addShoppingList(String productName, int quantity) {
        ShoppingList shoppingList = new ShoppingList(productName, quantity);
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

    // exportShoppingListは用途に応じて実装してください
}
