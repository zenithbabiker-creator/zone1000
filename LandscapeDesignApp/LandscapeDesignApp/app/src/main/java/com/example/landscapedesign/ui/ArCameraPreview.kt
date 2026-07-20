package com.example.landscapedesign.ar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.sceneview.ar.ARScene
import com.google.ar.core.Config
import com.google.ar.core.Frame

@Composable
fun ArCameraPreview(
    modifier: Modifier = Modifier,
    arSessionManager: ARSessionManager,
    onTap: (Float, Float, Frame?) -> Unit = { _, _, _ -> }
) {
    if (arSessionManager.isArSupported) {
        ARScene(
            modifier = modifier,
            // إعدادات جلسة الواقع المعزز لتتبع الأسطح الأفقية ورسم الحدود بدقة
            sessionConfiguration = { session, config ->
                config.depthMode = Config.DepthMode.AUTOMATIC
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            },
            onSessionUpdated = { session, frame ->
                // يمكن تحديث الإطارات أو تتبع الحركة هنا عند الحاجة
            },
            onTap = { hitResult, plane, motionEvent ->
                // التقاط الإحداثيات عند النقر على الشاشة لتحديد النقاط ومساحة الحديقة
                val x = motionEvent.x
                val y = motionEvent.y
                onTap(x, y, null)
            }
        )
    }
}
