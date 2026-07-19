package com.example.landscapedesign.ui

import androidx.compose.runtime.*
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step1AreaCaptureScreen(
    viewModel: LandscapeViewModel,
    arSessionManager: ARSessionManager,
    onNext: () -> Unit
) {
    ArCameraPreview(
        arSessionManager = arSessionManager,
        onTap = { x, y, frame -> /* لا يوجد منطق حالياً */ }
    )
}
