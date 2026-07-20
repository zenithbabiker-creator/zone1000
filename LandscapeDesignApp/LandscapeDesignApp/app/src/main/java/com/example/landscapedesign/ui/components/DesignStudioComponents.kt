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
 * Dialog for entering a precise numeric radius (in meters).[cite: 10]
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
 * Dialog for entering "trees per meter" density.[cite: 10]
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
 * One row of the 3-Tier Border configuration UI.[cite: 10]
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
