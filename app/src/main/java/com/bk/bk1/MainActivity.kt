package com.bk.bk1

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bk.bk1.compose.MainMapScreen
import com.bk.bk1.compose.MapScreenshotterScreen
import com.bk.bk1.compose.SensorConnectScreen
import com.bk.bk1.compose.TrackDetailScreen
import com.bk.bk1.compose.TrackListScreen
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.utilities.BluetoothStateReceiver
import com.bk.bk1.utilities.LocationStateReceiver
import com.bk.bk1.utilities.SensorService
import com.bk.bk1.viewModels.MainMapScreenViewModel
import com.bk.bk1.viewModels.MapScreenshotterScreenViewModel
import com.bk.bk1.viewModels.SensorConnectScreenViewModel
import com.bk.bk1.viewModels.TrackDetailScreenViewModel
import com.bk.bk1.viewModels.TrackListScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothStateReceiver: BluetoothStateReceiver

    @Inject
    lateinit var locationStateReceiver: LocationStateReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        val bluetoothStateReceiverFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, bluetoothStateReceiverFilter)
        val locationStateReceiverFiler = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationStateReceiver, locationStateReceiverFiler)
        setContent {
            BK1Theme(darkTheme = false) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "mapScreen") {
                    composable("mapScreen") {
                        val viewModel = hiltViewModel<MainMapScreenViewModel>()
                        val startSensorServiceWithAction: (String) -> Unit = {
                            Intent(applicationContext, SensorService::class.java)
                                .also { intent ->
                                    intent.action = it
                                    startService(intent)
                                }
                        }
                        MainMapScreen(navController, viewModel, startSensorServiceWithAction)
                    }
                    composable("sensorConnectScreen") {
                        val sensorServiceStarter: (String) -> Unit = { sensorAddress ->
                            Intent(applicationContext, SensorService::class.java)
                                .putExtra("sensorAddress", sensorAddress)
                                .also { intent ->
                                    intent.action = SensorService.Actions.CONNECT.toString()
                                    startService(intent)
                                }
                        }
                        val viewModel = hiltViewModel<SensorConnectScreenViewModel>()
                        SensorConnectScreen(viewModel, navController, sensorServiceStarter)
                    }
                    composable("trackList") {
                        val viewModel = hiltViewModel<TrackListScreenViewModel>()
                        TrackListScreen(viewModel, navController)
                    }
                    composable(
                        "mapScreenshotterScreen/{trackId}",
                        arguments = listOf(navArgument("trackId") {
                            type = NavType.IntType
                        })) { backStackEntry ->
                        val viewModel = hiltViewModel<MapScreenshotterScreenViewModel>()
                        val state by viewModel.state.collectAsState()
                        MapScreenshotterScreen(
                            state,
                            viewModel::onEvent,
                            viewModel,
                            navController,
                            backStackEntry.arguments?.getInt("trackId")
                        )
                    }
                    composable(
                        "trackDetailScreen/{trackId}",
                        arguments = listOf(navArgument("trackId") {
                            type = NavType.IntType
                        })) { backStackEntry ->
                        val viewModel = hiltViewModel<TrackDetailScreenViewModel>()
                        val state by viewModel.state.collectAsState()
                        TrackDetailScreen(
                            state,
                            viewModel::onEvent,
                            navController,
                            backStackEntry.arguments?.getInt("trackId")
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(bluetoothStateReceiver)
        unregisterReceiver(locationStateReceiver)
        super.onDestroy()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
        )

        ActivityCompat.requestPermissions(
            this,
            permissions,
            0
        )
    }
}