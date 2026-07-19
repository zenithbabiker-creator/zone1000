package com.example.landscapedesign.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step1AreaCaptureScreen(
    viewModel: LandscapeViewModel,
    arSessionManager: ARSessionManager,
    onNext: () -> Unit
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasCameraPermission) {
              ArCameraPreview(
    arSessionManager = arSessionManager,
    onTap = { x, y, frame -> /* منطق الـ tap الخاص بك */ },
    modifier = Modifier.fillMaxSize()
)
            }
        }
    }
}
