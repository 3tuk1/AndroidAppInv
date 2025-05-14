package com.inv.inventryapp.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;

public class Converters {
    ///
    /// とった画像はbyte
    // Bitmap を byte[] に変換するメソッド
    @TypeConverter
    public static byte[] fromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        // より小さいサイズに圧縮してから変換
        Bitmap compressedBitmap = compressImage(bitmap);

        // JPEG形式で圧縮率を上げる（品質を下げる）
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

        // 元のビットマップと圧縮したビットマップが異なる場合はリサイクル
        if (compressedBitmap != bitmap) {
            compressedBitmap.recycle();
        }

        return outputStream.toByteArray();
    }

    // byte[] を Bitmap に変換するメソッド
    @TypeConverter
    public static Bitmap toBitmap(byte[] byteArray) {
        if (byteArray == null) return null;

        // byte[] を Bitmap に変換
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    // ビットマップを圧縮するメソッド
    private static Bitmap compressImage(Bitmap original) {
        if (original == null) return null;

        int width = original.getWidth();
        int height = original.getHeight();
        float maxSize = 512.0f; // 最大サイズを512pxに縮小

        float scale = Math.min(maxSize / width, maxSize / height);
        if (scale >= 1) return original; // すでに小さい場合はそのまま

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}