package com.example.landscapedesign.model

import java.util.UUID

/** A single real-world 3D point captured either live or projected from a frozen frame.[cite: 8] */
data class Point3D(
    val x: Float, // meters, world space[cite: 8]
    val y: Float, // meters, world space (vertical - ignored in area math)[cite: 8]
    val z: Float  // meters, world space[cite: 8]
)

/** A 2D screen-space point, used for drawing overlays on the canvas / frozen photo.[cite: 8] */
data class ScreenPoint(val x: Float, val y: Float)

/** Correlated pair: where a world anchor projects to on screen, and its real-world position.[cite: 8] */
data class AnchorPoint(
    val id: String = UUID.randomUUID().toString(),
    val world: Point3D,
    val screen: ScreenPoint
)

enum class ShapeType { ARC, CIRCLE, POLYGON }

enum class PlantType(val labelRes: String) {
    NO_THORN("plant_no_thorn"),
    ROYAL_PALM("plant_royal_palm"),
    DURANTA("plant_duranta"),
    CUSTOM("custom")
}

/** A single planted node (major tree or hedge unit) placed on the canvas.[cite: 8] */
data class PlantNode(
    val id: String = UUID.randomUUID().toString(),
    val type: PlantType,
    val customName: String? = null,
    val world: Point3D,
    var screen: ScreenPoint
)

/** A geometric shape (arc/circle/polygon) drawn around a feature, e.g. a tree hedge ring.[cite: 8] */
data class ShapeElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType,
    val centerWorld: Point3D? = null,     // for circle/arc[cite: 8]
    val radiusMeters: Float? = null,       // for circle/arc[cite: 8]
    val startAngleDeg: Float? = null,      // for arc[cite: 8]
    val sweepAngleDeg: Float? = null,      // for arc[cite: 8]
    val polygonVertices: List<Point3D> = emptyList(), // for custom polygon[cite: 8]
    val attachedTreeId: String? = null,    // major tree this shape surrounds, if any[cite: 8]
    val hedgePlantName: String? = null,    // e.g. "دورنتا" - null = no hedge fill[cite: 8]
    val hedgeDensityPerMeter: Float? = null,
    val hedgePlantCount: Int? = null,
    val eraserGapsMeters: Float = 0f       // total length removed by eraser tool[cite: 8]
) {
    /** Real-world perimeter length in meters, accounting for eraser cuts.[cite: 8] */
    fun perimeterMeters(): Float {
        val raw = when (type) {
            ShapeType.CIRCLE -> {
                val r = radiusMeters ?: 0f
                (2f * Math.PI * r).toFloat()
            }
            ShapeType.ARC -> {
                val r = radiusMeters ?: 0f
                val sweep = sweepAngleDeg ?: 0f
                ((sweep / 360f) * 2f * Math.PI * r).toFloat()
            }
            ShapeType.POLYGON -> {
                var total = 0f
                val v = polygonVertices
                for (i in v.indices) {
                    val a = v[i]
                    val b = v[(i + 1) % v.size]
                    val dx = b.x - a.x
                    val dz = b.z - a.z
                    total += kotlin.math.sqrt(dx * dx + dz * dz)
                }
                total
            }
        }
        return (raw - eraserGapsMeters).coerceAtLeast(0f)
    }

    /** Enclosed area of the shape in square meters (used for "no-lawn" subtraction).[cite: 8] */
    fun enclosedAreaMeters2(): Float {
        return when (type) {
            ShapeType.CIRCLE -> {
                val r = radiusMeters ?: 0f
                (Math.PI * r * r).toFloat()
            }
            ShapeType.ARC -> {
                val r = radiusMeters ?: 0f
                val sweep = sweepAngleDeg ?: 0f
                ((sweep / 360f) * Math.PI * r * r).toFloat()
            }
            ShapeType.POLYGON -> {
                com.example.landscapedesign.geometry.GeometryUtils.shoelaceArea(polygonVertices)
            }
        }
    }
}

enum class BorderTier(val thicknessCm: Int) {
    LARGE(30),
    MEDIUM(20),
    SMALL(10)
}

/** One of the 3 fixed border tiers, drawn as a path/ribbon around part of the garden.[cite: 8] */
data class BorderElement(
    val id: String = UUID.randomUUID().toString(),
    val tier: BorderTier,
    val pathVertices: List<Point3D> = emptyList(), // polyline the user drew[cite: 8]
    val plantName: String? = null,                  // null/blank => structural border[cite: 8]
    val densityPerMeter: Float? = null,              // plants per linear meter[cite: 8]
    val eraserGapsMeters: Float = 0f,                // total length cut out via eraser (doors)[cite: 8]
    val openingsCount: Int = 0
) {
    val isStructural: Boolean get() = plantName.isNullOrBlank()

    fun rawLengthMeters(): Float {
        var total = 0f
        for (i in 0 until pathVertices.size - 1) {
            val a = pathVertices[i]
            val b = pathVertices[i + 1]
            val dx = b.x - a.x
            val dz = b.z - a.z
            total += kotlin.math.sqrt(dx * dx + dz * dz)
        }
        return total
    }

    fun netLengthMeters(): Float = (rawLengthMeters() - eraserGapsMeters).coerceAtLeast(0f)

    fun thicknessMeters(): Float = tier.thicknessCm / 100f

    /** Area occupied by this border strip (length x thickness), subtracted from lawn area.[cite: 8] */
    fun footprintAreaMeters2(): Float = netLengthMeters() * thicknessMeters()

    fun requiredPlantCount(): Int {
        if (isStructural) return 0
        val d = densityPerMeter ?: return 0
        val raw = netLengthMeters() * d
        return kotlin.math.ceil(raw.toDouble()).toInt()
    }
}
```[cite: 8]
