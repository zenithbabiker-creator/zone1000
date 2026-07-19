package com.example.landscapedesign.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.R
import com.example.landscapedesign.model.*
import com.example.landscapedesign.ui.components.PlantDropperMenu
import com.example.landscapedesign.ui.components.ShapeToolbar
import com.example.landscapedesign.viewmodel.LandscapeViewModel

/** Active interaction tool in the design canvas. */
private enum class StudioTool { NONE, ARC, CIRCLE, POLYGON, PLANT, ERASER }

/** Helper to map between real world meters and screen pixels */
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
    var lastTouchWorld by remember { mutableStateOf<Point3D?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.step3_title)) },
                    actions = {
                        IconButton(onClick = { viewModel.undo() }) {
                            Icon(Icons.Filled.Undo, contentDescription = "Undo")
                        }
                        IconButton(onClick = { viewModel.redo() }) {
                            Icon(Icons.Filled.Redo, contentDescription = "Redo")
                        }
                    }
                )
                ShapeToolbar(
                    activeTool = activeTool.name,
                    onToolSelected = { toolName ->
                        val tool = try { StudioTool.valueOf(toolName) } catch (e: Exception) { StudioTool.NONE }
                        activeTool = if (activeTool == tool) StudioTool.NONE else tool
                        if (activeTool != StudioTool.PLANT) activePlant = null
                    }
                )
            }
        },
        floatingActionButton = {
            PlantDropperMenu(
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
                        detectTapGestures { offset ->
                            val world = mapper.screenToWorld(offset)
                            lastTouchWorld = world
                            if (activeTool == StudioTool.PLANT && activePlant != null) {
                                // أضفنا ScreenPoint فارغ لتجنب خطأ الـ Missing parameter
                                viewModel.addPlant(PlantNode(type = activePlant!!, world = world, screen = ScreenPoint(offset.x, offset.y)))
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Garden Boundary
                    val boundaryScreen = state.gardenBoundary.map { mapper.worldToScreen(it) }
                    if (boundaryScreen.size >= 2) {
                        for (i in boundaryScreen.indices) {
                            drawLine(Color(0xFF2E7D32), boundaryScreen[i], boundaryScreen[(i + 1) % boundaryScreen.size], strokeWidth = 5f)
                        }
                    }
                    
                    // Borders
                    state.borders.forEach { border ->
                        if (border.pathVertices.size >= 2) {
                            val color = Color(0xFF6D4C41)
                            for (i in 0 until border.pathVertices.size - 1) {
                                drawLine(color, mapper.worldToScreen(border.pathVertices[i]), mapper.worldToScreen(border.pathVertices[i + 1]), strokeWidth = 8f)
                            }
                        }
                    }
                    
                    // Plants
                    state.plants.forEach { plant ->
                        val p = mapper.worldToScreen(plant.world)
                        drawCircle(Color(0xFF33691E), radius = 16f, center = p)
                    }
                }
            }

            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Save & Generate")
            }
        }
    }
}
