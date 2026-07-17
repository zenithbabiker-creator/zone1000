package com.example.landscapedesign.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy

/**
 * Asynchronously reads the pixels currently displayed within a given
 * on-screen region (in window coordinates) into a [Bitmap]. Used by Step 1
 * to "freeze" the live AR camera feed into a still photo when the user
 * presses Capture.
 *
 * Deliberately takes a plain [Rect] + [Activity] rather than a specific
 * [android.view.View] reference: SceneView's `ARSceneView` (2.x) is a pure
 * Compose function, not an instantiable View subclass, so there is no
 * dedicated View to hold onto. [PixelCopy] only needs the target [Rect] in
 * window coordinates and the hosting [android.view.Window] — both of which
 * are stable Android platform APIs unaffected by any third-party library
 * version change.
 */
object PixelCopyHelper {

    fun copyFromWindow(activity: Activity, boundsInWindow: Rect, onResult: (Bitmap?) -> Unit) {
        val width = boundsInWindow.width()
        val height = boundsInWindow.height()
        if (width <= 0 || height <= 0) {
            onResult(null)
            return
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(
            activity.window,
            boundsInWindow,
            bitmap,
            { copyResult -> onResult(if (copyResult == PixelCopy.SUCCESS) bitmap else null) },
            Handler(Looper.getMainLooper())
        )
    }
}
