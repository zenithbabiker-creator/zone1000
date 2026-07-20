package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.ar.ARSessionManager
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step1AreaCaptureScreen(
    viewModel: LandscapeViewModel,
    arSessionManager: ARSessionManager,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ArCameraPreview(
            arSessionManager = arSessionManager,
            onTap = { x, y, frame ->
                // إضافة نقطة جديدة للحدود وتحديث المساحة عبر الـ ViewModel
                viewModel.addBoundaryPoint(x, y)
            }
        )

        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مساحة الحديقة: ${String.format("%.2f", state.gardenAreaM2)} م²",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { viewModel.clearBoundaryPoints() }) {
                            Text("مسح النقاط")
                        }
                        Button(
                            onClick = onNext,
                            enabled = state.gardenBoundary.isNotEmpty()
                        ) {
                            Text("تأكيد والمتابعة")
                        }
                    }
                }
            }
        }
    }
}
```[cite: 32]

---

### 2. ملف `Step2_SoilVolume.kt`
```kotlin
package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step2SoilVolumeScreen(
    viewModel: LandscapeViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "الخطوة ٢: حساب كمية التراب", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "مساحة الحديقة: ${String.format("%.2f", state.gardenAreaM2)} م²")
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "اختر سماكة الردم: ${state.soilThicknessCm} سم")
        Slider(
            value = state.soilThicknessCm.toFloat(),
            onValueChange = { viewModel.updateSoilThickness(it.toInt()) },
            valueRange = 10f..100f,
            steps = 18
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "كمية التراب المطلوبة للردم: ${String.format("%.2f", state.soilVolumeM3)} متر مكعب",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) { Text("رجوع") }
            Button(onClick = onNext) { Text("التالي") }
        }
    }
}
```[cite: 33]

---

### 3. ملف `Step3_DesignStudio.kt`
```kotlin
package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step3DesignStudioScreen(
    viewModel: LandscapeViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "الخطوة ٣: استوديو التصميم", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.undo() }, modifier = Modifier.weight(1f)) { Text("تراجع") }
            Button(onClick = { viewModel.redo() }, modifier = Modifier.weight(1f)) { Text("إعادة") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "النباتات المضافة: ${state.royalPalmCount} رويال بالم، ${state.noThornCount} لاشوكة",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(state.plants) { plant ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "نبتة: ${plant.type} عند الإحداثيات (${plant.x}, ${plant.y})",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) { Text("رجوع") }
            Button(onClick = onNext) { Text("التالي") }
        }
    }
}
```[cite: 34]

---

### 4. ملف `Step4_LawnCalculation.kt`
```kotlin
package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step4LawnCalculationScreen(
    viewModel: LandscapeViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "الخطوة ٤: النجيلة والمساحة الصافية", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.netLawnAreaM2.toString(),
            onValueChange = { 
                val value = it.toFloatOrNull() ?: 0f
                viewModel.updateNetLawnArea(value)
            },
            label = { Text("مساحة النجيلة الصافية (م²)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "كثافة الشتلات: ${state.lawnDensityPerM2} شتلة / م²")
        Text(
            text = "إجمالي شتلات النجيلة المطلوبة: ${state.totalLawnPlants} شتلة",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) { Text("رجوع") }
            Button(onClick = onNext) { Text("التالي") }
        }
    }
}
```[cite: 35]

---

### 5. ملف `Step5_ReportScreen.kt`
```kotlin
package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step5ReportScreen(
    viewModel: LandscapeViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "الخطوة ٥: التقرير النهائي", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "• مساحة الحديقة: ${state.gardenAreaM2} م²")
                Text(text = "• كمية التراب: ${state.soilVolumeM3} م³ (سماكة ${state.soilThicknessCm} سم)")
                Text(text = "• إجمالي النخيل والنباتات: ${state.plants.size} نبتة")
                Text(text = "• إجمالي شتلات النجيلة: ${state.totalLawnPlants} شتلة")
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = state.generatedReportText, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) { Text("رجوع") }
            Button(onClick = onFinish) { Text("إنهاء وحفظ التصميم") }
        }
    }
}
```[cite: 36]

---

### الملفات المطلوبة في الدفعة القادمة:
لإكمال ربط المشروع بالكامل، يرجى إرسال الملفات التالية معاً في المرة القادمة:
1. ملف التنقل الرئيسي (`NavGraph.kt` أو الملف المسؤول عن التنقل بين الشاشات `MainActivity.kt`).
2. أي ملفات إضافية خاصة بنماذج المساعدات الهندسية (مثل ملفات الـ `Point3D` أو الـ `ShapeElement` إن وجدت لديك لتكتمل الصورة تماماً).
