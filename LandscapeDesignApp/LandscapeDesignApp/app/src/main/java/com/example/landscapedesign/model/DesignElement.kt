package com.example.landscapedesign.model

data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float = 0f
)

enum class PlantType {
    ROYAL_PALM,
    NO_THORN
}

data class PlantNode(
    val id: String,
    val type: PlantType,
    val position: Point3D
)

enum class BorderTier {
    LARGE,
    MEDIUM,
    SMALL
}

data class BorderElement(
    val tier: BorderTier,
    val lengthMeters: Float = 0f
)

data class ShapeElement(
    val id: String,
    val name: String,
    val points: List<Point3D> = emptyList()
)

/**
 * Single source of truth for the entire design flow (Steps 1 through 5).
 * Immutable data class updated via ViewModel copy() calls so Compose can
 * reactively recompute every dependent screen (summary cards, report, etc.)
 * whenever any parameter changes.
 */
data class DesignLayoutState(
    // Step 1: base garden boundary + area
    val gardenBoundary: List<Point3D> = emptyList(),
    val gardenAreaM2: Float = 0f,

    // Step 2: soil
    val soilThicknessCm: Int = 20,
    val soilVolumeM3: Float = 0f,

    // Step 3: design studio
    val shapes: List<ShapeElement> = emptyList(),
    val plants: List<PlantNode> = emptyList(),
    val borders: List<BorderElement> = listOf(
        BorderElement(tier = BorderTier.LARGE),
        BorderElement(tier = BorderTier.MEDIUM),
        BorderElement(tier = BorderTier.SMALL)
    ),

    // Step 4: lawn
    val lawnDensityPerM2: Int = 25,
    val netLawnAreaM2: Float = 0f,
    val totalLawnPlants: Int = 0
) {
    val royalPalmCount: Int get() = plants.count { it.type == PlantType.ROYAL_PALM }
    val noThornCount: Int get() = plants.count { it.type == PlantType.NO_THORN }
}
