package com.example.landscapedesign.ar

import com.google.ar.core.Session
import com.google.ar.core.Frame
import com.google.ar.core.Anchor
import com.google.ar.core.Pose

/**
 * Manages anchoring for both ARCore and Huawei AR Engine.
 */
class AnchorManager {
    private var arSession: Session? = null
    private var isHuaweiEngine: Boolean = false

    fun bindSession(session: Session) {
        this.arSession = session
        this.isHuaweiEngine = false
    }

    fun bindHuaweiEngine(active: Boolean) {
        this.isHuaweiEngine = active
    }

    fun onFrameUpdated(session: Session, frame: Frame) {
        this.arSession = session
    }

    fun createAnchor(pose: Pose): Anchor? {
        return arSession?.createAnchor(pose)
    }
}
```[cite: 1]

أخبرني فور جاهزيتك لننتقل إلى الملف الثاني.
