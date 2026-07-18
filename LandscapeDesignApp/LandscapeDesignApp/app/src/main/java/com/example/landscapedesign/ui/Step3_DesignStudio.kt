package com.example.landscapedesign.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.R
import com.example.landscapedesign.geometry.GeometryUtils
import com.example.landscapedesign.model.*
import com.example.landscapedesign.ui.components.BorderConfigRow
import com.example.landscapedesign.ui.components.RadiusInputDialog
import com.example.landscapedesign.ui.components.TreesPerMeterDialog
import com.example.landscapedesign.viewmodel.LandscapeViewModel

/** Active interaction tool in the design canvas. */
private enum class StudioTool { NONE, ARC, CIRCLE, POLYGON, PLANT, ERASER }

/**
 * Real-world-to-screen scale used to convert the garden's meter coordinates
 * into canvas pixel offsets.
 */
private class ScreenMapper(val canvasSize: IntSize, val pixelsPerMeter: Float = 40f) {
    private val originX get() = canvasSize.width / 2f
    private val originY get() = canvasSize.height / 2f

    fun worldToScreen(p: Point3D): Offset =
        Offset(originX + p.x * pixelsPerMeter, originY - p.z * pixelsPerMeter)

    fun screenToWorld(offset: Offset): Point3D {
        val x = (offset.x - originX) / pixelsPerMeter
        val z = -(offset.y - originY) / pixelsPerMeter
        return Point3D(x, 0f, z)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3DesignStudioScreen(
    viewModel: LandscapeViewModel,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }
    val mapper = remember(canvasSize) { ScreenMapper(canvasSize) }

    var activeTool by remember { mutableStateOf(StudioTool.NONE) }
    var activePlant by remember { mutableStateOf<PlantType?>(null) }

    var pendingShapeCenter by remember { mutableStateOf<Point3D?>(null) }
    var pendingShapeType by remember { mutableStateOf<ShapeType?>(null) }
    var showRadiusDialog by remember { mutableStateOf(false) }

    var pendingHedgeShapeId by remember { mutableStateOf<String?>(null) }
    var showTreesPerMeterDialog by remember { mutableStateOf(false) }

    var lastTouchWorld by remember { mutableStateOf<Point3D?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.step3_title)) },
                    actions = {
                        IconButton(onClick = { viewModel.undo() }) {
                            Icon(Icons.Filled.Undo, contentDescription = stringResource(R.string.undo))
                        }
                        IconButton(onClick = { viewModel.redo() }) {
                            Icon(Icons.Filled.Redo, contentDescription = stringResource(R.string.redo))
                        }
                    }
                )
                ShapeToolbar(
                    activeTool = activeTool,
                    onToolSelected = { tool ->
                        activeTool = if (activeTool == tool) StudioTool.NONE else tool
                        if (activeTool != StudioTool.PLANT) activePlant = null
                    }
                )
            }
        },
        floatingActionButton = {
            PlantDropperMenu(
                activePlant = activePlant,
                onPlantSelected = { plant ->
                    activePlant = plant
                    activeTool = StudioTool.PLANT
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFEFEBE0))
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(activeTool, activePlant) {
                        if (activeTool == StudioTool.ERASER) {
                            detectDragGestures { change, dragAmount ->
                                val stepStartScreen = change.position - dragAmount
                                val w1 = mapper.screenToWorld(stepStartScreen)
                                val w2 = mapper.screenToWorld(change.position)
                                val gap = GeometryUtils.distanceXZ(w1, w2)
                                val nearestTier = state.borders
                                    .filter { it.rawLengthMeters() > 0f }
                                    .maxByOrNull { it.rawLengthMeters() }
                                    ?.tier
                                if (nearestTier != null && gap > 0.01f) {
                                    viewModel.eraseBorderSegment(nearestTier, gap)
                                }
                            }
                        } else {
                            detectTapGestures { offset ->
                                val world = mapper.screenToWorld(offset)
                                lastTouchWorld = world
                                when (activeTool) {
                                    StudioTool.CIRCLE, StudioTool.ARC -> {
                                        pendingShapeCenter = world
                                        pendingShapeType = if (activeTool == StudioTool.CIRCLE) ShapeType.CIRCLE else ShapeType.ARC
                                        showRadiusDialog = true
                                    }
                                    StudioTool.PLANT -> {
                                        activePlant?.let { plant ->
                                            viewModel.addPlant(PlantNode(type = plant, world = world, screen = ScreenPoint(offset.x, offset.y)))
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // --- Garden boundary ---
                    val boundaryScreen = state.gardenBoundary.map { mapper.worldToScreen(it) }
                    if (boundaryScreen.size >= 2) {
                        for (i in boundaryScreen.indices) {
                            drawLine(Color(0xFF2E7D32), boundaryScreen[i], boundaryScreen[(i + 1) % boundaryScreen.size], strokeWidth = 5f)
                        }
                    }
                    // --- Shapes ---
                    state.shapes.forEach { drawShape(it, mapper) }
                    // --- Borders ---
                    state.borders.forEach { border ->
                        if (border.pathVertices.size >= 2) {
                            val widthPx = border.thicknessMeters() * mapper.pixelsPerMeter
                            val color = when (border.tier) {
                                BorderTier.LARGE -> Color(0xFF6D4C41)
                                BorderTier.MEDIUM -> Color(0xFF8D6E63)
                                BorderTier.SMALL -> Color(0xFFA1887F)
                            }
                            for (i in 0 until border.pathVertices.size - 1) {
                                drawLine(color, mapper.worldToScreen(border.pathVertices[i]), mapper.worldToScreen(border.pathVertices[i + 1]), strokeWidth = widthPx.coerceAtLeast(4f))
                            }
                        }
                    }
                    // --- Plants ---
                    state.plants.forEach { plant ->
                        val p = mapper.worldToScreen(plant.world)
                        drawCircle(Color(0xFF33691E), radius = 16f, center = p)
                        drawCircle(Color.Black, radius = 16f, center = p, style = Stroke(width = 2f))
                    }
                    // --- Measurements ---
                    lastTouchWorld?.let { touch ->
                        val edges = GeometryUtils.nearestTwoEdgeDistances(touch, state.gardenBoundary)
                        val touchScreen = mapper.worldToScreen(touch)
                        edges.forEach { edge ->
                            drawLine(Color(0xFFD32F2F), touchScreen, mapper.worldToScreen(edge.closestPoint), strokeWidth = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))
                        }
                    }
                }
            }

            // Summary Section (omitted partial details for brevity in display)
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Add your UI Cards here as per original
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.btn_save_generate))
                }
            }
        }
    }
    // Dialogs implementation remains as in your original logic...
}
