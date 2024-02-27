package com.bk.bk1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bk.bk1.compose.MainMapScreen
import com.bk.bk1.compose.SensorConnectScreen
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.utilities.BluetoothScanService
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        checkPermissions()
        setContent {
            BK1Theme {
                val context = LocalContext.current
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "mapScreen") {
                    composable("mapScreen") {
                        val fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(context)
                        MainMapScreen(navController, fusedLocationProviderClient)
                    }
                    composable("sensorConnectScreen") {
                        val btService = BluetoothScanService(context)
                        SensorConnectScreen(navController, btService)
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION)
        }
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