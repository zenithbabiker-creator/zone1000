// ملف: app/src/main/java/com/example/landscapedesign/viewmodel/LandscapeViewModel.kt
package com.example.landscapedesign.viewmodel

import androidx.lifecycle.ViewModel
import com.example.landscapedesign.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LandscapeState(
    val gardenBoundary: List<Point3D> = emptyList(),
    val borders: List<BorderElement> = emptyList(),
    val plants: List<PlantNode> = emptyList(),
    val gardenAreaM2: Float = 0.0f,
    val soilThicknessCm: Int = 20,
    val soilVolumeM3: Float = 0.0f,
    val lawnType: String = "نجيلة طبيعية",
    val lawnAreaM2: Float = 0.0f,
    val generatedReportText: String = ""
)

class LandscapeViewModel : ViewModel() {
    private val _state = MutableStateFlow(LandscapeState())
    val state: StateFlow<LandscapeState> = _state.asStateFlow()

    fun addPlant(plant: PlantNode) { _state.update { it.copy(plants = it.plants + plant) } }
    fun updateBoundary(boundary: List<Point3D>) { _state.update { it.copy(gardenBoundary = boundary) } }
    fun updateLawnType(type: String) { _state.update { it.copy(lawnType = type) } }
    fun undo() {}
    fun redo() {}
}
