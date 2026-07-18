package com.example.landscapedesign.ui

import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.landscapedesign.ar.ARSessionManager
import com.google.ar.core.Frame
import io.github.sceneview.ar.ARSceneView

@Composable
fun ArCameraPreview(
    // تم إضافة context هنا ليتوافق مع متطلبات الاستدعاء التي ظهرت في الأخطاء
    context: Context = LocalContext.current, 
    arSessionManager: ARSessionManager,
    onTap: (x: Float, y: Float, frame: Frame?) -> Unit,
    modifier: Modifier = Modifier
) {
    var latestFrame by remember { mutableStateOf<Frame?>(null) }

    // استخدام الـ modifier الممرر للحاوية الخارجية
    Box(modifier = modifier) {
        
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            onSessionUpdated = { session, frame ->
                latestFrame = frame
                arSessionManager.bindSession(session)
                arSessionManager.onFrameUpdated(session, frame)
            }
        )

        // طبقة شفافة فوق الـ AR لتلقي اللمسات
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onTap(offset.x, offset.y, latestFrame)
                    }
                }
        )
    }
}
