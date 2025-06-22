package com.inv.inventryapp.view.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.inv.inventryapp.databinding.AddOptionsBottomSheetBinding

class AddOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: AddOptionsBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var listener: AddOptionsListener? = null

    interface AddOptionsListener {
        fun onBarcodeAddSelected()
        fun onManualAddSelected()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? AddOptionsListener ?: context as? AddOptionsListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addWithBarcode.setOnClickListener {
            listener?.onBarcodeAddSelected()
            dismiss()
        }
        binding.addManually.setOnClickListener {
            listener?.onManualAddSelected()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddOptionsBottomSheet"
    }
}

