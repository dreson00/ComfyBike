@file:OptIn(ExperimentalAnimationApi::class, ExperimentalPermissionsApi::class)

package com.bk.bk1.compose


import android.os.Build
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.enums.ExportCsvStatus
import com.bk.bk1.models.TrackRecord
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.utilities.getExternalStoragePermissionList
import com.bk.bk1.utilities.parseFromDbFormat
import com.bk.bk1.viewModels.TrackListScreenViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

// Component that represents a screen with a list of all TrackRecords.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TrackListScreen(viewModel: TrackListScreenViewModel, navController: NavController) {

    val state by viewModel.state.collectAsState()

    // Permissions handling
    val externalStoragePermissionsState = rememberMultiplePermissionsState(
        permissions = getExternalStoragePermissionList()
    )

    val hasExternalStoragePermissions: () -> Boolean = {
        externalStoragePermissionsState.allPermissionsGranted
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    // CSV export handling
    if (state.exportCsvStatus != ExportCsvStatus.NOT_SAVING) {
        val message = when (state.exportCsvStatus) {
            ExportCsvStatus.SUCCESS -> {
                stringResource(R.string.info_csv_exp_succ)
            }
            ExportCsvStatus.FAILURE -> {
                stringResource(R.string.info_exp_err)
            }
            else -> { String() }
        }

        Toast.makeText(
            LocalContext.current,
            message,
            Toast.LENGTH_LONG
        ).show()

        viewModel.resetExportCsvStatus()
    }

    if (state.showExternalStorageRequest) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_storage),
            onConfirm = {
                externalStoragePermissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                viewModel.setShowExternalStorageRequest(false)
            }
        )
    }

    // Main container for this screen.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_track_list)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back)
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding),
            ) {
                if (state.trackRecords.isEmpty()) {
                    Text(
                        text = stringResource(R.string.info_no_tracks),
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                else {

                    // Displays a list of all TrackRecords.
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp, 0.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item() { }
                        items(state.trackRecords) { track ->
                            if (track.id != state.currentTrackId) {
                                TrackListItem(
                                    track = track,
                                    onTrackClick = {
                                        navController.navigate("trackDetailScreen/${track.id}")
                                    },
                                    onSaveTrackAsCSV = {
                                        if (hasExternalStoragePermissions()) {
                                            viewModel.saveCiRecordsAsCsv(track)
                                            false
                                        }
                                        else {
                                            viewModel.setShowExternalStorageRequest(true)
                                            true
                                        }
                                    },
                                    onSaveTrackAsImage = {
                                        if (hasExternalStoragePermissions()) {
                                            navController.navigate("mapScreenshotterScreen/${track.id}")
                                            false
                                        }
                                        else {
                                            viewModel.setShowExternalStorageRequest(true)
                                            true
                                        }
                                    },
                                    onDeleteTrackRecord = {
                                        viewModel.deleteTrack(track)
                                        false
                                    }
                                )
                            }
                        }
                        item() { }
                    }
                }
            }
        }
    )
}

// Component that represents a list item on this screen.
// Navigates user to the details screen when clicked.
// Has a button to allow the user to export this track or delete it.
@Composable
fun TrackListItem(
    track: TrackRecord,
    onTrackClick: () -> Unit,
    onSaveTrackAsCSV: () -> Boolean,
    onSaveTrackAsImage: () -> Boolean,
    onDeleteTrackRecord: () -> Boolean,
) {
    val cascadeState = rememberCascadeState()
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier
            .clickable { onTrackClick() }
            .height(70.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(12.dp, 10.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${stringResource(R.string.label_track_x)} ${track.id}"
                )
                Text(
                    text = parseFromDbFormat(track.time),
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
                    contentDescription = stringResource(R.string.btn_options),
                    tint = Color.Black)

                CascadeDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    state = cascadeState,
                    offset = DpOffset(x = 20.dp, y = (-10).dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.btn_export))
                        },
                        children = {
                            androidx.compose.material.DropdownMenuItem(
                                content = {
                                    Text(stringResource(R.string.btn_csv))
                                },
                                onClick = {
                                    expanded = onSaveTrackAsCSV()
                                }
                            )
                            androidx.compose.material.DropdownMenuItem(
                                content = {
                                    Text(stringResource(R.string.btn_img))
                                },
                                onClick =  {
                                    expanded = onSaveTrackAsImage()
                                }
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.btn_delete))
                        },
                        children = {
                            androidx.compose.material.DropdownMenuItem(
                                content = {
                                    Text(stringResource(R.string.btn_confirm))
                                },
                                onClick = {
                                    expanded = onDeleteTrackRecord()
                                }
                            )
                            androidx.compose.material.DropdownMenuItem(
                                content = {
                                    Text(stringResource(R.string.btn_cancel))
                                },
                                onClick = {
                                    cascadeState.navigateBack()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

// Testing previews.

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
