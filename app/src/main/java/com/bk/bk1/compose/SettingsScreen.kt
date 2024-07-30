package com.bk.bk1.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.viewModels.SettingsScreenViewModel
import com.chargemap.compose.numberpicker.NumberPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    navController: NavController,
) {

    val state by viewModel.state.collectAsState()

    // Show dialog window if user wants to change DCI record limit
    if (state.showSetDciLimitDialog) {
        var dciRecordLimit by remember { mutableIntStateOf(state.dciRecordLimit) }
        DciRecordLimitDialog(
            onConfirm = { viewModel.setDciRecordLimit(dciRecordLimit) },
            onDismiss = { viewModel.toggleDciRecordLimitDialog() },
            initialValue = dciRecordLimit,
            onValuesChanged = { value -> dciRecordLimit = value }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.label_settings)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back)
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(15.dp, padding.calculateTopPadding() + 10.dp)
            ) {

                // Clickable Row with DCI record limit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                            shape = getBottomLineShape(lineThicknessDp = 2.dp)
                        )
                        .clickable { viewModel.toggleDciRecordLimitDialog() }
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.label_dci_limit_x))
                    Text(text = state.dciRecordLimit.toString())
                }
            }
        }
    )
}


// Displays a dialogue window with a number picker.
// User can choose the limit of DCI records in a single track.
@Composable
fun DciRecordLimitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    initialValue: Int,
    onValuesChanged: (Int) -> Unit,
) {
    SettingsDialog(
        title = stringResource(R.string.label_set_dci_limit),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            var pickerValue by remember { mutableIntStateOf(initialValue / 50) }

            NumberPicker(
                modifier = Modifier.align(Alignment.Center),
                dividersColor = MaterialTheme.colorScheme.tertiary,
                label = {value -> (value * 50).toString()},
                value = pickerValue,
                range = 1..20,
                onValueChange = {
                    pickerValue = it
                    onValuesChanged(pickerValue * 50)
                }
            )
        }
    }
}


// Settings dialogue window for displaying user inputs.
@Composable
fun SettingsDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        content = {
            Card(
                colors = CardDefaults
                    .cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth()
                ) {
                    Text(title)

                    content()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Button(onClick = {
                            onConfirm()
                            onDismiss()
                        }) {
                            Text(stringResource(R.string.btn_confirm))
                        }

                        Button(onClick = { onDismiss() }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                    }
                }
            }
        },
    )
}

// Function for a line shape
// Source: https://stackoverflow.com/questions/68592618/how-to-add-border-on-bottom-only-in-jetpack-compose
@Composable
private fun getBottomLineShape(lineThicknessDp: Dp) : Shape {
    val lineThicknessPx = with(LocalDensity.current) {lineThicknessDp.toPx()}
    return GenericShape { size, _ ->
        // 1) Bottom-left corner
        moveTo(0f, size.height)
        // 2) Bottom-right corner
        lineTo(size.width, size.height)
        // 3) Top-right corner
        lineTo(size.width, size.height - lineThicknessPx)
        // 4) Top-left corner
        lineTo(0f, size.height - lineThicknessPx)
    }
}