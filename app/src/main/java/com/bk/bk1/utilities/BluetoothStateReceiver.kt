package com.bk.bk1.utilities

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import javax.inject.Inject

// Class that contains an updater so that the app can react to Bluetooth adapter changes.
class BluetoothStateReceiver @Inject constructor(
    private val bluetoothStateUpdater: BluetoothStateUpdater
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            bluetoothStateUpdater.updateBluetoothState(state == BluetoothAdapter.STATE_ON)
        }
    }
}