@file:OptIn(ExperimentalAnimationApi::class, ExperimentalPermissionsApi::class)

package com.bk.bk1.compose


import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.utilities.getExternalStoragePermissionList
import com.bk.bk1.viewModels.TrackListScreenViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen(viewModel: TrackListScreenViewModel, navController: NavController) {

    val tracks by viewModel.tracks.observeAsState(emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val externalStoragePermissionsState = rememberMultiplePermissionsState(
        permissions = getExternalStoragePermissionList()
    )
    val showExternalStorageRequest = remember { mutableStateOf(false) }

    if (showExternalStorageRequest.value) {
        PermissionRequestDialog(
            messageText = "Pro exportování tras je nutné oprávnění pro přístup k úložišti.",
            onConfirm = {
                externalStoragePermissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                showExternalStorageRequest.value = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seznam tras") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tracks) { track ->
                        val state = rememberCascadeState()
                        var expanded by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .clickable { navController.navigate("trackDetailScreen/${track.id}") }
                                .height(70.dp)
                                .fillMaxWidth()
                                .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(White)
                                .padding(PaddingValues(12.dp, 10.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween

                        ) {
                            Column {
                                Text(
                                    text = "Trasa ${track.id}"
                                )
                                Text(
                                    text = track.time,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            ElevatedButton(
                                onClick = {
                                    expanded = !expanded
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(40.dp),
                                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp),
                                contentPadding = PaddingValues(1.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = White)

                            ) {
                                Icon(
                                    Icons.TwoTone.MoreVert,
                                    contentDescription = "Možnosti",
                                    tint = Color.Black)

                                CascadeDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    state = state,
                                    offset = DpOffset(x = 20.dp, y = (-10).dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = "Exportovat") },
                                        children = {
                                            androidx.compose.material.DropdownMenuItem(
                                                content = { Text(text = "CSV") },
                                                onClick = {
                                                    if (!externalStoragePermissionsState.allPermissionsGranted) {
                                                        scope.launch {
                                                            val success = viewModel.saveCiRecordsAsCsv(track)
                                                            val message = if (success == 0) {
                                                                "CSV soubor byl uložen do složky Documents/ComfyBike."
                                                            } else {
                                                                "Při exportu se vyskytla chyba."
                                                            }
                                                            Toast.makeText(
                                                                context,
                                                                message,
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                        expanded = !expanded
                                                    }
                                                    else {
                                                        showExternalStorageRequest.value = true
                                                    }
                                                }
                                            )
                                            androidx.compose.material.DropdownMenuItem(
                                                content = { Text(text = "Obrázek") },
                                                onClick =  {
                                                    if (!externalStoragePermissionsState.allPermissionsGranted) {
                                                        navController.navigate("mapScreenshotterScreen/${track.id}")
                                                    }
                                                    else {
                                                        showExternalStorageRequest.value = true
                                                    }
                                                }
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = "Smazat") },
                                        children = {
                                            androidx.compose.material.DropdownMenuItem(
                                                content = { Text(text = "Potvrdit") },
                                                onClick = {
                                                    viewModel.deleteTrack(track)
                                                    expanded = !expanded
                                                }
                                            )
                                            androidx.compose.material.DropdownMenuItem(
                                                content = { Text(text = "Zrušit") },
                                                onClick = {
                                                    state.navigateBack()
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun TrackListItemPreview() {
    BK1Theme(darkTheme = false) {
        Row(
            modifier = Modifier
                .clickable { }
                .height(70.dp)
                .fillMaxWidth()
                .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(White)
                .padding(PaddingValues(12.dp, 10.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "Trasa ${10}"
                )
                Text(
                    text = "2024-03-19 13:17:20",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            ElevatedButton(
                onClick = {

                },
                shape = CircleShape,
                modifier = Modifier
                    .size(40.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp),
                contentPadding = PaddingValues(1.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White)

            ) {
                Icon(
                    Icons.TwoTone.MoreVert,
                    contentDescription = "Možnosti",
                    tint = Color.Black
                )
                val state = rememberCascadeState()
                var expanded by remember { mutableStateOf(false) }
            }
        }
    }
}
