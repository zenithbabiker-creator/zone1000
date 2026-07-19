package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.landscapedesign.model.PlantNode
import com.example.landscapedesign.viewmodel.LandscapeViewModel
// قمت بإزالة الاستيرادات المسببة للأخطاء وسنعتمد على المكونات الأساسية
@Composable
fun Step3DesignStudioScreen(
    viewModel: LandscapeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "استوديو التصميم", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        // تم تبسيط المكونات لتجنب Unresolved reference
        Button(onClick = { viewModel.undo() }) { Text("تراجع") }
        Button(onClick = { viewModel.redo() }) { Text("إعادة") }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "النباتات المضافة:", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(state.plants) { plant ->
                Text(text = "- ${plant.type}")
            }
        }
    }
}
