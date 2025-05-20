package com.inv.inventryapp.utility;

import java.time.LocalDate;
import java.time.YearMonth;

public class SelectCalendar extends CalendarSetup{
    private static final SelectCalendar instance = new SelectCalendar();
    private String selectedDate;
    private SelectCalendar() {
        super();
    }
    public static SelectCalendar getInstance() {
        return instance;
    }

    @Override
    public void onDateSelected(LocalDate date) {
        selectedDate = date.toString();
    }

    @Override
    public void onMonthChanged(YearMonth month) {

    }
    public String getSelectedDate() {
        // Implement the logic to get the selected date
        return selectedDate;
    }
}
