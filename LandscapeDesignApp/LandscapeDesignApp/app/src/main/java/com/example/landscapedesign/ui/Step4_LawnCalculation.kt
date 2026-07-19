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
        Text(text = "حسابات النجيلة", style = MaterialTheme.typography.headlineSmall)
        
        OutlinedTextField(
            value = state.lawnType,
            onValueChange = { viewModel.updateLawnType(it) },
            label = { Text("نوع النجيلة") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onNext) { Text("التالي") }
        Button(onClick = onBack) { Text("عودة") }
    }
}
