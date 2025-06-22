package com.inv.inventryapp.view.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.inv.inventryapp.R
import com.inv.inventryapp.databinding.CalendarDayLayoutBinding
import com.inv.inventryapp.databinding.FragmentAnalysisCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentAnalysisCalendarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12) // 過去1年
        val endMonth = currentMonth.plusMonths(12)    // 未来1年
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        // 日付セルの描画設定
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                // TODO: ここで日付ごとのイベントを実装します。
            }
        }

        // 月ヘッダーの描画設定
        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // 月のタイトルを設定
                container.monthTitle.text = "${month.yearMonth.year}年 ${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.JAPANESE)}"

                // 曜日のテキストを設定 (初回のみ)
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = true
                    val daysOfWeek = com.kizitonwose.calendar.core.daysOfWeek()
                    container.legendLayout.children.forEachIndexed { index, childView ->
                        (childView as? TextView)?.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.JAPANESE)
                    }
                }
            }
        }
    }

    // 日付セル用のViewContainer
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = CalendarDayLayoutBinding.bind(view).calendarDayText
    }

    // 月ヘッダー用のViewContainerを追加
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val legendLayout: ViewGroup = view.findViewById(R.id.legend_layout)
        val monthTitle: TextView = view.findViewById(R.id.calendar_month_text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
