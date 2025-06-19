package com.inv.inventryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {

    private val _scannedBarcode = MutableLiveData<String>()
    val scannedBarcode: LiveData<String>
        get() = _scannedBarcode

    private val _takenPhotoUri = MutableLiveData<String>()
    val takenPhotoUri: LiveData<String>
        get() = _takenPhotoUri

    fun onBarcodeScanned(barcode: String) {
        // バックグラウンドスレッドから呼び出される可能性を考慮してpostValueを使用
        _scannedBarcode.postValue(barcode)
    }

    fun onPhotoTaken(uri: String) {
        _takenPhotoUri.postValue(uri)
    }
}
