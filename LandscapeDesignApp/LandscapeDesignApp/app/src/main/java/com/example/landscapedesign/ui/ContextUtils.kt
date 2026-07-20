package com.example.landscapedesign.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Safely walks the [ContextWrapper] chain to find the real hosting [Activity].
 *
 * `LocalContext.current as? Activity` is a common Compose pitfall: on many
 * OEM ROMs / configurations, the Context handed to a Composable is a
 * [android.view.ContextThemeWrapper] (or another ContextWrapper) around the
 * Activity rather than the Activity instance itself, so a direct cast
 * silently returns null. If calling code then does `?: return`, the
 * composable exits with NO crash and NO log — producing a persistent blank
 * screen that is very hard to diagnose. Always prefer this unwrapping
 * helper over a direct cast when a Composable needs the Activity (e.g. to
 * construct an [com.example.landscapedesign.ar.ARSessionManager]).
 */
fun Context.findActivityOrNull(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return ctx as? Activity
}
