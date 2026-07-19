package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step1AreaCaptureScreen(
    viewModel: LandscapeViewModel,
    arSessionManager: ARSessionManager,
    onNext: () -> Unit
) {
    // تم استخدام الدالة المحدثة التي تقبل المعاملات الاختيارية
    ArCameraPreview(
        arSessionManager = arSessionManager,
        modifier = Modifier.fillMaxSize(),
        onTap = { x, y, frame -> 
            // يمكنك هنا إضافة المنطق الخاص بك عند النقر على الشاشة
        }
    )
}
