package com.example.landscapedesign

import android.app.Application
import android.util.Log

/**
 * Custom [Application] class — also serves as the single place to install a
 * global uncaught-exception logger.
 *
 * DEBUGGING NOTE (blank-screen / Huawei report): on some OEM ROMs (including
 * EMUI/HarmonyOS on non-GMS Huawei devices), an
 * unhandled crash during app startup can terminate the process WITHOUT
 * showing the standard "App has stopped" system dialog and WITHOUT writing
 * a clear crash trace that's easy to find — the user just sees a blank or
 * flashing white screen and the app silently restarts or sits idle.
 *
 * Installing our own [Thread.UncaughtExceptionHandler] here guarantees the
 * full stack trace is written to Logcat under a single greppable tag
 * ("LandscapeApp-FATAL") *before* the default handler runs.
 */
class LandscapeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "LandscapeApp.onCreate() — process starting")
        installGlobalCrashLogger()
    }

    private fun installGlobalCrashLogger() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(
                "LandscapeApp-FATAL",
                "Uncaught exception on thread '${thread.name}'. " +
                    "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}, " +
                    "Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})",
                throwable
            )
            // الحفاظ على السلوك الافتراضي بعد تسجيل الخطأ
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "LandscapeApp"
    }
}
