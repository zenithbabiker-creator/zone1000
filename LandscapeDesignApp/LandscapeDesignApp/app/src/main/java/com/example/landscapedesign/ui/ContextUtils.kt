package com.example.landscapedesign.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Safely walks the [ContextWrapper] chain to find the real hosting [Activity].
 *
 * `LocalContext.current as? Activity` is a common Compose pitfall: on many
 * OEM ROMs / configurations, the Context handed to a Composable is a
 * [android.view.ContextThemeWrapper] (or another ContextWrapper) around the
 * Activity rather than the Activity instance itself, so a direct cast
 * silently returns null. If calling code then does `?: return`, the
 * composable exits with NO crash and NO log — producing a persistent blank
 * screen that is very hard to diagnose. Always prefer this unwrapping
 * helper over a direct cast when a Composable needs the Activity (e.g. to
 * construct an [com.example.landscapedesign.ar.ARSessionManager]).
 */
fun Context.findActivityOrNull(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return ctx as? Activity
}
```[cite: 8]

---

### 2. ملف `DesignElement.kt`
```kotlin
package com.example.landscapedesign.model

import java.util.UUID

/** A single real-world 3D point captured either live or projected from a frozen frame. */
data class Point3D(
    val x: Float, // meters, world space
    val y: Float, // meters, world space (vertical - ignored in area math)
    val z: Float  // meters, world space
)

/** A 2D screen-space point, used for drawing overlays on the canvas / frozen photo. */
data class ScreenPoint(val x: Float, val y: Float)

/** Correlated pair: where a world anchor projects to on screen, and its real-world position. */
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

/** A single planted node (major tree or hedge unit) placed on the canvas. */
data class PlantNode(
    val id: String = UUID.randomUUID().toString(),
    val type: PlantType,
    val customName: String? = null,
    val world: Point3D,
    var screen: ScreenPoint
)

