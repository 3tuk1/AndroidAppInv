package com.inv.inventryapp.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;

public class Converters {
    ///
    /// とった画像はbyte
    // Bitmap を byte[] に変換するメソッド
    public static Bitmap compressImage(Bitmap original) {
        if (original == null) return null;

        int width = original.getWidth();
        int height = original.getHeight();
        float maxSize = 512.0f; // 最大サイズを512pxに縮小

        float scale = Math.min(maxSize / width, maxSize / height);
        if (scale >= 1) return original; // すでに小さい場合はそのまま

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);


        Bitmap newBitmap = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

        return newBitmap;
    }
}