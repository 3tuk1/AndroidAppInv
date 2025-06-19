package com.inv.inventryapp.repository;

import android.content.Context;
import com.inv.inventryapp.model.ModelDatabase;
import com.inv.inventryapp.model.dao.BarcodeDao;
import com.inv.inventryapp.model.dao.ProductDao;
import com.inv.inventryapp.model.entity.Barcode;
import com.inv.inventryapp.model.entity.Product;
import java.time.LocalDate;

public class ProductRepository {
    private final ProductDao productDao;
    private final BarcodeDao barcodeDao;

    public ProductRepository(Context context) {
        ModelDatabase db = ModelDatabase.getInstance(context);
        this.productDao = db.productDao();
        this.barcodeDao = db.barcodeDao(); // barcodeDaoの初期化
    }

    public boolean addProduct(String productName, int price, int quantity, String location, LocalDate expirationDate, LocalDate purchaseDate, String imagePath, int categoryId,int barcodeNumber) {
        Product product = new Product();
        if(productName == null || productName.isEmpty()|| quantity != 0) {
            return false; // 商品名が空または数量が0の場合は追加しない
        } else if (barcodeNumber != 0) {
            if(!barcodeDao.exists(barcodeNumber)) { // barcodeDaoでバーコードが存在しない場合
                Barcode barcode = new Barcode(barcodeNumber);
                product.setBarcodeId(barcode.getBarcodeId()); // barcodeidはBarcodeDaoで生成されたIDを使用する必要があります
            }else {
                product.setBarcodeId(barcodeDao.getByCode(barcodeNumber).getBarcodeId());
            }
        }
        product.setProductName(productName);// 必須情報
        product.setPrice(price);
        product.setQuantity(quantity);// 必須情報
        product.setLocation(location);
        product.setExpirationDate(expirationDate);
        product.setPurchaseDate(purchaseDate);
        product.setImagePath(imagePath);
        product.setCategoryId(categoryId);
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
}