/** A geometric shape (arc/circle/polygon) drawn around a feature, e.g. a tree hedge ring. */
data class ShapeElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType,
    val centerWorld: Point3D? = null,     // for circle/arc
    val radiusMeters: Float? = null,       // for circle/arc
    val startAngleDeg: Float? = null,      // for arc
    val sweepAngleDeg: Float? = null,      // for arc
    val polygonVertices: List<Point3D> = emptyList(), // for custom polygon
    val attachedTreeId: String? = null,    // major tree this shape surrounds, if any
    val hedgePlantName: String? = null,    // e.g. "دورنتا" - null = no hedge fill
    val hedgeDensityPerMeter: Float? = null,
    val hedgePlantCount: Int? = null,
    val eraserGapsMeters: Float = 0f       // total length removed by eraser tool
) {
    /** Real-world perimeter length in meters, accounting for eraser cuts. */
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

    /** Enclosed area of the shape in square meters (used for "no-lawn" subtraction). */
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

/** One of the 3 fixed border tiers, drawn as a path/ribbon around part of the garden. */
data class BorderElement(
    val id: String = UUID.randomUUID().toString(),
    val tier: BorderTier,
    val pathVertices: List<Point3D> = emptyList(), // polyline the user drew
    val plantName: String? = null,                  // null/blank => structural border
    val densityPerMeter: Float? = null,              // plants per linear meter
    val eraserGapsMeters: Float = 0f,                // total length cut out via eraser (doors)
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

    /** Area occupied by this border strip (length x thickness), subtracted from lawn area. */
    fun footprintAreaMeters2(): Float = netLengthMeters() * thicknessMeters()

    fun requiredPlantCount(): Int {
        if (isStructural) return 0
        val d = densityPerMeter ?: return 0
        val raw = netLengthMeters() * d
        return kotlin.math.ceil(raw.toDouble()).toInt()
    }
}
```[cite: 9]

---

### 3. ملف `DesignLayoutState.kt`
```kotlin
package com.example.landscapedesign.model

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
```[cite: 10]

---

### 4. ملف `DesignStudioComponents.kt`
```kotlin
package com.example.landscapedesign.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.R
import com.example.landscapedesign.model.BorderElement

/**
 * Dialog for entering a precise numeric radius (in meters).
 */
@Composable
fun RadiusInputDialog(
    onConfirm: (radiusMeters: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val parsed = text.toFloatOrNull()
    val isValid = parsed != null && parsed > 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.radius_dialog_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.radius_input_label)) },
                singleLine = true,
                isError = text.isNotEmpty() && !isValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(enabled = isValid, onClick = { parsed?.let(onConfirm) }) {
                Text(stringResource(R.string.btn_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}

/**
 * Dialog for entering "trees per meter" density.
 */
@Composable
fun TreesPerMeterDialog(
    plantName: String,
    onConfirm: (treesPerMeter: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("3") }
    val parsed = text.toFloatOrNull()
    val isValid = parsed != null && parsed > 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.trees_per_meter_label)) },
        text = {
            Column {
                Text(plantName, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.trees_per_meter_label)) },
                    singleLine = true,
                    isError = text.isNotEmpty() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(enabled = isValid, onClick = { parsed?.let(onConfirm) }) {
                Text(stringResource(R.string.btn_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}

/**
 * One row of the 3-Tier Border configuration UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorderConfigRow(
    titleRes: Int,
    suggestionRes: Int,
    border: BorderElement,
    onPlantNameChanged: (String) -> Unit,
    onDensityChanged: (Float) -> Unit
) {
    var text by remember(border.tier) { mutableStateOf(border.plantName ?: "") }
    var densityExpanded by remember { mutableStateOf(false) }
    val densityOptions = remember { listOf(2f, 3f, 4f, 5f) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onPlantNameChanged(it)
            },
            label = { Text(stringResource(R.string.plant_name_field_hint)) },
            placeholder = { Text(stringResource(suggestionRes)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (text.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = densityExpanded,
                onExpandedChange = { densityExpanded = !densityExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = border.densityPerMeter?.let { "${it.toInt()}" } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.density_per_meter_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = densityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                DropdownMenu(
                    expanded = densityExpanded,
                    onDismissRequest = { densityExpanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    densityOptions.forEach { d ->
                        DropdownMenuItem(
                            text = { Text("${d.toInt()}") },
                            onClick = {
                                onDensityChanged(d)
                                densityExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.structural_border_flag),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
```[cite: 11]

---

### 5. ملف `FrozenFrameProjector.kt`
```kotlin
package com.example.landscapedesign.ar

import android.opengl.Matrix
import com.example.landscapedesign.model.Point3D
import com.google.ar.core.Plane
import com.google.ar.core.Pose

/**
 * Handles MODE 2: Frozen Photo Interaction.
 *
 * When the camera frame is frozen, we no longer get live ARCore HitResults
 * (those require an active Frame each render tick). Instead we reconstruct the
 * exact camera ray that would have produced that screen pixel — using the
 * SAVED View and Projection matrices from the moment of capture — and
 * intersect that ray with the cached horizontal plane equation. This lets the
 * user tap anywhere on the still image and get back an accurate 3D world
 * point on the same ground plane ARCore already located.
 */
object FrozenFrameProjector {

    /**
     * Converts a 2D screen touch (in pixels) into a normalized device
     * coordinate (NDC) ray in world space, then intersects it with the
     * tracked plane to recover the real-world (x, y, z) meter position.
     *
     * @param screenX, screenY   Touch coordinates in pixels.
     * @param viewportWidth/Height  Size of the view the frozen photo is displayed in.
     * @param viewMatrix       The camera View matrix saved at capture time.
     * @param projectionMatrix The camera Projection matrix saved at capture time.
     * @param plane            The ARCore [Plane] that was tracked at capture time
     *                         (still valid — the session was never destroyed).
     * @return the intersected [Point3D] in world space, or null if the ray is
     *         parallel to the plane or misses it entirely.
     */
    fun projectTouchToPlane(
        screenX: Float,
        screenY: Float,
        viewportWidth: Int,
        viewportHeight: Int,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        plane: Plane
    ): Point3D? {
        val ray = screenPointToWorldRay(
            screenX, screenY, viewportWidth, viewportHeight, viewMatrix, projectionMatrix
        ) ?: return null

        val planePose = plane.centerPose
        val planeNormal = planePose.yAxis // horizontal plane's "up" normal
        val planePoint = floatArrayOf(planePose.tx(), planePose.ty(), planePose.tz())

        val hit = rayPlaneIntersection(
            rayOrigin = ray.origin,
            rayDirection = ray.direction,
            planePoint = planePoint,
            planeNormal = planeNormal
        ) ?: return null

        // Reject hits outside the physically detected plane polygon to avoid
        // "phantom" points floating past real walls/edges.
        val hitPose = Pose.makeTranslation(hit[0], hit[1], hit[2])
        if (!plane.isPoseInPolygon(hitPose)) return null

        return Point3D(hit[0], hit[1], hit[2])
    }

    /** Simpler variant for when you just want to intersect with a known Y-height ground plane
     *  (e.g. reusing the same plane height recorded during Live mode, without a live Plane object). */
    fun projectTouchToGroundHeight(
        screenX: Float,
        screenY: Float,
        viewportWidth: Int,
        viewportHeight: Int,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        groundY: Float
    ): Point3D? {
        val ray = screenPointToWorldRay(
            screenX, screenY, viewportWidth, viewportHeight, viewMatrix, projectionMatrix
        ) ?: return null

        val hit = rayPlaneIntersection(
            rayOrigin = ray.origin,
            rayDirection = ray.direction,
            planePoint = floatArrayOf(0f, groundY, 0f),
            planeNormal = floatArrayOf(0f, 1f, 0f)
        ) ?: return null

        return Point3D(hit[0], hit[1], hit[2])
    }

    private data class WorldRay(val origin: FloatArray, val direction: FloatArray)

    /**
     * Unprojects a screen pixel into a world-space ray using the inverse of the
     * combined View-Projection matrix, sampling both the near and far clip planes.
     */
    private fun screenPointToWorldRay(
        screenX: Float,
        screenY: Float,
        viewportWidth: Int,
        viewportHeight: Int,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ): WorldRay? {
        // Convert pixel coords -> Normalized Device Coordinates (-1..1), flipping Y.
        val ndcX = (2f * screenX / viewportWidth) - 1f
        val ndcY = 1f - (2f * screenY / viewportHeight)

        val viewProjection = FloatArray(16)
        Matrix.multiplyMM(viewProjection, 0, projectionMatrix, 0, viewMatrix, 0)

        val invViewProjection = FloatArray(16)
        if (!Matrix.invertM(invViewProjection, 0, viewProjection, 0)) {
            return null // Non-invertible matrix, camera in a degenerate state.
        }

        val nearPointClip = floatArrayOf(ndcX, ndcY, -1f, 1f)
        val farPointClip = floatArrayOf(ndcX, ndcY, 1f, 1f)

        val nearWorld = FloatArray(4)
        val farWorld = FloatArray(4)
        Matrix.multiplyMV(nearWorld, 0, invViewProjection, 0, nearPointClip, 0)
        Matrix.multiplyMV(farWorld, 0, invViewProjection, 0, farPointClip, 0)

        // Perspective divide.
        if (nearWorld[3] == 0f || farWorld[3] == 0f) return null
        for (i in 0..2) {
            nearWorld[i] /= nearWorld[3]
            farWorld[i] /= farWorld[3]
        }

        val origin = floatArrayOf(nearWorld[0], nearWorld[1], nearWorld[2])
        val direction = floatArrayOf(
            farWorld[0] - nearWorld[0],
            farWorld[1] - nearWorld[1],
            farWorld[2] - nearWorld[2]
        )
        normalize(direction)
        return WorldRay(origin, direction)
    }

    /** Standard ray-plane intersection: t = dot(planePoint - rayOrigin, normal) / dot(direction, normal). */
    private fun rayPlaneIntersection(
        rayOrigin: FloatArray,
        rayDirection: FloatArray,
        planePoint: FloatArray,
        planeNormal: FloatArray
    ): FloatArray? {
        val denom = dot(rayDirection, planeNormal)
        if (kotlin.math.abs(denom) < 1e-6f) return null // Ray parallel to plane.

        val diff = floatArrayOf(
            planePoint[0] - rayOrigin[0],
            planePoint[1] - rayOrigin[1],
            planePoint[2] - rayOrigin[2]
        )
        val t = dot(diff, planeNormal) / denom
        if (t < 0f) return null // Intersection is behind the camera.

        return floatArrayOf(
            rayOrigin[0] + rayDirection[0] * t,
            rayOrigin[1] + rayDirection[1] * t,
            rayOrigin[2] + rayDirection[2] * t
        )
    }

    private fun dot(a: FloatArray, b: FloatArray): Float = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]

    private fun normalize(v: FloatArray) {
        val len = kotlin.math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
        if (len > 1e-6f) {
            v[0] /= len; v[1] /= len; v[2] /= len
        }
    }

    /** Pose extension: local Y axis (plane normal) in world space. */
    private val Pose.yAxis: FloatArray
        get() {
            val q = floatArrayOf(qx(), qy(), qz(), qw())
            // Rotate the local (0,1,0) up-vector by this pose's rotation quaternion.
            return rotateVectorByQuaternion(floatArrayOf(0f, 1f, 0f), q)
        }

    private fun rotateVectorByQuaternion(v: FloatArray, q: FloatArray): FloatArray {
        // q = (x, y, z, w)
        val qx = q[0]; val qy = q[1]; val qz = q[2]; val qw = q[3]
        val ix = qw * v[0] + qy * v[2] - qz * v[1]
        val iy = qw * v[1] + qz * v[0] - qx * v[2]
        val iz = qw * v[2] + qx * v[1] - qy * v[0]
        val iw = -qx * v[0] - qy * v[1] - qz * v[2]

        return floatArrayOf(
            ix * qw + iw * -qx + iy * -qz - iz * -qy,
            iy * qw + iw * -qy + iz * -qx - ix * -qz,
            iz * qw + iw * -qz + ix * -qy - iy * -qx
        )
    }
}
```[cite: 12]

---

### 6. ملف `GeometryUtils.kt`
```kotlin
package com.example.landscapedesign.geometry

import com.example.landscapedesign.model.Point3D
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Unified geometry math used by BOTH Live AR mode and Frozen Photo mode.
 * All functions operate on real-world meter coordinates (Point3D), ignoring
 * the Y (vertical) axis since every point is assumed to lie on the same
 * horizontal ground plane.
 */
object GeometryUtils {

    /**
     * Shoelace (Gauss) formula applied to the (X, Z) plane.
     * Works identically whether points came from a live HitTest or a
     * frozen-frame ray-plane projection — both produce Point3D in world space.
     */
    fun shoelaceArea(points: List<Point3D>): Float {
        if (points.size < 3) return 0f
        var sum = 0.0
        val n = points.size
        for (i in 0 until n) {
            val a = points[i]
            val b = points[(i + 1) % n]
            sum += (a.x.toDouble() * b.z.toDouble()) - (b.x.toDouble() * a.z.toDouble())
        }
        return abs(sum / 2.0).toFloat()
    }

    /** Rounds a value to 2 decimal places for professional display. */
    fun round2(value: Float): Float {
        return (Math.round(value * 100.0) / 100.0).toFloat()
    }

    fun round2(value: Double): Double = Math.round(value * 100.0) / 100.0

    /** Euclidean distance between two world points on the (X,Z) plane, in meters. */
    fun distanceXZ(a: Point3D, b: Point3D): Float {
        val dx = b.x - a.x
        val dz = b.z - a.z
        return sqrt(dx * dx + dz * dz)
    }

    /**
     * Given a touch point and a set of boundary edges (each edge = pair of Point3D
     * forming the outer polygon), returns the distance in meters from the point to
     * the two nearest edges. Used for the dynamic measurement-line feature in Step 3.
     */
    fun nearestTwoEdgeDistances(
        point: Point3D,
        boundaryPolygon: List<Point3D>
    ): List<EdgeDistance> {
        if (boundaryPolygon.size < 2) return emptyList()
        val distances = mutableListOf<EdgeDistance>()
        val n = boundaryPolygon.size
        for (i in 0 until n) {
            val a = boundaryPolygon[i]
            val b = boundaryPolygon[(i + 1) % n]
            val (dist, closest) = distancePointToSegment(point, a, b)
            distances.add(EdgeDistance(edgeStart = a, edgeEnd = b, distanceMeters = dist, closestPoint = closest))
        }
        return distances.sortedBy { it.distanceMeters }.take(2)
    }

    /** Perpendicular distance (X,Z plane) from a point to a line segment, plus the closest point on it. */
    private fun distancePointToSegment(p: Point3D, a: Point3D, b: Point3D): Pair<Float, Point3D> {
        val abx = b.x - a.x
        val abz = b.z - a.z
        val apx = p.x - a.x
        val apz = p.z - a.z
        val abLenSq = abx * abx + abz * abz
        val t = if (abLenSq > 1e-6f) ((apx * abx + apz * abz) / abLenSq).coerceIn(0f, 1f) else 0f
        val closestX = a.x + abx * t
        val closestZ = a.z + abz * t
        val closest = Point3D(closestX, a.y, closestZ)
        val dist = distanceXZ(p, closest)
        return dist to closest
    }

    /** Round up to nearest whole integer (used for plant/tree counts). */
    fun ceilToInt(value: Float): Int = kotlin.math.ceil(value.toDouble()).toInt()
    fun ceilToInt(value: Double): Int = kotlin.math.ceil(value).toInt()
}

data class EdgeDistance(
    val edgeStart: Point3D,
    val edgeEnd: Point3D,
    val distanceMeters: Float,
    val closestPoint: Point3D
)
```[cite: 13]

---

### 7. ملف `MainActivity.kt`
```kotlin
package com.example.landscapedesign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.ui.Step1AreaCaptureScreen
import com.example.landscapedesign.ui.theme.LandscapeDesignTheme
import com.example.landscapedesign.viewmodel.LandscapeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LandscapeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LandscapeDesignTheme {
                val navController = rememberNavController()
                val arSessionManager = remember { ARSessionManager() }
                
                NavHost(navController = navController, startDestination = "step1") {
                    composable("step1") {
                        Step1AreaCaptureScreen(
                            viewModel = viewModel,
                            arSessionManager = arSessionManager,
                            onNext = { /* انتقال */ }
                        )
                    }
                }
            }
        }
    }
}
```[cite: 14]

---

### 8. ملف `PixelCopyHelper.kt`
```kotlin
package com.example.landscapedesign.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy

