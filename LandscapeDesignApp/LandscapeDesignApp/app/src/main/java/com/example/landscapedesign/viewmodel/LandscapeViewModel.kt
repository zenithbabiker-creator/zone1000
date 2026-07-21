package com.example.landscapedesign.viewmodel

import androidx.lifecycle.ViewModel
import com.example.landscapedesign.model.DesignLayoutState
import com.example.landscapedesign.model.PlantNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LandscapeViewModel : ViewModel() {
    private val _state = MutableStateFlow(DesignLayoutState())
    val state: StateFlow<DesignLayoutState> = _state.asStateFlow()

    private val history = mutableListOf<DesignLayoutState>()
    private var historyIndex = -1

    init {
        saveStateToHistory(_state.value)
    }

    private fun saveStateToHistory(newState: DesignLayoutState) {
        if (historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        history.add(newState)
        historyIndex = history.size - 1
    }

    fun updateSoilThickness(thickness: Int) {
        _state.update { current ->
            val volume = (current.gardenAreaM2 * thickness) / 100f
            val updated = current.copy(soilThicknessCm = thickness, soilVolumeM3 = volume)
            saveStateToHistory(updated)
            updated
        }
    }

    fun addPlant(plant: PlantNode) {
        _state.update { current ->
            val newPlants = current.plants + plant
            val updated = current.copy(plants = newPlants)
            saveStateToHistory(updated)
            updated
        }
    }

    fun undo() {
        if (historyIndex > 0) {
            historyIndex--
            _state.value = history[historyIndex]
        }
    }

    fun redo() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            _state.value = history[historyIndex]
        }
    }
}
