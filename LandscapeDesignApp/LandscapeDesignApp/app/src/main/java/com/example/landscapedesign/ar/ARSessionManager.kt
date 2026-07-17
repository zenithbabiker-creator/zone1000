package com.example.landscapedesign.ar

import android.app.Activity
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Whether the AR interaction is currently live-tracking or reviewing a frozen photo. */
enum class ArMode { LIVE, FROZEN }

data class ArAvailability(
    val supported: Boolean,
    val reasonIfUnsupported: String? = null
)

/**
 * Holds the shared ARCore spatial state for the whole design flow (Steps 1 & 3
 * both read from the same [Session] so anchors / plane data stay consistent).
 *
 * IMPORTANT — session ownership: the ARCore [Session] itself is now created,
 * installed, and lifecycle-managed by SceneView's `ARSceneView` composable
 * (see `ui/ArCameraPreview.kt`), which also renders the camera passthrough
 * background automatically via Filament — no manual GLES/shader code needed.
 * This class no longer creates or resumes/pauses the Session; instead
 * `ARSceneView`'s `onSessionUpdated` callback calls [bindSession] (once,
 * idempotently) and [onFrameUpdated] (every tick) here so the rest of the
 * app (Step 1's dual-mode capture, Step 3's design studio) keeps working
 * against a single consistent source of spatial truth.
 *
 * Responsibilities that remain here:
 *  - Runtime ARCore availability check (pre-flight, before showing ArSceneView).
 *  - Tracking the latest detected horizontal [Plane].
 *  - Capturing ("freezing") the last valid [Frame] plus its View/Projection
 *    matrices so Step 1's Frozen Photo mode (and Step 3) can keep doing
 *    ray-plane math without a live camera feed.
 */
class ARSessionManager(private val activity: Activity) {

    var session: Session? = null
        private set

    private val _mode = MutableStateFlow(ArMode.LIVE)
    val mode: StateFlow<ArMode> = _mode

    private val _planeTracked = MutableStateFlow(false)
    val planeTracked: StateFlow<Boolean> = _planeTracked

    private val _trackedPlane = MutableStateFlow<Plane?>(null)
    val trackedPlane: StateFlow<Plane?> = _trackedPlane

    /** Frozen data captured at the moment the user hits "Capture/Freeze". */
    var frozenViewMatrix: FloatArray? = null
        private set
    var frozenProjectionMatrix: FloatArray? = null
        private set
    var frozenFrame: Frame? = null
        private set

    /** Most recent live frame; kept so we always have a valid frame to freeze. */
    private var lastValidFrame: Frame? = null

    fun checkAvailability(): ArAvailability {
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(activity)
            Log.i(TAG, "ArCoreApk.checkAvailability() -> $availability")
            if (availability.isSupported) {
                ArAvailability(true)
            } else {
                ArAvailability(false, "ARCore not supported: $availability")
            }
        } catch (e: Throwable) {
            // On devices with no Google Play Services at all (e.g. many Huawei
            // phones shipped after 2019, including the Pura series, which run
            // HMS instead of GMS), the ARCore client library's internal calls
            // to Play-Services classes can throw NoClassDefFoundError /
            // ClassNotFoundException here rather than returning a clean
            // UNSUPPORTED_DEVICE_NOT_CAPABLE result. Catching Throwable (not
            // just Exception) is intentional: NoClassDefFoundError is an
            // Error, not an Exception, and would otherwise crash the app
            // before the UI ever renders, or in some ROMs terminate the
            // process silently (blank screen, nothing "crashed").
            Log.e(TAG, "ArCoreApk.checkAvailability() threw — device likely has no Google Play Services (common on non-GMS Huawei devices)", e)
            ArAvailability(false, "ARCore availability check failed: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    /**
     * Called every tick by [ui.ArCameraPreview] via `ARSceneView`'s
     * `onSessionUpdated` callback. Idempotent by design (checked via
     * reference equality) so it's safe to call on every frame instead of
     * depending on a separate, wrapper-specific "session created" callback
     * name that may not exist — or may be spelled differently — across
     * SceneView versions.
     *
     * Session configuration (horizontal-plane finding) is done here too,
     * once, using only `com.google.ar.core.Session`/`Config` — the stable,
     * version-independent ARCore SDK surface — rather than any SceneView
     * wrapper callback.
     */
    fun bindSession(session: Session) {
        if (this.session === session) return
        this.session = session
        try {
            val config = session.config
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            config.focusMode = Config.FocusMode.AUTO
            session.configure(config)
            Log.i(TAG, "ARCore session bound and configured (HORIZONTAL plane finding)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure newly bound ARCore session", e)
        }
    }

    fun clearSession() {
        session = null
    }

    /**
     * Called every frame by [ui.ArCameraPreview] via `ArSceneView.onSessionUpdated`.
     * Updates the latest [Frame] and scans detected trackables for a tracked
     * horizontal surface, and caches the latest frame so Capture() always has
     * something valid to freeze. While in FROZEN mode this is a no-op so the
     * cached frozen frame/matrices are never overwritten by live updates.
     */
    fun onFrameUpdated(session: Session, frame: Frame) {
        if (_mode.value == ArMode.FROZEN) return
        lastValidFrame = frame

        val plane = session.getAllTrackables(Plane::class.java)
            .firstOrNull {
                it.trackingState == TrackingState.TRACKING &&
                    it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
            }
        _trackedPlane.value = plane
        _planeTracked.value = plane != null
    }

    /**
     * Freezes the current camera frame: caches the last valid [Frame] plus its
     * View and Projection matrices, and switches mode to FROZEN. The ARCore
     * session itself (owned by ArSceneView) keeps running underneath — NOT
     * paused/destroyed — so its spatial map / anchors remain valid for later
     * ray-plane projection.
     */
    fun captureFrame(viewportWidth: Int, viewportHeight: Int) {
        val frame = lastValidFrame ?: return
        val camera = frame.camera

        val viewMatrix = FloatArray(16)
        val projMatrix = FloatArray(16)
        camera.getViewMatrix(viewMatrix, 0)
        camera.getProjectionMatrix(projMatrix, 0, NEAR_CLIP, FAR_CLIP)

        frozenFrame = frame
        frozenViewMatrix = viewMatrix
        frozenProjectionMatrix = projMatrix
        _mode.value = ArMode.FROZEN
    }

    /** Returns to LIVE tracking mode and clears frozen frame data. */
    fun goLive() {
        _mode.value = ArMode.LIVE
        frozenFrame = null
        frozenViewMatrix = null
        frozenProjectionMatrix = null
    }

    /** Convenience: combined View*Projection matrix for the frozen frame, if any. */
    fun frozenViewProjectionMatrix(): FloatArray? {
        val v = frozenViewMatrix ?: return null
        val p = frozenProjectionMatrix ?: return null
        val vp = FloatArray(16)
        Matrix.multiplyMM(vp, 0, p, 0, v, 0)
        return vp
    }

    fun createAnchor(pose: com.google.ar.core.Pose): Anchor? {
        return try {
            session?.createAnchor(pose)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create anchor", e)
            null
        }
    }

    companion object {
        private const val TAG = "ARSessionManager"
        private const val NEAR_CLIP = 0.1f
        private const val FAR_CLIP = 100f
    }
}
