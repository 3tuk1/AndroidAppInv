package com.inv.inventryapp.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "barcode")
public class Barcode {
    /**
     * バーコードエンティティ
     * 商品のバーコード情報を保持するクラス
     * 各フィールドはデータベースのカラムに対応しており、
     * 主キーとしてbarcodeIdを使用します。
     * 各バーコードは、バーコード番号を持ちます。
     */
    @PrimaryKey(autoGenerate = true)
    private int barcodeId;

    @ColumnInfo(name = "barcode_number")
    private int barcodeNumber;

    // コンストラクタ
    public Barcode(int barcodeNumber) {
        this.barcodeNumber = barcodeNumber;
    }

    // getter, setter
    public int getBarcodeId() { return barcodeId; }
    public void setBarcodeId(int barcodeId) { this.barcodeId = barcodeId; }
    public int getBarcodeNumber() { return barcodeNumber; }
    public void setBarcodeNumber(int barcodeNumber) { this.barcodeNumber = barcodeNumber; }
}

