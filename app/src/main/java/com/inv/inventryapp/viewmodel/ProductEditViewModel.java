package com.inv.inventryapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.inv.inventryapp.model.entity.Product;

import java.time.LocalDate;

public class ProductEditViewModel extends ViewModel {
    // xml ライブデータ
    /*
    private final MutableLiveData<String> productName = new MutableLiveData<>();
    private final MutableLiveData<Integer> productPrice = new MutableLiveData<>();
    private final MutableLiveData<String> productCategory = new MutableLiveData<>();
    private final MutableLiveData<Integer> productQuantity = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> productExpirationDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> productPurchaseDate = new MutableLiveData<>();
    private final MutableLiveData<String> productLocation = new MutableLiveData<>();
    private final MutableLiveData<String> productImagePath = new MutableLiveData<>();
    private final MutableLiveData<Integer> productBarcodeId = new MutableLiveData<>();
    */
    private final MutableLiveData<Product> product = new MutableLiveData<>(new Product());

}

