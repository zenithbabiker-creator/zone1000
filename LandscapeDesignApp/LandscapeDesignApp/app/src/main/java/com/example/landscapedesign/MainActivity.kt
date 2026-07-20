package com.example.landscapedesign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.ui.Step1AreaCaptureScreen
import com.example.landscapedesign.ui.theme.LandscapeDesignTheme
import com.example.landscapedesign.viewmodel.LandscapeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LandscapeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LandscapeDesignTheme {
                val navController = rememberNavController()
                val arSessionManager = remember { ARSessionManager() }
                
                NavHost(navController = navController, startDestination = "step1") {
                    composable("step1") {
                        Step1AreaCaptureScreen(
                            viewModel = viewModel,
                            arSessionManager = arSessionManager,
                            onNext = { /* انتقال */ }
                        )
                    }
                }
            }
        }
    }
}
```[cite: 6]
