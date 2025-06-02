package com.inv.inventryapp.utility;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.inv.inventryapp.R;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public abstract class CalendarSetup {
    CalendarView calendarView;


    // ヘルパーメソッドとして実装
    public void setupCalendar(CalendarView calendarView, View view) {
        this.calendarView = calendarView;
        // 開始日と終了日を設定
        TextView monthTextView = view.findViewById(R.id.monthTextView); // This is the external month TextView
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(6);
        YearMonth endMonth = currentMonth.plusMonths(6);
        DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;

        if (this.calendarView != null) {
            this.calendarView.setup(startMonth, endMonth, firstDayOfWeek);
            this.calendarView.scrollToMonth(currentMonth);

            // Month Header Binder
            this.calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
                @NonNull
                @Override
                public MonthViewContainer create(@NonNull View headerView) {
                    // Ensure your CalendarView XML uses app:cv_month_header_resource pointing to a layout
                    // that contains R.id.monthHeaderTextView (or whatever ID you use in MonthViewContainer)
                    return new MonthViewContainer(headerView);
                }

                @Override
                public void bind(@NonNull MonthViewContainer container, @NonNull CalendarMonth calendarMonth) {
                    if (container.headerTextView != null && calendarMonth != null && calendarMonth.getYearMonth() != null) {
                        // Example: "JUNE 2025"
                        String monthTitle = calendarMonth.getYearMonth().getMonth().name() + " " + calendarMonth.getYearMonth().getYear();
                        container.headerTextView.setText(monthTitle);
                    } else if (container.headerTextView == null) {
                        android.util.Log.e("CalendarSetup", "MonthViewContainer.headerTextView is null. Check month header layout.");
                    }
                }
            });

            // 月が変わったときのリスナー
            this.calendarView.setMonthScrollListener(month -> {
                // 月が変わったときの処理
                if (monthTextView != null && month != null && month.getYearMonth() != null) {
                    monthTextView.setText(month.getYearMonth().toString());
                }

                if (month != null && month.getYearMonth() != null) {
                    YearMonth selectedMonth = month.getYearMonth();
                    onMonthChanged(selectedMonth);
                }
                return null; // KotlinのUnitに対応するためnullを返す
            });
        } else {
            android.util.Log.e("CalendarSetup", "calendarView is null in setupCalendar. Cannot setup.");
        }

        if (monthTextView != null && currentMonth != null) {
            monthTextView.setText(currentMonth.toString());
        }
    }

    public abstract void selectBind();

    // 日付をクリックしたときの処理
    public abstract void onDateSelected(LocalDate date);

    // 月が変わったときの処理
    public abstract void onMonthChanged(YearMonth month);


    // ViewContainerクラス for Day
    public class DayViewContainer extends ViewContainer {
        public TextView textView; // finalを削除して、nullの場合のフォールバックを可能にする

        public DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
            if (textView == null) {
                // Log an error, but don't throw an exception to prevent immediate crash.
                android.util.Log.e("DayViewContainer",
                    "calendarDayText is null. View ID: " + (view.getId() == View.NO_ID ? "no_id" : view.getResources().getResourceEntryName(view.getId())) +
                    ", View class: " + view.getClass().getName() +
                    ". Check your day layout XML file (e.g., calendar_day_layout.xml) " +
                    "and ensure it contains a TextView with android:id=\"@+id/calendarDayText\"."
                );
            }
        }
    }

    // ViewContainerクラス for Month Header
    public class MonthViewContainer extends ViewContainer {
        public TextView headerTextView;

        public MonthViewContainer(@NonNull View view) {
            super(view);
            // Ensure this ID matches the TextView in your month header layout XML
            // (e.g., R.layout.calendar_month_header_layout.xml)
            headerTextView = view.findViewById(R.id.monthHeaderTextView);
            if (headerTextView == null) {
                android.util.Log.e("MonthViewContainer",
                    "monthHeaderTextView is null. View ID: " + (view.getId() == View.NO_ID ? "no_id" : view.getResources().getResourceEntryName(view.getId())) +
                    ", View class: " + view.getClass().getName() +
                    ". Check your month header layout XML file (e.g., calendar_month_header_layout.xml) " +
                    "and ensure it contains a TextView with android:id=\"@+id/monthHeaderTextView\"."
                );
            }
        }
    }
}
