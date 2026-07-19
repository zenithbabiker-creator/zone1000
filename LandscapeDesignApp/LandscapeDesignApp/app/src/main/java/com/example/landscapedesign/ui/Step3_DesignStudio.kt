package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.landscapedesign.R
import com.example.landscapedesign.model.PlantNode
import com.example.landscapedesign.ui.components.PlantDropperMenu
import com.example.landscapedesign.ui.components.ShapeToolbar
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step3_DesignStudio(
    viewModel: LandscapeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        // 1. شريط الأدوات (تم إصلاح خطأ Unresolved reference)
        ShapeToolbar(
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. قائمة اختيار النباتات
        PlantDropperMenu(
            onPlantSelected = { selectedPlant ->
                // تحويل المدخلات إلى PlantNode (تم إصلاح خطأ Unresolved reference)
                viewModel.addPlant(PlantNode(type = selectedPlant))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. عرض منطقة التصميم والقوائم
        Text(
            text = "تصميم الحديقة",
            style = MaterialTheme.typography.headlineSmall
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // عرض الحدود (Boundary)
            item {
                Text("الحدود المحددة: ${state.gardenBoundary.size} نقطة")
            }

            // عرض الحدود المسجلة (تم إصلاح خطأ Ambiguity بوضع النوع الصريح)
            items(state.borders) { border ->
                Text("حد: ${border.plantName ?: "غير محدد"}")
            }

            // عرض النباتات المضافة (تم إصلاح خطأ Ambiguity بوضع النوع الصريح)
            items(state.plants) { plant ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "نبات: ${plant.type}"
                    )
                }
            }
        }
    }
}
