package com.inv.inventryapp.view.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.inv.inventryapp.R
import com.inv.inventryapp.databinding.FragmentPieChartBinding
import com.inv.inventryapp.viewmodel.PieChartViewModel
import java.text.NumberFormat

class PieChartFragment : Fragment() {

    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PieChartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pieChartSummary.observe(viewLifecycleOwner) { summary ->
            summary ?: return@observe

            // 円グラフのデータと色を設定
            val pieData = listOf(
                summary.pieDataMap["購入"] ?: 0f,
                summary.pieDataMap["消費"] ?: 0f,
                summary.pieDataMap["廃棄"] ?: 0f
            )
            val pieColors = listOf(
                ContextCompat.getColor(requireContext(), R.color.pie_color_purchase),
                ContextCompat.getColor(requireContext(), R.color.pie_color_consumption),
                ContextCompat.getColor(requireContext(), R.color.pie_color_disposal)
            )
            binding.pieChartView.setData(pieData, pieColors)

            // 円グラフ中央のテキストを更新（合計支出）
            val totalSpending = summary.totalPurchase - summary.totalConsumption - summary.totalDisposal
            binding.pieChartView.setCenterText(formatCurrency(totalSpending), "今月の支出")

            // 下部のテキスト表示を更新
            binding.textPurchaseTotal.text = formatCurrency(summary.totalPurchase)
            binding.textConsumptionTotal.text = formatCurrency(summary.totalConsumption)
            binding.textDisposalTotal.text = formatCurrency(summary.totalDisposal)
            binding.textStockTotal.text = formatCurrency(summary.currentStockValue)
            binding.textOverallTotal.text = formatCurrency(summary.overallTotal)
        }

        viewModel.loadPieChartData()
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance().format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
