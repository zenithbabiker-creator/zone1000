package com.example.landscapedesign

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.landscapedesign.ui.Step1AreaCaptureScreen
import com.example.landscapedesign.ui.Step2SoilVolumeScreen
import com.example.landscapedesign.ui.Step3DesignStudioScreen
import com.example.landscapedesign.ui.Step4LawnScreen
import com.example.landscapedesign.ui.Step5ReportScreen
import com.example.landscapedesign.ui.theme.LandscapeDesignTheme
import com.example.landscapedesign.viewmodel.LandscapeViewModel

object Routes {
    const val STEP1 = "step1_area"
    const val STEP2 = "step2_soil"
    const val STEP3 = "step3_studio"
    const val STEP4 = "step4_lawn"
    const val STEP5 = "step5_report"
}

private const val TAG = "MainActivity"

/**
 * DEBUGGING NOTE (blank white screen on launch): this onCreate is
 * intentionally verbose with Log.i/Log.e checkpoints, and wraps every risky
 * initialization step (ViewModel creation, setContent's initial composition)
 * in try/catch so that ANY failure is (a) written loudly to Logcat under the
 * "MainActivity" tag and (b) shown to the user as a real error screen
 * instead of a silent blank one. To diagnose on-device, run:
 *
 *   adb logcat -s MainActivity:* LandscapeApp:* LandscapeApp-FATAL:* \
 *               ARSessionManager:* Step1AreaCapture:*
 *
 * then relaunch the app. The very first line should be
 * "LandscapeApp.onCreate() — process starting" (from LandscapeApp.kt); if
 * that line is missing, the process itself is being killed before Android
 * even runs Application.onCreate (check `adb logcat -b crash` and
 * `adb shell dumpsys package com.example.landscapedesign` for install/ABI
 * mismatches — this is common when an OEM's Play Store substitute, e.g.
 * Huawei AppGallery, installs the wrong CPU-architecture APK from a split).
 */
class MainActivity : ComponentActivity() {

    // Shared across every step/screen in the flow. Created lazily on first
    // access — the log line below confirms exactly when that happens.
    private val viewModel: LandscapeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate() start — savedInstanceState=${savedInstanceState != null}")
        super.onCreate(savedInstanceState)

        val vm = try {
            Log.i(TAG, "Resolving LandscapeViewModel…")
            viewModel.also { Log.i(TAG, "LandscapeViewModel resolved OK: $it") }
        } catch (t: Throwable) {
            Log.e(TAG, "FAILED to resolve LandscapeViewModel", t)
            null
        }

        try {
            Log.i(TAG, "Calling setContent() — starting initial Compose composition…")
            setContent {
                LandscapeDesignTheme {
                    if (vm == null) {
                        // ViewModel construction itself failed — show a real
                        // error instead of leaving the screen blank.
                        InitErrorScreen(
                            reason = "تعذر تهيئة نموذج البيانات (LandscapeViewModel). " +
                                "راجع Logcat بالوسم MainActivity للتفاصيل."
                        )
                    } else {
                        LandscapeNavHost(vm)
                    }
                }
            }
            Log.i(TAG, "setContent() returned — initial composition dispatched successfully")
        } catch (t: Throwable) {
            // Catches synchronous failures during the FIRST composition pass
            // (e.g. a theme resource that fails to resolve, or a Composable
            // that throws during its initial call). Recomposition-time
            // exceptions on later frames are still routed to the global
            // handler installed in LandscapeApp.kt.
            Log.e(TAG, "FATAL: setContent() / initial composition threw", t)
            setContent {
                InitErrorScreen(
                    reason = "حدث خطأ أثناء تشغيل الواجهة: ${t.javaClass.simpleName}: ${t.message}"
                )
            }
        }
    }
}

/** Minimal, dependency-free fallback screen shown when startup fails, so the user never sees a silent blank screen. */
@Composable
private fun InitErrorScreen(reason: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "حدث خطأ عند بدء التشغيل", style = MaterialTheme.typography.headlineSmall)
            Text(text = reason, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun LandscapeNavHost(viewModel: LandscapeViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.STEP1, modifier = Modifier) {
        composable(Routes.STEP1) {
            Step1AreaCaptureScreen(
                viewModel = viewModel,
                onConfirmed = { navController.navigate(Routes.STEP2) }
            )
        }
        composable(Routes.STEP2) {
            Step2SoilVolumeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.STEP3) }
            )
        }
        composable(Routes.STEP3) {
            Step3DesignStudioScreen(
                viewModel = viewModel,
                onNext = { navController.navigate(Routes.STEP4) }
            )
        }
        composable(Routes.STEP4) {
            Step4LawnScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.STEP5) }
            )
        }
        composable(Routes.STEP5) {
            Step5ReportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
