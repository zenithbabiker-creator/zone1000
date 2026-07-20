package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step1AreaCaptureScreen(
    viewModel: LandscapeViewModel,
    arSessionManager: ARSessionManager,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ArCameraPreview(
            arSessionManager = arSessionManager,
            onTap = { x, y, frame ->
                viewModel.addBoundaryPoint(x, y)
            }
        )

        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مساحة الحديقة: ${String.format("%.2f", state.gardenAreaM2)} م²",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { viewModel.clearBoundaryPoints() }) {
                            Text("مسح النقاط")
                        }
                        Button(
                            onClick = onNext,
                            enabled = state.gardenBoundary.isNotEmpty()
                        ) {
                            Text("تأكيد والمتابعة")
                        }
                    }
                }
            }
        }
    }
}
