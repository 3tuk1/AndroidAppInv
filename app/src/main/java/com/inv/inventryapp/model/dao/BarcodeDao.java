package com.inv.inventryapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;
import com.inv.inventryapp.model.entity.Barcode;

@Dao
public interface BarcodeDao {
    @Insert
    void insert(Barcode barcode);

    @Update
    void update(Barcode barcode);

    @Delete
    void delete(Barcode barcode);

    @Query("SELECT * FROM barcode")
    List<Barcode> getAll();

    // そのバーコードが存在するかどうかを確認するメソッド返り値はboolean
    // 引数はint型のバーコード番号
    // 存在する場合はtrue、存在しない場合はfalseを返す
    @Query("SELECT EXISTS(SELECT 1 FROM barcode WHERE barcode_number = :code)")
    boolean exists(int code);

    // barcode_numberでバーコードを検索し、存在する場合はそのBarcodeオブジェクトを返す
    @Query("SELECT * FROM barcode WHERE barcode_number = :code LIMIT 1")
    Barcode getByCode(int code);

}

