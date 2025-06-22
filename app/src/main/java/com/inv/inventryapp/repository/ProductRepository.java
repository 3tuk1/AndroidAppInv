package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.ModelDatabase;
import com.inv.inventryapp.model.dao.ProductDao;
import com.inv.inventryapp.model.entity.Product;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ProductRepository {
    private final ProductDao productDao;

    public ProductRepository(Context context) {
        ModelDatabase db = ModelDatabase.Companion.getInstance(context);
        this.productDao = db.productDao();
    }

    public void addProduct(Product product) {
        productDao.insert(product);
    }

    public void updateProduct(Product product) {
        productDao.update(product);
    }

    public void deleteProduct(Product product) {
        productDao.delete(product);
    }

    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAll();
    }

    public LiveData<List<Product>> getAllProductsWithZeroQuantity() {
        return productDao.getAllWithZeroQuantity();
    }

    public Product findById(int productId) {
        return productDao.findById(productId);
    }

    public Product findByName(String name) {
        return productDao.findByName(name);
    }

    public Product findByBarcode(long barcodeNumber) {
        return productDao.findByBarcode(barcodeNumber);
    }
}
