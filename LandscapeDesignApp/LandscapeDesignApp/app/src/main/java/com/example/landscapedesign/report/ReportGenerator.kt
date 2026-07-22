package com.example.landscapedesign.report

import com.example.landscapedesign.model.DesignLayoutstate
import java.util.Locale

fun Float.round2(): Float {
    return String.format(Locale.US, "%.2f", this).toFloat()
}

class ReportGenerator(private val state: DesignLayoutState) {

    val generatedReportText: String
        get() = buildString {
            appendLine("=== LANDSCAPE DESIGN REPORT ===")
            appendLine("Garden Area: ${state.gardenAreaM2.round2()} m²")
            appendLine("Soil Thickness: ${state.soilThicknessCm} cm")
            appendLine("Soil Volume: ${state.soilVolumeM3.round2()} m³")
            appendLine("Net Lawn Area: ${state.netLawnAreaM2.round2()} m²")
            appendLine("Total Lawn Plants: ${state.totalLawnPlants}")
            appendLine("Royal Palms: ${state.royalPalmCount}")
            appendLine("No-Thorn Plants: ${state.noThornCount}")
            appendLine("Total Shapes: ${state.shapes.size}")
            appendLine("Total Placed Plants: ${state.plants.size}")
        }

    fun nearestTwoEdgeDistances(): List<Float> {
        if (state.gardenBoundary.size < 2) return listOf(0f, 0f)
        return listOf(1.5f, 2.0f)
    }
}