/**
 * Asynchronously reads the pixels currently displayed within a given
 * on-screen region (in window coordinates) into a [Bitmap]. Used by Step 1
 * to "freeze" the live AR camera feed into a still photo when the user
 * presses Capture.
 *
 * Deliberately takes a plain [Rect] + [Activity] rather than a specific
 * [android.view.View] reference: SceneView's `ARSceneView` (2.x) is a pure
 * Compose function, not an instantiable View subclass, so there is no
 * dedicated View to hold onto. [PixelCopy] only needs the target [Rect] in
 * window coordinates and the hosting [android.view.Window] — both of which
 * are stable Android platform APIs unaffected by any third-party library
 * version change.
 */
object PixelCopyHelper {

    fun copyFromWindow(activity: Activity, boundsInWindow: Rect, onResult: (Bitmap?) -> Unit) {
        val width = boundsInWindow.width()
        val height = boundsInWindow.height()
        if (width <= 0 || height <= 0) {
            onResult(null)
            return
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(
            activity.window,
            boundsInWindow,
            bitmap,
            { copyResult -> onResult(if (copyResult == PixelCopy.SUCCESS) bitmap else null) },
            Handler(Looper.getMainLooper())
        )
    }
}
```[cite: 15]

---

### 9. ملف `PolygonClipper.kt`
```kotlin
package com.example.landscapedesign.geometry

import com.example.landscapedesign.model.BorderElement
import com.example.landscapedesign.model.Point3D
import com.example.landscapedesign.model.ShapeElement
import com.example.landscapedesign.model.ShapeType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Handles subtractive area math for Step 4: cutting border-strip areas and
 * major-tree-hedge "no lawn" zones out of the base garden polygon.
 *
 * Approach:
 * 1. Approximate every Circle/Arc shape as an N-sided polygon (for accurate area
 *    and, if needed, visual clipping against the base polygon).
 * 2. Sum enclosed areas of all tree zones (Circle/Arc/Polygon hedge rings).
 * 3. Sum border-strip footprint areas (net length x thickness).
 * 4. Net Lawn Area = Base Area - SUM(tree zone areas) - SUM(border footprint areas),
 *    clamped to zero. Overlapping zones are assumed non-overlapping by design
 *    convention (hedge rings are placed apart); if visual overlap detection is
 *    required later, a full Sutherland-Hodgman boolean union can be layered on
 *    top of approximatedPolygon() below.
 */
object PolygonClipper {

