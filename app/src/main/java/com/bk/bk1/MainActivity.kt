package com.bk.bk1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bk.bk1.compose.MainMapScreen
import com.bk.bk1.compose.SensorConnectScreen
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.utilities.SensorService
import com.bk.bk1.viewModels.MainMapScreenViewModel
import com.bk.bk1.viewModels.SensorConnectScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
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
                }
            }
        }
    }

    override fun onDestroy() {
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
            Manifest.permission.FOREGROUND_SERVICE
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