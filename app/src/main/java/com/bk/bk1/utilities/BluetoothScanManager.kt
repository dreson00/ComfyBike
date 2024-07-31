package com.bk.bk1.utilities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
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
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                results?.let {
                    val deviceList = results
                        .filter {
                            it.device.name != null && it.device.name.startsWith("Movesense")
                        }
                        .map { it.device }

                    devicesLiveData.postValue(deviceList)
                }
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setReportDelay(500)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        leScanner?.startScan(listOf(), scanSettings, scanCallback)
    }
    @SuppressLint("MissingPermission")
    fun stopScan() {
        leScanner?.stopScan(scanCallback)
        devicesLiveData.postValue(emptyList())
    }
}