package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.landscapedesign.model.Plant
import com.example.landscapedesign.viewmodel.LandscapeViewModel

/**
 * دمج متكامل لخطة التصميم (Plan) داخل شاشة استوديو التصميم (Step 3)
 */
data class DesignPlan(
    val planId: String,
    val title: String,
    val defaultPlants: List<Plant>
)

@Composable
fun Step3DesignStudioScreen(
    viewModel: LandscapeViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    // نموذج الخطة المدمج داخل استوديو التصميم
    val currentPlan = remember {
        DesignPlan(
            planId = "PLAN-001",
            title = "مخطط الحديقة الرئيسي",
            defaultPlants = state.plants
        )
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "الخطوة ٣: استوديو التصميم - ${currentPlan.title}", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.undo() }, modifier = Modifier.weight(1f)) { Text("تراجع") }
            Button(onClick = { viewModel.redo() }, modifier = Modifier.weight(1f)) { Text("إعادة") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "النباتات المضافة: ${state.royalPalmCount} رويال بالم، ${state.noThornCount} لاشوكة",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(state.plants) { plant ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "نبتة: ${plant.type} عند الإحداثيات (${plant.x}, ${plant.y})",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) { Text("رجوع") }
            Button(onClick = onNext) { Text("التالي") }
        }
    }
}
