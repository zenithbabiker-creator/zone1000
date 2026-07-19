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
    // استخدم الدالة المعرفة في ArCameraPreview.kt هنا
    ArCameraPreview(
        arSessionManager = arSessionManager,
        onTap = { x, y, frame -> 
            // المنطق الخاص بك هنا
        }
    )
}
