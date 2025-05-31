package com.inv.inventryapp.utility;

import android.view.View;
import android.widget.TextView;
import com.inv.inventryapp.R;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.LocalDate;
import java.time.YearMonth;

public class SelectCalendar extends ManageCalendar {
    private static final SelectCalendar instance = new SelectCalendar();
    private LocalDate currentSelectedDate; // 選択された日付をLocalDate型で保持
    // private Context context; // 未使用のため削除

    private SelectCalendar() {
        super();
    }
    void selectBind(){
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                TextView textView = container.textView;
                LocalDate date = day.getDate();

                // 日付を表示
                textView.setText(String.valueOf(date.getDayOfMonth()));

                // 当月以外の日付はグレーアウト
                if (day.getPosition() == DayPosition.MonthDate) {
                    textView.setAlpha(1f);
                    container.view.setClickable(true); // 当月の日付のみクリック可能に

                    // 選択状態に応じて背景を変更
                    if (date.equals(currentSelectedDate)) {
                        container.view.setBackgroundResource(R.drawable.calendar_day_selected);
                    } else {
                        container.view.setBackgroundResource(R.drawable.calendar_day_normal);
                    }

                    // クリックリスナーを設定
                    container.view.setOnClickListener(v -> {
                        LocalDate oldDate = currentSelectedDate;
                        currentSelectedDate = date;
                        // 以前の日付と新しい日付の表示を更新
                        calendarView.notifyDateChanged(date);
                        if (oldDate != null) {
                            calendarView.notifyDateChanged(oldDate);
                        }
                        onDateSelected(date); // 選択処理を実行
                    });

                } else {
                    textView.setAlpha(0.3f);
                    container.view.setClickable(false); // 当月以外の日付はクリック不可に
                    container.view.setBackgroundResource(R.drawable.calendar_day_normal); // 非選択状態の背景
                }
            }
        });
    }

    public static SelectCalendar getInstance() {
        if (instance.calendarView != null) {
            instance.selectBind();
        }
        return instance;
    }


    @Override
    public void onDateSelected(LocalDate date) {
        // ここでの currentSelectedDate = date; は TextView への表示目的が主
        currentSelectedDate = date;
    }

    @Override
    public void onMonthChanged(YearMonth month) {
        // 月が変わったときに選択状態をリセットする場合は currentSelectedDate を null にする
    }



    public LocalDate getSelectedDate() {
        if (currentSelectedDate != null) {
            return currentSelectedDate;
        }
        return null;
    }

    public boolean hasDateSelected() {
        return currentSelectedDate != null;
    }

    /**
     * 日付ビューのコンテナクラス
     */
    private static class DayViewContainer extends ViewContainer {
        public final TextView textView;
        public final View view;

        public DayViewContainer(View view) {
            super(view);
            this.view = view;
            this.textView = view.findViewById(R.id.calendarDayText);
        }
    }
}