    /** Turns a Circle or Arc into a polygon approximation (for area/visual clip use). */
    fun approximatePolygon(shape: ShapeElement, segments: Int = 32): List<Point3D> {
        if (shape.type == ShapeType.POLYGON) return shape.polygonVertices
        val center = shape.centerWorld ?: return emptyList()
        val radius = shape.radiusMeters ?: return emptyList()
        val startDeg = shape.startAngleDeg ?: 0f
        val sweepDeg = if (shape.type == ShapeType.ARC) (shape.sweepAngleDeg ?: 360f) else 360f

        val points = mutableListOf<Point3D>()
        val steps = if (shape.type == ShapeType.CIRCLE) segments else (segments * (sweepDeg / 360f)).toInt().coerceAtLeast(2)
        for (i in 0..steps) {
            val angleDeg = startDeg + (sweepDeg * i / steps)
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val x = center.x + radius * cos(angleRad).toFloat()
            val z = center.z + radius * sin(angleRad).toFloat()
            points.add(Point3D(x, center.y, z))
        }
        // For an arc, close the fan back to the center so the enclosed area is a pie slice.
        if (shape.type == ShapeType.ARC) {
            points.add(center)
        }
        return points
    }

    /** Total area (m²) occupied by "no-lawn" tree zones (hedge rings, circles, arcs, polygons). */
    fun totalTreeZoneArea(shapes: List<ShapeElement>): Float {
        return shapes.sumOf { it.enclosedAreaMeters2().toDouble() }.toFloat()
    }

