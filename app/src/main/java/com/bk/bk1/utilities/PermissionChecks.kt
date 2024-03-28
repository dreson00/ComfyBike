package com.bk.bk1.utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

fun Context.hasLocationPermissions(): Boolean {
    val coarseLocationGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val fineLocationGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    return coarseLocationGranted && fineLocationGranted
}

fun Context.hasBluetoothPermissions(): Boolean {
    val bluetoothGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED

    val bluetoothAdminGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED

    val fineLocationGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val oldApiReturnValue = bluetoothGranted && bluetoothAdminGranted && fineLocationGranted

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val bluetoothScanGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

        val bluetoothConnectGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        return oldApiReturnValue && bluetoothScanGranted && bluetoothConnectGranted
    }

    return oldApiReturnValue
}

fun Context.hasBackgroundLocationAndNotificationPermissions(): Boolean {
    val foregroundServiceGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val postNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    return foregroundServiceGranted && postNotificationsGranted && backgroundLocationGranted
}

fun Context.hasExternalStoragePermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}