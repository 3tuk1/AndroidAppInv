package com.inv.inventryapp.view.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.inv.inventryapp.R
import com.inv.inventryapp.viewmodel.CameraViewModel
import java.io.File

class CameraFragment : Fragment() {

    private lateinit var cameraView: CameraPhotoView
    private lateinit var viewModel: CameraViewModel
    private lateinit var shutterButton: Button
    private var mode: CameraPhotoView.CameraMode = CameraPhotoView.CameraMode.PHOTO // Default mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mode = it.getSerializable(ARG_CAMERA_MODE) as? CameraPhotoView.CameraMode
                ?: CameraPhotoView.CameraMode.PHOTO
        }
        viewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_camera_photo.xmlを再利用
        return inflater.inflate(R.layout.fragment_camera_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraView = view.findViewById(R.id.camera_photo_view)
        shutterButton = view.findViewById(R.id.shutter_button)

        if (allPermissionsGranted()) {
            startCameraBasedOnMode()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCameraBasedOnMode() {
        cameraView.startCamera(viewLifecycleOwner, mode)
        if (mode == CameraPhotoView.CameraMode.PHOTO) {
            setupPhotoCapture()
        } else {
            setupBarcodeScanner()
        }
    }

    private fun setupPhotoCapture() {
        shutterButton.visibility = View.VISIBLE
        shutterButton.setOnClickListener {
            val photoFile = File(
                requireContext().externalMediaDirs.firstOrNull(),
                "${System.currentTimeMillis()}.jpg"
            )

            cameraView.takePhoto(photoFile.absolutePath) { savedUri, errorMessage ->
                activity?.runOnUiThread {
                    if (savedUri != null) {
                        Toast.makeText(requireContext(), "写真が保存されました: $savedUri", Toast.LENGTH_LONG).show()
                        viewModel.onPhotoTaken(savedUri.toString())
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "保存失敗: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupBarcodeScanner() {
        shutterButton.visibility = View.GONE
        cameraView.setBarcodeListener { barcode ->
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "スキャン成功: $barcode", Toast.LENGTH_SHORT).show()
                viewModel.onBarcodeScanned(barcode)
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCameraBasedOnMode()
            } else {
                Toast.makeText(requireContext(), "カメラ権限がありません", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 12
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val ARG_CAMERA_MODE = "camera_mode"

        @JvmStatic
        fun newInstance(mode: CameraPhotoView.CameraMode) =
            CameraFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CAMERA_MODE, mode)
                }
            }
    }
}
