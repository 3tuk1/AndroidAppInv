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
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
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

        // 月のヘッダーにある曜日のテキストを設定
        val legendLayout = binding.root.findViewById<ViewGroup>(R.id.legend_layout) //前回XMLに追加したID
        val daysOfWeek = com.kizitonwose.calendar.core.daysOfWeek()
        legendLayout.children.forEachIndexed { index, childView ->
            (childView as? TextView)?.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.JAPANESE)
        }

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
                // TODO: ここで日付ごとのイベント（消費履歴など）をDBから取得し、
                //       ドットを表示するなどの処理を実装します。
            }
        }

        // 月のヘッダーのテキストを設定
        binding.calendarView.monthScrollListener = { month ->
            val monthTitle = "${month.yearMonth.year}年 ${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.JAPANESE)}"
            binding.root.findViewById<TextView>(R.id.calendar_month_text).text = monthTitle //前回XMLに追加したID
        }
    }

    // 日付セル用のViewContainer
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = CalendarDayLayoutBinding.bind(view).calendarDayText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

