package com.bk.bk1.compose

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bk.bk1.utilities.BluetoothScanService
import com.bk.bk1.viewModels.SensorConnectScreenViewModel

@SuppressLint("MissingPermission")
@Composable
fun SensorConnectScreen(navController: NavController , btService: BluetoothScanService) {
    val viewModel = viewModel<SensorConnectScreenViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SensorConnectScreenViewModel(btService) as T
            }
        }
    )
    viewModel.startScan()
    val devices by viewModel.devicesLiveData.observeAsState(initial = emptyList())
    var number by remember { mutableIntStateOf(1) }

    BackHandler(enabled = true) {
        navController.navigate("mapScreen")
        number++
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(){
            Button(onClick = {
//                number++
                navController.popBackStack()
                println("stisknuto")
            }) {
                Text("Zvýšit číslo")
            }
            Text(number.toString())
        }

//        LazyColumn(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            items(devices) { device ->
//                Box(
//                    modifier = Modifier.height(50.dp)
//                ){
//                    Text(device.name.toString())
//                }
//            }
//        }
    }
}