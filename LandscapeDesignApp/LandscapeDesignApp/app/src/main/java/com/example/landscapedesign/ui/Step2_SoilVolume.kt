package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step2SoilVolumeScreen(
    viewModel: LandscapeViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "حساب حجم التربة", style = MaterialTheme.typography.headlineSmall)
        
        Slider(
            value = state.soilThicknessCm.toFloat(),
            onValueChange = { viewModel.updateSoilThickness(it.toInt()) },
            valueRange = 10f..100f
        )
        Text(text = "سمك التربة: ${state.soilThicknessCm} سم")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onNext) { Text("التالي") }
        Button(onClick = onBack) { Text("عودة") }
    }
}
