package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.report.ReportGenerator
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@Composable
fun Step5ReportScreen(
    viewModel: LandscapeViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val reportGenerator = remember { ReportGenerator(state) }
    val generatedReportText = reportGenerator.generatedReportText
    
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
                Text(text = generatedReportText, style = MaterialTheme.typography.bodyMedium)
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
