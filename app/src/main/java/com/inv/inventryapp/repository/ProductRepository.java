package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.ModelDatabase;
import com.inv.inventryapp.model.dao.BarcodeDao;
import com.inv.inventryapp.model.dao.ProductCategoryDao;
import com.inv.inventryapp.model.dao.ProductDao;
import com.inv.inventryapp.model.entity.Barcode;
import com.inv.inventryapp.model.entity.Product;
import com.inv.inventryapp.model.entity.ProductCategory;

import java.time.LocalDate;

public class ProductRepository {
    private final ProductDao productDao;
    private final BarcodeDao barcodeDao;
    private final ProductCategoryDao categoryDao;

    public ProductRepository(Context context) {
        ModelDatabase db = ModelDatabase.getInstance(context);
        this.productDao = db.productDao();
        this.barcodeDao = db.barcodeDao(); // barcodeDaoの初期化
        this.categoryDao = db.productCategoryDao(); // categoryDaoの初期化

    }

    public boolean addProduct(String productName, int price, int quantity, String location, LocalDate expirationDate, LocalDate purchaseDate, String imagePath, String category, int barcodeNumber) {
        Product product = new Product();
        // Nullチェックと空文字チェックを分けて安全に
        if (productName == null || productName.isEmpty() || quantity == 0) {
            return false; // 商品名が空または数量が0の場合は追加しない
        } else {
            if (barcodeNumber != 0) {
                if (!barcodeDao.exists(barcodeNumber)) { // barcodeDaoでバーコードが存在しない場合
                    Barcode barcode = new Barcode(barcodeNumber);
                    barcodeDao.insert(barcode); // まずBarcodeをDBに登録
                    product.setBarcodeId(barcodeDao.getByCode(barcodeNumber).getBarcodeId()); // 登録後のIDを取得
                } else {
                    product.setBarcodeId(barcodeDao.getByCode(barcodeNumber).getBarcodeId());
                }
            }
            if (category != null && !category.isEmpty()) {
                if(!categoryDao.exists(category)) { // カテゴリが存在しない場合
                    ProductCategory productCategory = new ProductCategory(category);
                    categoryDao.insert(productCategory); // まずProductCategoryをDBに登録
                    product.setCategoryId(categoryDao.getByName(category).getCategoryId()); // 登録後のIDを取得
                } else {
                    product.setCategoryId(categoryDao.getByName(category).getCategoryId());
                }
            }
        }
        product.setProductName(productName);// 必須情報
        product.setPrice(price);
        product.setQuantity(quantity);// 必須情報
        product.setLocation(location);
        product.setExpirationDate(expirationDate);
        product.setPurchaseDate(purchaseDate);
        product.setImagePath(imagePath);
        productDao.insert(product);
        return true;
    }


    public boolean updateProduct(Product product) {
        productDao.update(product);
        return true;
    }

    public boolean deleteProduct(Product product) {

        productDao.delete(product);
        return true;
    }



    // 全ての製品を取得するメソッド
    public Product[] getAllProducts() {
        return productDao.getAll().toArray(new Product[0]);
    }
}
