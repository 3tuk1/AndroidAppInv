package com.inv.inventryapp.utility;

import java.time.LocalDate;
import java.time.YearMonth;

public class ManageCalendar extends CalendarSetup{
    private static final ManageCalendar instance = new ManageCalendar();
    private String selectedDate;
    private ManageCalendar() {
        super();
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
