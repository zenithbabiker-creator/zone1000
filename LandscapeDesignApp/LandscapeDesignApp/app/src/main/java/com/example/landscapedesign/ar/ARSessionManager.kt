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
 * Manages the ARCore & Huawei AREngine Session lifecycle and frame updates.
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
        if (frame.camera.trackingState == TrackingState.TRACKING) {
            viewModelScope.launch {
                // تنفيذ منطق التتبع الموحد لـ ARCore و Huawei AREngine هنا
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        arSession?.close()
        arSession = null
    }
}
