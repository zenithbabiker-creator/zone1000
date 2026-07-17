package com.example.landscapedesign

import android.app.Application
import android.util.Log

/**
 * Custom [Application] class — also serves as the single place to install a
 * global uncaught-exception logger.
 *
 * DEBUGGING NOTE (blank-screen / Huawei report): on some OEM ROMs (including
 * EMUI/HarmonyOS on non-GMS Huawei devices such as the Pura 70), an
 * unhandled crash during app startup can terminate the process WITHOUT
 * showing the standard "App has stopped" system dialog and WITHOUT writing
 * a clear crash trace that's easy to find — the user just sees a blank or
 * flashing white screen and the app silently restarts or sits idle.
 *
 * Installing our own [Thread.UncaughtExceptionHandler] here guarantees the
 * full stack trace is written to Logcat under a single greppable tag
 * ("LandscapeApp-FATAL") *before* the default handler runs, so running:
 *   adb logcat -s LandscapeApp-FATAL:E LandscapeApp:I Step1AreaCapture:E ARSessionManager:E
 * will show exactly what failed and where, even on devices where the
 * on-screen crash UI is suppressed by the manufacturer.
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
            // Preserve default behavior (e.g. Play Vitals/Crashlytics reporting,
            // and the normal OS crash-restart flow) after logging.
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "LandscapeApp"
    }
}
