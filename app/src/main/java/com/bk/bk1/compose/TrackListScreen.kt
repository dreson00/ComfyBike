@file:OptIn(ExperimentalAnimationApi::class)

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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.twotone.MoreVert
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
import com.bk.bk1.viewModels.TrackListScreenViewModel
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

@Composable
fun TrackListScreen(viewModel: TrackListScreenViewModel, navController: NavController) {

    val tracks by viewModel.tracks.observeAsState(emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                                .clickable { }
                                .height(70.dp)
                                .fillMaxWidth()
                                .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(White)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween

                        ) {
                            Column {
                                Text("Trasa ${track.id}")
                                Text(track.time)
                            }
                            Button(
                                onClick = {
                                    expanded = !expanded
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(40.dp),
                                contentPadding = PaddingValues(1.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = White)

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
                                                    scope.launch {
                                                        val success = viewModel.saveCiRecordsAsCsv(track)
                                                        var message = if (success == 0) {
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
                                            )
                                            androidx.compose.material.DropdownMenuItem(
                                                content = { Text(text = "Obrázek") },
                                                onClick =  { navController.navigate("mapScreenshotterScreen/${track.id}") }
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
    Row(
        modifier = Modifier
            .clickable { }
            .height(70.dp)
            .fillMaxWidth()
            .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

        ) {
        Column {
            Text("Trasa ${10}")
            Text("2024-03-19 13:17:20")
        }
        Button(
            onClick = {

            },
            shape = CircleShape,
            modifier = Modifier
                .size(40.dp),
            contentPadding = PaddingValues(1.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = White)

            ) {
            Icon(
                Icons.TwoTone.MoreVert,
                contentDescription = "Možnosti",
                tint = Color.Black)
            val state = rememberCascadeState()
            var expanded by remember { mutableStateOf(false) }
            CascadeDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                state = state
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Exportovat") },
                    children = {
                        DropdownMenuItem(
                            text = { Text(text = "CSV") },
                            children = { },
                            modifier = Modifier
                                .clickable {

                                }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Obrázek") },
                            children = { },
                            modifier = Modifier
                                .clickable {

                                }
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Smazat") },
                    children = {
                        DropdownMenuItem(
                            text = { Text(text = "Potvrdit") },
                            children = { },
                            modifier = Modifier
                                .clickable {

                                }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Zrušit") },
                            children = { },
                            modifier = Modifier
                                .clickable {
                                    state.navigateBack()
                                }
                        )
                    }
                )
            }
        }
    }
}