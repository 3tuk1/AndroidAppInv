package com.inv.inventryapp.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;

public class Converters {
    @TypeConverter
    public static byte[] fromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        // Bitmap を byte[] に変換
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @TypeConverter
    public static Bitmap toBitmap(byte[] byteArray) {
        if (byteArray == null) return null;

        // byte[] を Bitmap に変換
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}