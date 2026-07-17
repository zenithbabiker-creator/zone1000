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

/**
 * Hosts the live ARCore camera feed inside Compose using SceneView's
 * [ARSceneView] — camera passthrough (via Filament) and the horizontal-plane
 * scanning mesh (`planeRenderer`) are rendered automatically; zero manual
 * GLES/shader code is written here.
 *
 * ── COMPATIBILITY NOTE (why this file was rewritten) ──────────────────────
 * `io.github.sceneview:arsceneview` went through a breaking API generation
 * change: pre-2.x releases exposed an instantiable View class
 * `io.github.sceneview.ar.ArSceneView` (lowercase "r") configured
 * imperatively — `ArSceneView(ctx).apply { onSessionConfiguration = ...;
 * onArSessionFailed = ...; planeRenderer.isVisible = true }` inside an
 * `AndroidView` factory. The pinned version (2.2.1) ships the CURRENT
 * generation instead: `io.github.sceneview.ar.ARSceneView` (capital "AR") is
 * a plain **Compose function**, not a class you construct — so the old
 * `ArSceneView(ctx).apply { ... }` / `onSessionConfiguration` /
 * `onArSessionFailed` code failed to compile with `Unresolved reference`
 * and `Cannot find parameter` errors. That mismatch was the root cause of
 * the build failures reported.
 *
 * This rewrite intentionally uses ONLY the parameters verified directly
 * against the library's public source (github.com/sceneview/sceneview,
 * `arsceneview` module): `modifier`, `planeRenderer: Boolean`, and
 * `onSessionUpdated: (Session, Frame) -> Unit`. Everything else that used to
 * depend on wrapper-specific callback names — horizontal-plane session
 * configuration, and tap hit-testing — is now done through the raw, stable
 * `com.google.ar.core.Session` / `Frame` API (see [ARSessionManager]) or
 * plain Compose pointer input, neither of which can drift out of sync with
 * a fast-moving third-party wrapper version again.
 */
@Composable
fun ArCameraPreview(
    arSessionManager: ARSessionManager,
    onTap: (x: Float, y: Float, frame: Frame?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Latest Frame seen by SceneView's per-tick callback, used to resolve
    // taps against the correct camera/plane state at the moment of the tap.
    var latestFrame by remember { mutableStateOf<Frame?>(null) }

    Box(modifier = modifier) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            planeRenderer = true,
            onSessionUpdated = { session, frame ->
                latestFrame = frame
                // bindSession() is idempotent — see ARSessionManager — so
                // calling it every tick is safe and removes any dependency
                // on a separate "session created" callback name that may
                // not exist (or may be named differently) in this version.
                arSessionManager.bindSession(session)
                arSessionManager.onFrameUpdated(session, frame)
            }
        )

        // Tap handling lives here, layered above ARSceneView with plain
        // Compose pointer input — the same technique already used for the
        // FROZEN-mode overlay in Step1AreaCaptureScreen — instead of an
        // uncertain SceneView touch-callback parameter name.
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
