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
        Text(text = "الخطوة ٢: حساب كمية التراب", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "مساحة الحديقة: ${String.format("%.2f", state.gardenAreaM2)} م²")
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "اختر سماكة الردم: ${state.soilThicknessCm} سم")
        Slider(
            value = state.soilThicknessCm.toFloat(),
            onValueChange = { viewModel.updateSoilThickness(it.toInt()) },
            valueRange = 10f..100f,
            steps = 18
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "كمية التراب المطلوبة للردم: ${String.format("%.2f", state.soilVolumeM3)} متر مكعب",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) { Text("رجوع") }
            Button(onClick = onNext) { Text("التالي") }
        }
    }
}
