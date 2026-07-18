package com.example.landscapedesign.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.landscapedesign.ar.ARSessionManager
import com.google.ar.core.Frame
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.PlaneRenderer

/**
 * ArCameraPreview: A robust ARCore camera host for LandscapeDesignApp.
 * Updated to match the ARSceneView Compose API signature (Version 2.2.1).
 */
@Composable
fun ArCameraPreview(
    arSessionManager: ARSessionManager,
    onTap: (x: Float, y: Float, frame: Frame?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current ARCore Frame state for tap resolution
    var latestFrame by remember { mutableStateOf<Frame?>(null) }

    // PlaneRenderer needs to be initialized correctly for sceneview 2.2.1
    val planeRenderer = remember { PlaneRenderer() }

    Box(modifier = modifier) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            planeRenderer = planeRenderer,
            onSessionUpdated = { session, frame ->
                latestFrame = frame
                // Binding session and updating frame logic
                arSessionManager.bindSession(session)
                arSessionManager.onFrameUpdated(session, frame)
            }
        )

        // Overlay Box for handling gestures, independent of ARSceneView parameters
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
