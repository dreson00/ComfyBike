package com.bk.bk1.utilities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject


// Class that uses the Bluetooth adapter to find nearby sensors.
class BluetoothScanManager @Inject constructor(
    private val bluetoothManager: BluetoothManager
) {
    val devicesLiveData: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    private var scanCallback: ScanCallback? = null
    private var leScanner: BluetoothLeScanner? = null

    @SuppressLint("MissingPermission")
    fun startScan() {
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        leScanner = bluetoothAdapter?.bluetoothLeScanner
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val newDeviceList = devicesLiveData.value?.toMutableList() ?: mutableListOf()
                val isDeviceInList = devicesLiveData.value?.contains(result.device)
                if ((isDeviceInList == null || isDeviceInList == false)
                    && result.device.name != null
                    && result.device.name.startsWith("Movesense")
                ) {
                    newDeviceList.add(result.device)
                    devicesLiveData.value = newDeviceList
                }
            }

        }

        leScanner?.startScan(scanCallback)
    }
    @SuppressLint("MissingPermission")
    fun stopScan() {
        leScanner?.stopScan(scanCallback)
        devicesLiveData.postValue(emptyList())
    }
}