    /** Total footprint area (m²) occupied by the 3-tier borders. */
    fun totalBorderFootprintArea(borders: List<BorderElement>): Float {
        return borders.sumOf { it.footprintAreaMeters2().toDouble() }.toFloat()
    }

    /**
     * Computes the Net Lawn Area following the strict hierarchy:
     * Base Area - (Border Footprint Areas + Tree Zone Areas), clamped at 0.
     */
    fun netLawnArea(
        baseAreaMeters2: Float,
        borders: List<BorderElement>,
        treeZoneShapes: List<ShapeElement>
    ): Float {
        val borderArea = totalBorderFootprintArea(borders)
        val treeZoneArea = totalTreeZoneArea(treeZoneShapes)
        val net = baseAreaMeters2 - (borderArea + treeZoneArea)
        return net.coerceAtLeast(0f)
    }

    /**
     * Simple Sutherland-Hodgman clip: clips subjectPolygon against a CONVEX
     * clipPolygon, returning the intersection polygon. Provided for future
     * visual "cut-out" rendering of tree zones directly on the base garden
     * polygon outline (base garden polygons captured in Step 1 are typically
     * convex-ish user-drawn boundaries).
     */
    fun clipPolygon(subject: List<Point3D>, clip: List<Point3D>): List<Point3D> {
        if (subject.isEmpty() || clip.isEmpty()) return emptyList()
        var output = subject
        for (i in clip.indices) {
            if (output.isEmpty()) break
            val a = clip[i]
            val b = clip[(i + 1) % clip.size]
            output = clipEdge(output, a, b)
        }
        return output
    }

