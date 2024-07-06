@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.bk.bk1.compose

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.utilities.getBackgroundLocationServicePermissionList
import com.bk.bk1.utilities.getPostNotificationsPermissionList
import com.bk.bk1.viewModels.SensorConnectScreenViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// Component that represents a screen where the user can see nearby movesense sensors
// and connect to one of them.
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SensorConnectScreen(
    viewModel: SensorConnectScreenViewModel,
    navController: NavController,
    sensorServiceStarter: (address: String) -> Unit
) {

    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.startScan()
    }
    val context = LocalContext.current

    val backgroundServicePermissionState = rememberMultiplePermissionsState(
        permissions = getBackgroundLocationServicePermissionList()
    )

    val postNotificationPermissionState = rememberMultiplePermissionsState(
        permissions = getPostNotificationsPermissionList()
    )

    // Stop scanning a return user to the previous screen if Bluetooth adapter is off.
    LaunchedEffect(state.isBluetoothAdapterOn) {
        if (!state.isBluetoothAdapterOn) {
            viewModel.stopScan()
            Toast.makeText(
                context,
                context.getText(R.string.warn_bt_off),
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    }

    if (state.showBackgroundServiceRequest) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_bg_loc),
            onConfirm = {
                backgroundServicePermissionState.permissions.forEach {
                    it.launchPermissionRequest()
                }
            },
            onDismiss = {
                viewModel.setShowBackgroundServiceRequest(false)
            }
        )
    }

    if (state.showPostNotificationRequest) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_notif),
            onConfirm = {
                postNotificationPermissionState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                viewModel.setShowPostNotificationRequest(false)
            }
        )
    }

    // Main container. Displays a list of nearby movesense sensors.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_connect_sensor)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.stopScan()
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
                modifier = Modifier.padding(padding)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.isBluetoothAdapterOn) {
                        items(state.deviceList) { device ->
                            Row(
                                modifier = Modifier
                                    .clickable(enabled = !state.blockButtonClicks) {
                                        if (postNotificationPermissionState.allPermissionsGranted) {
                                            if (backgroundServicePermissionState.allPermissionsGranted) {
                                                viewModel.setBlockButtonClicks(true)
                                                sensorServiceStarter(device.address)
                                                viewModel.stopScan()
                                                navController.popBackStack()
                                            } else {
                                                viewModel.setShowBackgroundServiceRequest(true)
                                            }
                                        } else {
                                            viewModel.setShowPostNotificationRequest(true)
                                        }
                                    }
                                    .height(70.dp)
                                    .fillMaxWidth()
                                    .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,

                                ) {
                                Text(device.name.toString())
                            }
                        }
                    }
                }
            }
        }
    )
}

// Testing previews.

@Preview
@Composable
fun ListItemPreview() {
    Row(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color.LightGray)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("BLAHADASLDNASOJDNAOJ")
    }
}