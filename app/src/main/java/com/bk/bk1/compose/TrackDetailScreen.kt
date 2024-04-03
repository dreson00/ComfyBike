@file:OptIn(ExperimentalMaterial3Api::class)

package com.bk.bk1.compose

import android.content.res.Configuration
import android.text.Layout
import android.text.TextUtils
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.viewModels.TrackDetailScreenViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.chart.zoom.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.columnSeries
import com.patrykandpatrick.vico.core.model.lineSeries
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun TrackDetailScreen(
    viewModel: TrackDetailScreenViewModel,
    navController: NavController,
    trackId: Int?
) {
    if (trackId == null) {
        navController.popBackStack()
    }
    val trackRecord = viewModel.getTrackRecordById(trackId!!).collectAsState(null)

    if (trackRecord.value == null) {
        return
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val comfortIndexRecords = viewModel
        .getComfortIndexRecordsByTrackId(trackId)
        .collectAsState(emptyList())
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        SheetState(
            skipPartiallyExpanded = false,
            density = LocalDensity.current
        )
    )
    val bottomSheetMove = remember { mutableStateOf(BottomSheetMoves.MovingDown) }
    val orientation = LocalConfiguration.current.orientation

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetSwipeEnabled = false,
        sheetDragHandle = null,
        sheetPeekHeight = 80.dp,
        sheetMaxWidth = 1000.dp,
        sheetShadowElevation = 0.dp,
        sheetTonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.background,
        sheetContainerColor = Color.Transparent,
        sheetContentColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("${stringResource(R.string.label_track_x)} $trackId") },
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
        content = {
            Column(
                modifier = Modifier
                    .padding(PaddingValues(15.dp, 15.dp, 15.dp, 5.dp))
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                TrackDetailCard {
                        val dbFormatter =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val date = dbFormatter.parse(trackRecord.value!!.time)
                        val outputFormatter =
                            SimpleDateFormat("d. MMMM yyyy HH:mm:ss", Locale.forLanguageTag("cs"))
                        val outputDateString = date?.let { outputFormatter.format(it) }
                        Text(
                            text = "${stringResource(R.string.record_time_x)} $outputDateString"
                        )
                        Text(
                            text = "${stringResource(R.string.record_count_x)} ${comfortIndexRecords.value.count()}"
                        )
                }
                TrackDetailCard {
                    TrackProgressLineChart(comfortIndexRecords)
                }
                TrackDetailCard {
                    CiDistributionColumnChart(comfortIndexRecords)
                }
            }
        },
        sheetContent = {
            val spacing: Dp by animateDpAsState(
                if (bottomSheetMove.value == BottomSheetMoves.MovingDown)
                    35.dp else (-70).dp, label = "Bottom sheet spacing animation",
                animationSpec = spring()
            )
            Column(
                modifier = Modifier
                    .background(Color.Transparent)
                    .fillMaxWidth()
                    .padding(0.dp, 10.dp, 0.dp, 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Row(
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .zIndex(2f)
                        .fillMaxWidth(),
                    horizontalArrangement =
                    if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Arrangement.End
                    }
                    else {
                        Arrangement.Center
                    }
                ) {
                    ElevatedButton(
                        modifier = Modifier
                            .size(58.dp)
                            .zIndex(2f),
                        shape = CircleShape,
                        contentPadding = PaddingValues(1.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = {
                            scope.launch {
                                toggleBottomSheet(
                                    bottomSheetScaffoldState.bottomSheetState,
                                    bottomSheetMove
                                )
                            }
                        }
                    ) {
                        Icon(
                            painterResource(R.drawable.outline_map_24),
                            contentDescription = stringResource(R.string.desc_toggle_map),
                            tint = Color.Black
                        )
                    }
                }
                if (comfortIndexRecords.value.isNotEmpty()) {
                    val recordsValue = comfortIndexRecords.value
                    val middleRecord = recordsValue[recordsValue.count() / 2]
                    LaunchedEffect(comfortIndexRecords.value.first()) {
                        cameraPositionState.animate(CameraUpdateFactory.zoomTo(17F))
                        cameraPositionState.animate(
                            CameraUpdateFactory
                                .newLatLng(LatLng(middleRecord.latitude, middleRecord.longitude))
                        )
                    }
                }
                GoogleMap(
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    cameraPositionState = cameraPositionState,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp))
                ) {
                    MapEffect { map ->
                        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_no_poi))
                    }
                    comfortIndexRecords.value.forEach { item ->
                        val color = Color.hsl(mapFloatToHue(item.comfortIndex), 1f, 0.5f)
                        MapMarker(
                            context = LocalContext.current,
                            position = LatLng(item.latitude, item.longitude),
                            title = "${stringResource(R.string.ci_x)}${item.comfortIndex}",
                            color = color,
                            iconResourceId = R.drawable.baseline_circle_12
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun TrackDetailCard(
    content: @Composable (() -> Unit)
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            content()
        }
    }
}