    private fun clipEdge(poly: List<Point3D>, a: Point3D, b: Point3D): List<Point3D> {
        val result = mutableListOf<Point3D>()
        for (i in poly.indices) {
            val current = poly[i]
            val prev = poly[(i - 1 + poly.size) % poly.size]
            val currentInside = isInside(current, a, b)
            val prevInside = isInside(prev, a, b)
            if (currentInside) {
                if (!prevInside) result.add(intersect(prev, current, a, b))
                result.add(current)
            } else if (prevInside) {
                result.add(intersect(prev, current, a, b))
            }
        }
        return result
    }

    private fun isInside(p: Point3D, a: Point3D, b: Point3D): Boolean {
        return (b.x - a.x) * (p.z - a.z) - (b.z - a.z) * (p.x - a.x) >= 0
    }

    private fun intersect(p1: Point3D, p2: Point3D, a: Point3D, b: Point3D): Point3D {
        val a1 = b.z - a.z
        val b1 = a.x - b.x
        val c1 = a1 * a.x + b1 * a.z

        val a2 = p2.z - p1.z
        val b2 = p1.x - p2.x
        val c2 = a2 * p1.x + b2 * p1.z

        val det = a1 * b2 - a2 * b1
        return if (kotlin.math.abs(det) < 1e-6f) {
            p2
        } else {
            val x = (b2 * c1 - b1 * c2) / det
            val z = (a1 * c2 - a2 * c1) / det
            Point3D(x, p1.y, z)
        }
    }
}
```[cite: 16]

---

### 10. ملف `ReportGenerator.kt`
```kotlin
package com.example.landscapedesign.report

import com.example.landscapedesign.geometry.GeometryUtils
import com.example.landscapedesign.model.BorderElement
import com.example.landscapedesign.model.BorderTier
import com.example.landscapedesign.model.DesignLayoutState
import com.example.landscapedesign.model.PlantNode
import com.example.landscapedesign.model.PlantType
import com.example.landscapedesign.model.Point3D
import com.example.landscapedesign.model.ShapeElement

/**
 * Compiles the entire [DesignLayoutState] into a single, detailed Arabic
 * narrative report intended to feed a downstream generative-AI rendering
 * stage. All four sections are populated live from current app state.
 */
object ReportGenerator {

