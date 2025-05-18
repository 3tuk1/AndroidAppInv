package com.inv.inventryapp.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.inv.inventryapp.models.Barcode;

import java.util.List;

public interface BarcodeDao {
    // Insert a new barcode
    @Insert
    void insert(Barcode barcode);

    // Update an existing barcode
    @Update
    void update(Barcode barcode);

    // Delete a barcode
    @Delete
    void delete(Barcode barcode);

    // Get all barcodes
    @Query("SELECT * FROM barcodes")
    List<Barcode> getAllBarcodes();

    // Get a barcode by its ID
    @Query("SELECT * FROM barcodes WHERE id = :id")
    Barcode getBarcodeById(int id);
}
