package com.inv.inventryapp.utility;

import java.time.LocalDate;
import java.time.YearMonth;

public class ManageCalendar extends CalendarSetup{
    private static final ManageCalendar instance = new ManageCalendar();
    private String selectedDate;
    ManageCalendar() {
        super();
    }

    void selectBind() {
        // カレンダーの初期化処理をここに記述
        // 例えば、カレンダーのビューを設定するなど
    }
    public static ManageCalendar getInstance() {
        return instance;
    }

    @Override
    public void onDateSelected(LocalDate date) {
        selectedDate = date.toString();
    }

    @Override
    public void onMonthChanged(YearMonth month) {

    }


}
