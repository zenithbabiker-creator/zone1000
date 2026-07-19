package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.landscapedesign.ar.ARSessionManager
import com.google.ar.core.Frame
import io.github.sceneview.ar.ARSceneView

@Composable
fun ArCameraPreview(
    arSessionManager: ARSessionManager,
    onTap: (x: Float, y: Float, frame: Frame?) -> Unit,
    modifier: Modifier = Modifier
) {
    // تم نقل الـ context إلى الداخل ليكون محلياً ولا يحتاج تمريره كبارامتر
    val context = LocalContext.current 
    
    Box(modifier = modifier) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            onSessionUpdated = { session, frame ->
                arSessionManager.bindSession(session)
                arSessionManager.onFrameUpdated(session, frame)
            }
        )
    }
}
