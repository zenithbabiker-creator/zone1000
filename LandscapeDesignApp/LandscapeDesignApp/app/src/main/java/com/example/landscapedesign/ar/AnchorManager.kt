// ملف: app/src/main/java/com/example/landscapedesign/ar/AnchorManager.kt
package com.example.landscapedesign.ar

import com.google.ar.core.Session
import com.google.ar.core.Frame
import com.google.ar.core.Anchor

class AnchorManager {
    private var session: Session? = null

    fun bindSession(session: Session) {
        this.session = session
    }

    fun onFrameUpdated(session: Session, frame: Frame) {
        this.session = session
        // تنفيذ منطق معالجة الإطارات هنا
    }

    fun createAnchor(pose: com.google.ar.core.Pose): Anchor? {
        return session?.createAnchor(pose)
    }
}
