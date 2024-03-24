package com.bk.bk1

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.bk.bk1.compose.TrackListScreen
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.utilities.BluetoothStateReceiver
import com.bk.bk1.utilities.SensorService
import com.bk.bk1.viewModels.MainMapScreenViewModel
import com.bk.bk1.viewModels.MapScreenshotterScreenViewModel
import com.bk.bk1.viewModels.SensorConnectScreenViewModel
import com.bk.bk1.viewModels.TrackListScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothStateReceiver: BluetoothStateReceiver
    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)
        setContent {
            BK1Theme {
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
                        MapScreenshotterScreen(
                            viewModel,
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
        super.onDestroy()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        ActivityCompat.requestPermissions(
            this,
            permissions,
            0
        )
//        val permissions = mutableListOf<String>()
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
//            != PackageManager.PERMISSION_GRANTED) {
//            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
//            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
//            permissions.add(Manifest.permission.BLUETOOTH)
//            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
//        }
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
//            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
//        }
//
//        if (permissions.isNotEmpty()) {
//            ActivityCompat.requestPermissions(
//                this,
//                permissions.toTypedArray(),
//                MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION)
//        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions()
                }
                return
            }
        }
    }
}