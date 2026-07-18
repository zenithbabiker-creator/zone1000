package com.example.landscapedesign.ar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Manages the ARCore Session lifecycle and frame updates.
 * This implementation ensures that AR operations only trigger when the
 * tracking state is active, preventing rendering freezes and crashes.
 */
class ARSessionManager : ViewModel() {

    private var arSession: Session? = null

    private val _isSessionReady = MutableStateFlow(false)
    val isSessionReady: StateFlow<Boolean> = _isSessionReady

    fun bindSession(session: Session) {
        if (arSession == null) {
            arSession = session
            _isSessionReady.value = true
        }
    }

    fun onFrameUpdated(session: Session, frame: Frame) {
        // Logically safe: Only process frames if the camera is tracking
        // This prevents the "White Screen" if the camera is covered or not initialized.
        if (frame.camera.trackingState == TrackingState.TRACKING) {
            viewModelScope.launch {
                // Perform your AR logic here (e.g., hit-testing, coordinate mapping)
                // This block is now safe because we verified TrackingState
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources to prevent memory leaks that crash the app
        arSession?.close()
        arSession = null
    }
}
