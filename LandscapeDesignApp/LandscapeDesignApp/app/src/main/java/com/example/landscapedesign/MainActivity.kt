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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.ui.Step1AreaCaptureScreen
import com.example.landscapedesign.ui.Step2SoilVolumeScreen
import com.example.landscapedesign.ui.Step3DesignStudioScreen
import com.example.landscapedesign.ui.Step4LawnCalculationScreen
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

class MainActivity : ComponentActivity() {

    private val viewModel: LandscapeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LandscapeDesignTheme {
                val arSessionManager = remember { ARSessionManager() }
                LandscapeNavHost(viewModel, arSessionManager)
            }
        }
    }
}

@Composable
fun LandscapeNavHost(viewModel: LandscapeViewModel, arSessionManager: ARSessionManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.STEP1) {
        composable(Routes.STEP1) {
            Step1AreaCaptureScreen(
                viewModel = viewModel,
                arSessionManager = arSessionManager,
                onNext = { navController.navigate(Routes.STEP2) }
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
            Step3DesignStudioScreen(viewModel = viewModel)
        }
        composable(Routes.STEP4) {
            Step4LawnCalculationScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.STEP5) }
            )
        }
        composable(Routes.STEP5) {
            Step5ReportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onFinish = { }
            )
        }
    }
}