enum class BottomSheetMoves {
    MovingUp, MovingDown
}

@OptIn(ExperimentalMaterial3Api::class)
suspend fun toggleBottomSheet(
    sheetState: SheetState,
    move: MutableState<BottomSheetMoves>
) {
    if (sheetState.currentValue == SheetValue.Expanded) {
        move.value = BottomSheetMoves.MovingDown
        sheetState.partialExpand()
    }
    else if (sheetState.currentValue == SheetValue.Hidden || sheetState.currentValue == SheetValue.PartiallyExpanded) {
        move.value = BottomSheetMoves.MovingUp
        sheetState.expand()
    }
}

@Composable
//@Preview
fun LineChartCardPreview() {
    BK1Theme {
        val fakeRecords = remember { mutableListOf<ComfortIndexRecord>() }
        for (i in 0..30) {
            fakeRecords
                .add(
                    ComfortIndexRecord(
                        i.toLong(),
                        Random.nextDouble(0.0, 1.0).toFloat(),
                        trackRecordId = 1,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                )
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                val scrollState = rememberVicoScrollState()
                val zoomState = rememberVicoZoomState()
                val chartModelProducer = remember { CartesianChartModelProducer.build() }
                LaunchedEffect(Unit) {
                    chartModelProducer
                        .tryRunTransaction {
                            lineSeries {
                                series(
                                    fakeRecords.map { it.id },
                                    fakeRecords.map { it.comfortIndex }
                                )
                            }
                        }
                }
                CartesianChartHost(
                    scrollState = scrollState,
                    zoomState = zoomState,
                    chart =
                    rememberCartesianChart(
                        rememberLineCartesianLayer(
                            lines = listOf(
                                rememberLineSpec(
                                    shader = DynamicShaders.verticalGradient(
                                        arrayOf(Color.Green, Color.Yellow, Color.Red),
                                    ),
                                )
                            )
                        ),
                        startAxis = rememberStartAxis(
                            title = "CI",
                            titleComponent = rememberTextComponent(
                                background = rememberShapeComponent(
                                    shape = Shapes.pillShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ),
                                padding = dimensionsOf(5.dp, 4.dp)
                            ),
                            label = rememberAxisLabelComponent(
                                color = Color.Black
                            )
                        ),
                        bottomAxis = rememberBottomAxis(
                            title = "Čísla záznamů",
                            titleComponent = rememberTextComponent(
                                background = rememberShapeComponent(
                                    shape = Shapes.pillShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ),
                                padding = dimensionsOf(5.dp, 4.dp)
                            )
                        ),
                    ),
                    marker = rememberMarker(),
                    modelProducer = chartModelProducer
                )
            }
        }
    }
}

