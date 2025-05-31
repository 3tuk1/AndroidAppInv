package com.inv.inventryapp.utility;

import android.annotation.SuppressLint;

import java.time.LocalDate;

public class ConvertDate {

    /**
     * LocalDateを文字列に変換するメソッド
     * @param date LocalDate型の日付
     * @return "yyyy-MM-dd"形式の文字列
     */
    public static String localDateToString(LocalDate date) {
        // yyyy年/MM月/dd日形式の文字列に変換
        if (date == null) {
            return ""; // dateがnullの場合は空文字を返す
        }
        String year = String.valueOf(date.getYear());
        @SuppressLint("DefaultLocale") String month = String.format("%02d", date.getMonthValue());
        @SuppressLint("DefaultLocale") String day = String.format("%02d", date.getDayOfMonth());
        return year+"年"+ month+"月"+ day+"日"; // 変換した文字列を返す
    }

    /**
     * 文字列をLocalDateに変換するメソッド
     * @param dateStr "yyyy-MM-dd"形式の日付文字列
     * @return LocalDate型の日付
     */
    public static LocalDate stringToLocalDate(String dateStr) {
        if(dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        // yyyy年MM月dd日 形式を試す
        try {
            String[] parts = dateStr.split("年|月|日");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                return LocalDate.of(year, month, day);
            }
        } catch (Exception ignored) {
            // この形式での変換に失敗した場合は次の形式を試す
        }

        // yyyy-MM-dd 形式を試す
        try {
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                return LocalDate.of(year, month, day);
            }
        } catch (Exception ignored) {
            // この形式での変換に失敗した場合は次の形式を試す
        }

        // yyyy/MM/dd 形式を試す
        try {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                return LocalDate.of(year, month, day);
            }
        } catch (Exception ignored) {
            // この形式での変換にも失敗した場合
        }

        // すべての形式で失敗した場合はエラーをスロー
        throw new IllegalArgumentException("Invalid date format. Expected format: yyyy年MM月dd日");
    }

}
