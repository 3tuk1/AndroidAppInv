package com.inv.inventryapp.view.camera

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var barcodeListener: ((String) -> Unit)? = null

    init {
        previewView = PreviewView(context)
        addView(previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, mode: CameraMode) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()

                val useCaseList = mutableListOf<UseCase>(preview)

                if (mode == CameraMode.PHOTO) {
                    imageCapture = ImageCapture.Builder().build()
                    imageCapture?.let { useCaseList.add(it) }
                } else if (mode == CameraMode.BARCODE) {
                    imageAnalyzer = ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                                barcodeListener?.invoke(barcode)
                                cameraProvider?.unbindAll()
                            })
                        }
                    imageAnalyzer?.let { useCaseList.add(it) }
                }

                cameraProvider?.bindToLifecycle(
                    lifecycleOwner, cameraSelector, *useCaseList.toTypedArray()
                )
            } catch (exc: Exception) {
                // Log error
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun setBarcodeListener(listener: (String) -> Unit) {
        this.barcodeListener = listener
    }

    fun takePhoto(outputFilePath: String, onImageSaved: (savedUri: Uri?, errorMessage: String?) -> Unit) {
        val imageCapture = imageCapture ?: return

        val photoFile = File(outputFilePath)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    onImageSaved(null, "Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    onImageSaved(savedUri, null)
                }
            }
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cameraExecutor.shutdown()
    }

    private class BarcodeAnalyzer(private val barcodeListener: (String) -> Unit) : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient()

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            barcodes.first().rawValue?.let {
                                barcodeListener(it)
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Log error
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    enum class CameraMode {
        PHOTO, BARCODE
    }
}
