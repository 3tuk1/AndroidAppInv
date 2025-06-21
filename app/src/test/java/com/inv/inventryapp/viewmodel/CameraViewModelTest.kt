package com.inv.inventryapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CameraViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CameraViewModel

    @Before
    fun setUp() {
        viewModel = CameraViewModel()
    }

    @Test
    fun `onBarcodeScanned should update scannedBarcode LiveData`() {
        // Given
        val barcode = "1234567890"

        // When
        viewModel.onBarcodeScanned(barcode)

        // Then
        val value = viewModel.scannedBarcode.value
        assertEquals(barcode, value)
    }

    @Test
    fun `onPhotoTaken should update takenPhotoUri LiveData`() {
        // Given
        val uri = "content://media/external/images/media/123"

        // When
        viewModel.onPhotoTaken(uri)

        // Then
        val value = viewModel.takenPhotoUri.value
        assertEquals(uri, value)
    }
}

