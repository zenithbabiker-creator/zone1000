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
    val context = LocalContext.current

    // التأكد من تنظيف الموارد عند خروج المستخدم من الشاشة
    DisposableEffect(Unit) {
        onDispose {
            // هنا يمكنك إضافة كود لإيقاف الجلسة إذا كان الـ ARSessionManager يدعم ذلك
            // arSessionManager.pause() أو ما يشابهه
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ARSceneView(ctx).apply {
                    onSessionUpdated = { session, frame ->
                        arSessionManager.bindSession(session)
                        arSessionManager.onFrameUpdated(session, frame)
                    }
                }
            }
        )
    }
}
