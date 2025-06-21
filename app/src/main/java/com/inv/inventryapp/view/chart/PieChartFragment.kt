package com.inv.inventryapp.view.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inv.inventryapp.databinding.FragmentPieChartBinding

class PieChartFragment : Fragment() {

    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここでViewModelを初期化し、データを監視してPieChartViewを更新します。
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

