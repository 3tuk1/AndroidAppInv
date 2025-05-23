package com.inv.inventryapp.utility;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.inv.inventryapp.R;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public abstract class CalendarSetup {
    CalendarView calendarView;


    // ヘルパーメソッドとして実装
    public void setupCalendar(CalendarView calendarView,View view) {
        this.calendarView = calendarView;
        // 開始日と終了日を設定
        TextView monthTextView = view.findViewById(R.id.monthTextView);
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(6);
        YearMonth endMonth = currentMonth.plusMonths(6);
        DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;

        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);
        if(monthTextView != null) {
            monthTextView.setText(currentMonth.toString());
        }
        selectBind();
        // 月が変わったときのリスナー
        calendarView.setMonthScrollListener(month -> {
            // 月が変わったときの処理
            if(monthTextView != null) {
                monthTextView.setText(month.getYearMonth().toString());
            }

            YearMonth selectedMonth = month.getYearMonth();
            onMonthChanged(selectedMonth);
            return null;
        });
    }

    abstract void selectBind();

    // 日付をクリックしたときの処理
    public abstract void onDateSelected(LocalDate date);
    // 月が変わったときの処理
    public abstract void onMonthChanged(YearMonth month);


    // ViewContainerクラス
    public class DayViewContainer extends ViewContainer {
        public final TextView textView;

        public DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }
}