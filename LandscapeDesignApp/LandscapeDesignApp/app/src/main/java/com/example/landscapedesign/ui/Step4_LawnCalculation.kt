package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step4LawnCalculationScreen(
    viewModel: LandscapeViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "الخطوة ٤: النجيلة والمساحة الصافية", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.netLawnAreaM2.toString(),
            onValueChange = { 
                val value = it.toFloatOrNull() ?: 0f
                viewModel.updateNetLawnArea(value)
            },
            label = { Text("مساحة النجيلة الصافية (م²)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "كثافة الشتلات: ${state.lawnDensityPerM2} شتلة / م²")
        Text(
            text = "إجمالي شتلات النجيلة المطلوبة: ${state.totalLawnPlants} شتلة",
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
