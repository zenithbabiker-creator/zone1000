package com.example.landscapedesign.ar

import com.google.ar.core.Session
import com.google.ar.core.Frame
import com.google.ar.core.Anchor
import com.google.ar.core.Pose

class AnchorManager {
    private var googleSession: Session? = null
    private var isHuaweiEngine: Boolean = false

    fun bindSession(session: Session) {
        this.googleSession = session
        this.isHuaweiEngine = false
    }

    // دعم إضافي لجلسة هواوي AR Engine عند الحاجة
    fun bindHuaweiSession(isHuawei: Boolean) {
        this.isHuaweiEngine = isHuawei
    }

    fun onFrameUpdated(session: Session, frame: Frame) {
        this.googleSession = session
        // تنفيذ منطق معالجة الإطارات المشترك لـ ARCore و Huawei AR Engine
    }

    fun createAnchor(pose: Pose): Anchor? {
        return googleSession?.createAnchor(pose)
    }
}