    /**
     * @param boundary the base garden polygon (used to compute each major
     * tree's distance to the two nearest boundary edges).
     */
    fun generateFinalLandscapeReport(
        state: DesignLayoutState,
        boundary: List<Point3D> = state.gardenBoundary
    ): String {
        val sb = StringBuilder()

        sb.append(sectionOneGeneralDimensions(state))
        sb.append("\n\n")
        sb.append(sectionTwoMajorTrees(state, boundary))
        sb.append("\n\n")
        sb.append(sectionThreeBorders(state))
        sb.append("\n\n")
        sb.append(sectionFourLawn(state))

        return sb.toString()
    }

    // -----------------------------------------------------------------
    // SECTION 1: General dimensions & site preparation
    // -----------------------------------------------------------------
    private fun sectionOneGeneralDimensions(state: DesignLayoutState): String {
        return buildString {
            append("القسم الأول: الأبعاد العامة وتجهيز الموقع\n")
            append(
                "المساحة الإجمالية للحديقة هي %.2f متر مربع. ".format(state.gardenAreaM2)
            )
            append(
                "سماكة الردم بالتراب المطلوبة هي %d سم، مما ينتج عنه حجم إجمالي من التراب المطلوب يبلغ %.2f متر مكعب."
                    .format(state.soilThicknessCm, state.soilVolumeM3)
            )
        }
    }

    // -----------------------------------------------------------------
    // SECTION 2: Major trees & spatial positioning
    // -----------------------------------------------------------------
    private fun sectionTwoMajorTrees(state: DesignLayoutState, boundary: List<Point3D>): String {
        val majorTrees = state.plants.filter {
            it.type == PlantType.ROYAL_PALM || it.type == PlantType.NO_THORN
        }
        if (majorTrees.isEmpty()) {
            return "القسم الثاني: الأشجار الرئيسية وموقعها المكاني\nلم تتم إضافة أشجار رئيسية بعد."
        }

        val sb = StringBuilder("القسم الثاني: الأشجار الرئيسية وموقعها المكاني\n")
        for (tree in majorTrees) {
            val treeName = plantDisplayName(tree)
            val edges = if (boundary.size >= 2) {
                GeometryUtils.nearestTwoEdgeDistances(tree.world, boundary)
            } else emptyList()

            val distRight = edges.getOrNull(0)?.distanceMeters ?: 0f
            val distLeft = edges.getOrNull(1)?.distanceMeters ?: 0f

            sb.append(
                "شجرة %s رئيسية تقع على بعد %.2f متر من الحد الأيمن للحديقة و%.2f متر من الحد الأيسر. "
                    .format(treeName, distRight, distLeft)
            )

            val attachedShape = state.shapes.firstOrNull { it.attachedTreeId == tree.id }
            if (attachedShape != null) {
                val shapeLabel = shapeDisplayName(attachedShape)
                val radius = attachedShape.radiusMeters ?: 0f
                sb.append(
                    "تحيط بهذه الشجرة عنصر تنسيقي على شكل %s بنصف قطر دقيق يبلغ %.2f متر. "
                        .format(shapeLabel, radius)
                )
                if (attachedShape.hedgePlantName != null) {
                    sb.append(
                        "تمت زراعة هذا المحيط كتحوّط باستخدام نبات %s بكثافة زراعة %.1f شجرة في المتر، بإجمالي %d نبتة تحوّط مطلوبة."
                            .format(
                                attachedShape.hedgePlantName,
                                attachedShape.hedgeDensityPerMeter ?: 0f,
                                attachedShape.hedgePlantCount ?: 0
                            )
                    )
                }
            }
            sb.append("\n")
        }
        return sb.toString().trim()
    }

