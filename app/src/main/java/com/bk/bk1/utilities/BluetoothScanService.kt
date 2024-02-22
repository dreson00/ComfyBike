package com.bk.bk1.utilities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.MutableLiveData

class BluetoothScanService(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val leScanner = bluetoothAdapter?.bluetoothLeScanner
    private var scanCallback: ScanCallback? = null

    val devicesLiveData: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()

    @SuppressLint("MissingPermission")
    fun startScan() {
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val newDeviceList = devicesLiveData.value?.toMutableList() ?: mutableListOf()
                val isDeviceInList = devicesLiveData.value?.contains(result.device)
                if((isDeviceInList == null || isDeviceInList == false)
                    && result.device.name != null
                    && result.device.name.startsWith("Movesense")) {
                    newDeviceList.add(result.device)
                    devicesLiveData.value = newDeviceList
                }
            }
        }

        leScanner?.startScan(scanCallback)
    }
}