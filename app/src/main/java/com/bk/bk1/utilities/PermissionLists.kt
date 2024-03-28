package com.bk.bk1.utilities

import android.Manifest
import android.os.Build


fun getLocationPermissionList(): List<String> {
    return listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

fun getBluetoothPermissionList(): List<String> {
    val permissionList = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissionList.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissionList.add(Manifest.permission.BLUETOOTH_SCAN)
    }
    return permissionList
}

fun getBackgroundLocationServicePermissionList(): List<String> {
    val permissionList = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        permissionList.add(Manifest.permission.FOREGROUND_SERVICE)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    return permissionList
}

fun getPostNotificationsPermissionList(): List<String> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return listOf(Manifest.permission.POST_NOTIFICATIONS)
    }
    return emptyList()
}

fun getExternalStoragePermissionList(): List<String> {
    return listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}