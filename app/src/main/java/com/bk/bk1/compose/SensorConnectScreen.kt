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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@SuppressLint("MissingPermission")
@Composable
fun SensorConnectScreen(
    viewModel: SensorConnectScreenViewModel,
    navController: NavController,
    sensorServiceStarter: (address: String) -> Unit
) {

    LaunchedEffect(viewModel) {
        viewModel.startScan()
    }

    val devices by viewModel.devicesLiveData.observeAsState(initial = emptyList())
    val isBluetoothAdapterOn: Boolean by viewModel.isBluetoothAdapterOn.observeAsState(initial = true)
    val context = LocalContext.current

    val backgroundServicePermissionState = rememberMultiplePermissionsState(
        permissions = getBackgroundLocationServicePermissionList()
    )
    val showBackgroundServiceRequest = remember { mutableStateOf(false) }

    val postNotificationPermissionState = rememberMultiplePermissionsState(
        permissions = getPostNotificationsPermissionList()
    )
    val showPostNotificationRequest = remember { mutableStateOf(false) }

    LaunchedEffect(isBluetoothAdapterOn) {
        if (!isBluetoothAdapterOn) {
            viewModel.stopScan()
            Toast.makeText(
                context,
                context.getText(R.string.warn_bt_off),
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    }

    if (showBackgroundServiceRequest.value) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_bg_loc),
            onConfirm = {
                backgroundServicePermissionState.permissions.forEach {
                    it.launchPermissionRequest()
                }
            },
            onDismiss = {
                showBackgroundServiceRequest.value = false
            }
        )
    }

    if (showPostNotificationRequest.value) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_notif),
            onConfirm = {
                postNotificationPermissionState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                showPostNotificationRequest.value = false
            }
        )
    }


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
                    if (isBluetoothAdapterOn) {
                        items(devices) { device ->
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        if (postNotificationPermissionState.allPermissionsGranted) {
                                            if (backgroundServicePermissionState.allPermissionsGranted) {
                                                sensorServiceStarter(device.address)
                                                viewModel.stopScan()
                                                navController.popBackStack()
                                            } else {
                                                showBackgroundServiceRequest.value = true
                                            }
                                        } else {
                                            showPostNotificationRequest.value = true
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