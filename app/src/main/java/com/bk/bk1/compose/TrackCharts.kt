package com.bk.bk1.compose

import android.text.Layout
import android.text.TextUtils
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bk.bk1.R
import com.bk.bk1.models.ComfortIndexRecord
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
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.columnSeries
import com.patrykandpatrick.vico.core.model.lineSeries
import java.math.RoundingMode

@Composable
fun CiDistributionColumnChart(comfortIndexRecords: List<ComfortIndexRecord>) {
    val scrollState = rememberVicoScrollState()
    val zoomState = rememberVicoZoomState(zoomEnabled = false)
    val intervals = List(10) { it * 0.1f..(it + 1) * 0.1f }
    val counts = intervals.map { interval ->
        comfortIndexRecords.count { it.comfortIndex in interval }
    }

    val chartModelProducer = remember { CartesianChartModelProducer.build() }
    if (comfortIndexRecords.isNotEmpty()) {
        LaunchedEffect(comfortIndexRecords) {
            chartModelProducer
                .tryRunTransaction {
                    columnSeries {
                        series(
                            List(10) { it + 1 },
                            counts
                        )
                    }
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
                title = stringResource(R.string.graph_label_record_count),
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
                title = stringResource(R.string.graph_label_ci_val_ranges),
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
                    textAlignment = Layout.Alignment.ALIGN_CENTER,
                    color = Color.Black
                ),
            ),
        ),
        marker = rememberMarker(),
        modelProducer = chartModelProducer
    )
}

@Composable
fun TrackProgressLineChart(comfortIndexRecords: List<ComfortIndexRecord>) {
    val scrollState = rememberVicoScrollState()
    val zoomState = rememberVicoZoomState()
    val chartModelProducer = remember { CartesianChartModelProducer.build() }
    if (comfortIndexRecords.isNotEmpty()) {
        LaunchedEffect(comfortIndexRecords) {
            chartModelProducer
                .tryRunTransaction {
                    lineSeries {
                        series(
                            comfortIndexRecords.map { it.id - comfortIndexRecords.first().id + 1 },
                            comfortIndexRecords.map { it.comfortIndex }
                        )
                    }
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
                        thickness = 4.dp
                    )
                )
            ),
            startAxis = rememberStartAxis(
                title = stringResource(R.string.graph_label_ci),
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
                title = stringResource(R.string.graph_label_record_rank),
                titleComponent = rememberTextComponent(
                    background = rememberShapeComponent(
                        shape = Shapes.pillShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ),
                    padding = dimensionsOf(5.dp, 4.dp),
                ),
                label = rememberAxisLabelComponent(
                    color = Color.Black
                ),
                itemPlacer = if (zoomState.value <= 1) {
                    AxisItemPlacer
                        .Horizontal
                        .default(spacing = (1 + (10 - 1) * (1 - zoomState.value)).toInt())
                }
                else {
                    AxisItemPlacer.Horizontal.default()
                }
            ),
        ),
        marker = rememberMarker(),
        modelProducer = chartModelProducer
    )
}