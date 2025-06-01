package com.inv.inventryapp.utility;

import android.util.Log;
import android.view.View;

import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;

import java.time.LocalDate;
import java.time.YearMonth;

public class ManageCalendar extends CalendarSetup{
    private static final ManageCalendar instance = new ManageCalendar();
    private LocalDate selectedDate;
    private DateSelectedListener dateSelectedListener;

    // 日付選択リスナーのインターフェース
    public interface DateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    ManageCalendar() {
        super();
    }

    public static ManageCalendar getInstance() {
        return instance;
    }

    /**
     * CalendarViewが設定された後に呼び出す必要があるメソッド
     * @return 設定済みのManageCalendarインスタンス
     */
    public ManageCalendar initializeCalendar() {
        if (calendarView != null) {
            selectBind();
        }
        return this;
    }

    @Override
    public void selectBind() {
        if (calendarView == null) {
            Log.e("ManageCalendar", "calendarView is null in selectBind()");
            return;
        }

        try {
            calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
                @Override
                public DayViewContainer create(View view) {
                    return new DayViewContainer(view);
                }

                @Override
                public void bind(DayViewContainer container, CalendarDay day) {
                    // container.textViewがnullチェックされているか確認
                    if (container == null || day == null || day.getDate() == null) {
                        Log.w("ManageCalendar", "bind: container or day or day.getDate() is null. Skipping bind.");
                        return; // 無効なパラメータはスキップ
                    }

                    // container.textViewがnullの場合の処理を追加
                    if (container.textView == null) {
                        Log.e("ManageCalendar", "bind: container.textView is null. Day: " + day.getDate() + ". This usually means R.id.calendarDayText was not found in the day layout.");
                        // textViewがnullの場合、これ以上の処理はできないためreturn
                        return;
                    }

                    container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                    if (day.getPosition() == DayPosition.MonthDate) {
                        container.textView.setTextColor(android.graphics.Color.BLACK);
                        container.getView().setOnClickListener(v -> {
                            onDateSelected(day.getDate());
                        });
                    } else {
                        container.textView.setTextColor(android.graphics.Color.GRAY);
                        container.getView().setOnClickListener(null);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("ManageCalendar", "Error setting day binder", e);
        }
    }

    @Override
    public void onDateSelected(LocalDate date) {
        selectedDate = date;
        if (dateSelectedListener != null) {
            dateSelectedListener.onDateSelected(date);
        }
    }

    @Override
    public void onMonthChanged(YearMonth month) {
        // 月が変更された時の処理
    }

    public void setOnDateSelectedListener(DateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }
}
