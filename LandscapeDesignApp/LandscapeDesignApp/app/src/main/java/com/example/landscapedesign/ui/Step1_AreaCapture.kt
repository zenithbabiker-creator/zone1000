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
    // تم استدعاء ArCameraPreview بشكل صحيح مع إغلاق الأقواس بشكل سليم
    ArCameraPreview(
        arSessionManager = arSessionManager,
        onTap = { x, y, frame -> 
            // يمكنك هنا إضافة المنطق الخاص بك عند النقر على الشاشة
        }
    )
}
