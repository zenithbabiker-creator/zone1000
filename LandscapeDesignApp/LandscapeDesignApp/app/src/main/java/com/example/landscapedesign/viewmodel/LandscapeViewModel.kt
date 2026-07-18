package com.example.landscapedesign.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Data class representing the state of the entire landscape design process.
 */
data class LandscapeState(
    val gardenAreaM2: Float = 0.0f,
    val soilThicknessCm: Int = 20,
    val lawnType: String = "نجيلة طبيعية",
    val lawnAreaM2: Float = 0.0f,
    // الحساب التلقائي لمنع الأخطاء المنطقية
    val soilVolumeM3: Float = 0.0f,
    val generatedReportText: String = ""
)

class LandscapeViewModel : ViewModel() {

    private val _state = MutableStateFlow(LandscapeState())
    val state: StateFlow<LandscapeState> = _state.asStateFlow()

    fun updateSoilThickness(thickness: Int) {
        _state.update { currentState ->
            val newVolume = currentState.gardenAreaM2 * (thickness / 100f)
            currentState.copy(
                soilThicknessCm = thickness,
                soilVolumeM3 = newVolume
            )
        }
    }

    fun updateLawnType(type: String) {
        _state.update { it.copy(lawnType = type) }
    }

    fun updateArea(area: Float) {
        _state.update { currentState ->
            val newVolume = area * (currentState.soilThicknessCm / 100f)
            currentState.copy(
                gardenAreaM2 = area,
                soilVolumeM3 = newVolume
            )
        }
    }

    fun generateReport() {
        _state.update { currentState ->
            val report = "المساحة: ${currentState.gardenAreaM2} م2\n" +
                         "السماكة: ${currentState.soilThicknessCm} سم\n" +
                         "الحجم المطلوب: ${"%.2f".format(currentState.soilVolumeM3)} م3\n" +
                         "نوع النجيلة: ${currentState.lawnType}"
            currentState.copy(generatedReportText = report)
        }
    }
}
