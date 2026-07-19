package com.example.landscapedesign.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.landscapedesign.ar.ARSessionManager
import com.google.ar.core.Frame
import io.github.sceneview.ar.ARSceneView

@Composable
fun ArCameraPreview(
    arSessionManager: ARSessionManager,
    onTap: (x: Float, y: Float, frame: Frame?) -> Unit
) {
    val context = LocalContext.current // جلب السياق الحالي

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // إنشاء الـ ARSceneView يدوياً
                ARSceneView(ctx).apply {
                    onSessionUpdated = { session, frame ->
                        arSessionManager.bindSession(session)
                        arSessionManager.onFrameUpdated(session, frame)
                    }
                }
            },
            update = { arSceneView ->
                // تحديثات إضافية إذا لزم الأمر
            }
        )
    }
}
