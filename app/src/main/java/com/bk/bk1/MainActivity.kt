package com.bk.bk1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bk.bk1.compose.MainMapScreen
import com.bk.bk1.compose.SensorConnectScreen
import com.bk.bk1.data.TrackDatabase
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.utilities.SensorService
import com.bk.bk1.viewModels.MainMapScreenViewModel
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            BK1Theme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
                val db = TrackDatabase.getDatabase(context)
                NavHost(navController = navController, startDestination = "mapScreen") {
                    composable("mapScreen") {
                        val viewModel = viewModel<MainMapScreenViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return MainMapScreenViewModel(fusedLocationProviderClient, db.trackRecordDao, db.comfortIndexRecordDao) as T
                                }
                            }
                        )
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
                        SensorConnectScreen(navController, sensorServiceStarter)
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