@Composable
//@Preview
fun ColumnChartCardPreview() {
    BK1Theme {
        val fakeRecords = remember { mutableListOf<ComfortIndexRecord>() }
        for (i in 0..100) {
            fakeRecords
                .add(
                    ComfortIndexRecord(
                        i.toLong(),
                        Random.nextDouble(0.0, 1.0).toFloat(),
                        trackRecordId = 1,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                )
        }
        val intervals = List(10) { it * 0.1f..(it + 1) * 0.1f }
        val counts = intervals.map { interval ->
            fakeRecords.count { it.comfortIndex in interval }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                val scrollState = rememberVicoScrollState()
                val zoomState = rememberVicoZoomState()
                val chartModelProducer = remember { CartesianChartModelProducer.build() }
                LaunchedEffect(Unit) {
                    chartModelProducer
                        .tryRunTransaction {
                            columnSeries {
                                series(
                                    List(10) { it+1 },
                                    counts
                                )
                            }
                        }
                }
                CartesianChartHost(
                    scrollState = scrollState,
                    zoomState = zoomState,
                    chart =
                    rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columns = listOf(
                                rememberLineComponent(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    10.dp,
                                    Shapes.roundedCornerShape(35)
                                )
                            ),
                            spacing = 40.dp,
                        ),
                        startAxis = rememberStartAxis(
                            title = "Počet záznamů",
                            titleComponent = rememberTextComponent(
                                background = rememberShapeComponent(
                                    shape = Shapes.pillShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ),
                                padding = dimensionsOf(5.dp, 4.dp)
                            ),
                            label = rememberAxisLabelComponent(
                                color = Color.Black
                            ),
                            valueFormatter = DecimalFormatAxisValueFormatter(roundingMode = RoundingMode.CEILING)
                        ),
                        bottomAxis = rememberBottomAxis(
                            title = "Rozsahy hodnot CI",
                            titleComponent = rememberTextComponent(
                                background = rememberShapeComponent(
                                    shape = Shapes.pillShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                padding = dimensionsOf(5.dp, 4.dp),
                                margins = dimensionsOf(top = 20.dp)
                            ),
                            valueFormatter = {
                                value, _, _ -> "<0.${value.toInt() - 1}, 0.${value.toInt()})"
                            },
                            labelRotationDegrees = 0f,
                            label = rememberAxisLabelComponent(
                                ellipsize = TextUtils.TruncateAt.MARQUEE,
                                textAlignment = Layout.Alignment.ALIGN_CENTER
                            ),
                        ),
                    ),
                    marker = rememberMarker(),
                    modelProducer = chartModelProducer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape")
//@Preview
fun TrackDetailScreenPreview() {
    BK1Theme {
        val trackId = 10
        val partiallyExpanded = false

        val scope = rememberCoroutineScope()
        val cameraPositionState = rememberCameraPositionState()


        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            SheetState(
                skipPartiallyExpanded = partiallyExpanded,
                density = LocalDensity.current
            )
        )
        val bottomSheetMove = remember { mutableStateOf(BottomSheetMoves.MovingDown) }

        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetSwipeEnabled = false,
            sheetDragHandle = null,
            sheetPeekHeight = 80.dp,
            sheetMaxWidth = 1000.dp,
            sheetShadowElevation = 0.dp,
            sheetTonalElevation = 0.dp,
            containerColor = MaterialTheme.colorScheme.background,
            sheetContainerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Trasa $trackId") },
                    navigationIcon = {
                        IconButton(
                            onClick = { }
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PaddingValues(15.dp, 15.dp, 15.dp, 70.dp)),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = "Čas záznamu: 23. 12. 2023"
                            )
                            Text(
                                text = "Počet záznamů: 34"
                            )
                        }
                    }

                }
            },
            sheetContent = {
                val spacing: Dp by animateDpAsState(
                    if (bottomSheetMove.value == BottomSheetMoves.MovingDown)
                        25.dp else (-70).dp, label = "Bottom sheet spacing animation",
                    animationSpec = spring()
                )
                Column(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(0.dp, 10.dp, 0.dp, 0.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .zIndex(2f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ElevatedButton(
                            modifier = Modifier
                                .size(48.dp)
                                .zIndex(2f),
                            shape = CircleShape,
                            contentPadding = PaddingValues(1.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            onClick = {
                                scope.launch {
                                    toggleBottomSheet(
                                        bottomSheetScaffoldState.bottomSheetState,
                                        bottomSheetMove
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.outline_map_24),
                                contentDescription = "Toggle map",
                                tint = Color.Black
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
//                        .navigationBarsPadding()
//                        .padding(12.dp)
                            .clip(shape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp))
                            .background(color = Color.Red)
                            .fillMaxWidth()
                            .fillMaxHeight()
                    )
                }

            },
        )
        LaunchedEffect(true) {
            scope.launch {
//                bottomSheetScaffoldState.bottomSheetState.expand()
            }
        }
    }
}
