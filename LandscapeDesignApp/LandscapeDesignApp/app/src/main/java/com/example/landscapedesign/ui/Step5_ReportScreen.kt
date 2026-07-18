package com.example.landscapedesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.landscapedesign.R
import com.example.landscapedesign.viewmodel.LandscapeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step5ReportScreen(
    viewModel: LandscapeViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    // التأكد من أن ViewModel يقوم بتوفير الحالة
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text(stringResource(R.string.step5_title)) }
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.final_report_header),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.generatedReportText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // هذا Spacer يدفع الأزرار للأسفل إذا كان التقرير قصيراً
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBack) { 
                    Text(stringResource(R.string.btn_back)) 
                }
                Button(onClick = onFinish) { 
                    Text(stringResource(R.string.btn_finish)) 
                }
            }
        }
    }
}
