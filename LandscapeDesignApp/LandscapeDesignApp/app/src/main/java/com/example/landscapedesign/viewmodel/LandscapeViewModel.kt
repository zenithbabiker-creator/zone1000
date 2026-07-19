package com.example.landscapedesign.viewmodel

import androidx.lifecycle.ViewModel
import com.example.landscapedesign.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * LandscapeState تمثل الحالة الكاملة للتطبيق
 */
data class LandscapeState(
    val gardenBoundary: List<Point3D> = emptyList(),
    val borders: List<BorderElement> = emptyList(),
    val plants: List<PlantNode> = emptyList(),
    val gardenAreaM2: Float = 0.0f,
    val soilThicknessCm: Int = 20,
    val soilVolumeM3: Float = 0.0f
)

class LandscapeViewModel : ViewModel() {

    private val _state = MutableStateFlow(LandscapeState())
    val state: StateFlow<LandscapeState> = _state.asStateFlow()

    // إضافة نبات إلى الحديقة
    fun addPlant(plant: PlantNode) {
        _state.update { currentState ->
            currentState.copy(plants = currentState.plants + plant)
        }
    }

    // إضافة أو تحديث عنصر حدودي
    fun updateBorder(border: BorderElement) {
        _state.update { currentState ->
            currentState.copy(borders = currentState.borders + border)
        }
    }

    // تحديث حدود الحديقة
    fun updateBoundary(newBoundary: List<Point3D>) {
        _state.update { currentState ->
            currentState.copy(gardenBoundary = newBoundary)
        }
    }

    // دوال التراجع والإعادة (يمكنك إضافة منطق Stack هنا لاحقاً)
    fun undo() {
        // تنفيذ منطق التراجع
    }

    fun redo() {
        // تنفيذ منطق الإعادة
    }

    // تحديث مساحة الحديقة
    fun updateArea(area: Float) {
        _state.update { currentState ->
            val newVolume = area * (currentState.soilThicknessCm / 100f)
            currentState.copy(
                gardenAreaM2 = area,
                soilVolumeM3 = newVolume
            )
        }
    }

    // تحديث سمك التربة
    fun updateSoilThickness(thickness: Int) {
        _state.update { currentState ->
            val newVolume = currentState.gardenAreaM2 * (thickness / 100f)
            currentState.copy(
                soilThicknessCm = thickness,
                soilVolumeM3 = newVolume
            )
        }
    }
}
