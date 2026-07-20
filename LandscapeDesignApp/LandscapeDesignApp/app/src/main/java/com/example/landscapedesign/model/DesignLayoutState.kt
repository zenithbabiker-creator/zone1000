package com.example.landscapedesign.model

/**
 * Single source of truth for the entire design flow (Steps 1 through 5).[cite: 9]
 * Immutable data class updated via ViewModel copy() calls so Compose can[cite: 9]
 * reactively recompute every dependent screen (summary cards, report, etc.)[cite: 9]
 * whenever any parameter changes.[cite: 9]
 */
data class DesignLayoutState(
    // Step 1: base garden boundary + area[cite: 9]
    val gardenBoundary: List<Point3D> = emptyList(),[cite: 9]
    val gardenAreaM2: Float = 0f,[cite: 9]

    // Step 2: soil[cite: 9]
    val soilThicknessCm: Int = 20,[cite: 9]
    val soilVolumeM3: Float = 0f,[cite: 9]

    // Step 3: design studio[cite: 9]
    val shapes: List<ShapeElement> = emptyList(),[cite: 9]
    val plants: List<PlantNode> = emptyList(),[cite: 9]
    val borders: List<BorderElement> = listOf([cite: 9]
        BorderElement(tier = BorderTier.LARGE),[cite: 9]
        BorderElement(tier = BorderTier.MEDIUM),[cite: 9]
        BorderElement(tier = BorderTier.SMALL)[cite: 9]
    ),[cite: 9]

    // Step 4: lawn[cite: 9]
    val lawnDensityPerM2: Int = 25,[cite: 9]
    val netLawnAreaM2: Float = 0f,[cite: 9]
    val totalLawnPlants: Int = 0[cite: 9]
) {
    val royalPalmCount: Int get() = plants.count { it.type == PlantType.ROYAL_PALM }[cite: 9]
    val noThornCount: Int get() = plants.count { it.type == PlantType.NO_THORN }[cite: 9]
}
```[cite: 9]
