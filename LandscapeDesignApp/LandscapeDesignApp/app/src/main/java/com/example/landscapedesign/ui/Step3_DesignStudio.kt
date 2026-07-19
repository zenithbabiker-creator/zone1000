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
import com.example.landscapedesign.ui.components.PlantDropperMenu
import com.example.landscapedesign.ui.components.ShapeToolbar
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step3DesignStudioScreen(viewModel: LandscapeViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ShapeToolbar(onUndo = { viewModel.undo() }, onRedo = { viewModel.redo() })
        Spacer(modifier = Modifier.height(16.dp))
        PlantDropperMenu(onPlantSelected = { type -> viewModel.addPlant(PlantNode(type = type)) })
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "النباتات المضافة:", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(state.plants) { plant ->
                Text(text = "- ${plant.type}")
            }
        }
    }
}