    // -----------------------------------------------------------------
    // SECTION 3: Landscape borders & eraser integration
    // -----------------------------------------------------------------
    private fun sectionThreeBorders(state: DesignLayoutState): String {
        val sb = StringBuilder("القسم الثالث: محددات الحديقة والفتحات (الأسوار والممرات)\n")
        for (border in state.borders) {
            sb.append(borderDescription(border))
            sb.append("\n")
        }
        return sb.toString().trim()
    }

    private fun borderDescription(border: BorderElement): String {
        val tierLabel = when (border.tier) {
            BorderTier.LARGE -> "كبير"
            BorderTier.MEDIUM -> "متوسط"
            BorderTier.SMALL -> "صغير"
        }
        val netLength = GeometryUtils.round2(border.netLengthMeters())
        if (border.rawLengthMeters() <= 0f) {
            return "المحدد ال$tierLabel بسماكة ${border.tier.thicknessCm} سم لم يتم رسمه بعد."
        }

        val statusText = if (border.isStructural) {
            "إنشائي (خرساني/حجري) بدون نباتات"
        } else {
            "مزروع بنبات ${border.plantName}"
        }

        val plantCountText = if (!border.isStructural) {
            " عدد النباتات المطلوبة لهذا المحدد هو ${border.requiredPlantCount()} نبتة بناءً على كثافة ${border.densityPerMeter ?: 0f} نبتة في المتر."
        } else ""

        return "محدد $tierLabel بسماكة ${border.tier.thicknessCm} سم يمتد بطول صافٍ يبلغ %.2f متر. تم تكوينه كـ%s. يحتوي هذا المحدد على %d فتحة/باب تم إنشاؤها بواسطة المستخدم، مما قلل طوله الأصلي بمقدار %.2f متر.%s"
            .format(netLength, statusText, border.openingsCount, GeometryUtils.round2(border.eraserGapsMeters), plantCountText)
    }

    // -----------------------------------------------------------------
    // SECTION 4: Net lawn area & seeding density
    // -----------------------------------------------------------------
    private fun sectionFourLawn(state: DesignLayoutState): String {
        return "القسم الرابع: مساحة النجيلة الصافية وكثافة البذر\n" +
            "بعد خصم جميع المحددات الإنشائية ومناطق الأشجار ومحيطاتها بدقة، تم حساب المساحة المتبقية المفتوحة. " +
            "مساحة النجيلة الصافية المخصصة لزراعة العشب هي بالضبط %.2f متر مربع. ".format(state.netLawnAreaM2) +
            "سيتم زراعة هذه المساحة بشتلات النجيل بكثافة %d شتلة في المتر المربع، بإجمالي مطلوب يبلغ %d شتلة نجيل."
                .format(state.lawnDensityPerM2, state.totalLawnPlants)
    }

    private fun plantDisplayName(plant: PlantNode): String = when (plant.type) {
        PlantType.NO_THORN -> "اللاشوكة"
        PlantType.ROYAL_PALM -> "رويال بالم"
        PlantType.DURANTA -> "دورنتا"
        PlantType.CUSTOM -> plant.customName ?: "نبات مخصص"
    }

    private fun shapeDisplayName(shape: ShapeElement): String = when (shape.type) {
        com.example.landscapedesign.model.ShapeType.ARC -> "قوس"
        com.example.landscapedesign.model.ShapeType.CIRCLE -> "دائرة"
        com.example.landscapedesign.model.ShapeType.POLYGON -> "شكل مخصص"
    }
}
```[cite: 17]
