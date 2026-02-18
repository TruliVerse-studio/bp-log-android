package com.example.bplog.ui.screen

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bplog.data.Measurement
import com.example.bplog.ui.HistoryFilter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun BpLineChart(
    measurements: List<Measurement>,
    filter: HistoryFilter,
    modifier: Modifier = Modifier,
    onChartReady: (LineChart) -> Unit = {}
) {
    val chartData = measurements
        .reversed()
        .mapIndexed { index, m -> index to m }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.setLabelCount(6, false)
                xAxis.labelRotationAngle = if (filter == HistoryFilter.ALL) 60f else 0f

                axisLeft.setAxisMinimum(40f)
                axisLeft.setAxisMaximum(240f)
                axisLeft.setDrawGridLines(true)

                axisRight.isEnabled = true
                axisRight.axisMinimum = 40f
                axisRight.axisMaximum = 200f
                axisRight.setDrawGridLines(false)

                legend.isEnabled = true
                legend.textSize = 14f
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.setDrawInside(false)

                onChartReady(this)
            }
        },
        update = { chart ->
            if (chartData.isEmpty()) {
                chart.data = null
                chart.clear()
                chart.notifyDataSetChanged()
                chart.invalidate()
                return@AndroidView
            }

            chart.xAxis.labelRotationAngle = if (filter == HistoryFilter.ALL) 60f else 0f
            chart.xAxis.setLabelCount(6, false)

            chart.axisLeft.setAxisMinimum(40f)
            chart.axisLeft.setAxisMaximum(240f)

            val pattern = when (filter) {
                HistoryFilter.DAYS_7, HistoryFilter.DAYS_30 -> "MM.dd"
                HistoryFilter.ALL -> "yyyy.MM.dd"
            }

            val xDateFormat = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                chartData.map { (_, m) ->
                    xDateFormat.format(java.util.Date(m.timestamp))
                }
            )

            val sysEntries = chartData.map { (i, m) -> Entry(i.toFloat(), m.sys.toFloat()) }
            val diaEntries = chartData.map { (i, m) -> Entry(i.toFloat(), m.dia.toFloat()) }
            val pulseEntries = chartData.map { (i, m) -> Entry(i.toFloat(), m.pulse.toFloat()) }

            val sysDataSet = LineDataSet(sysEntries, "SYS(mmHg)").apply {
                color = Color.parseColor("#1f77b4")
                setCircleColor(Color.parseColor("#1f77b4"))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawCircles(true)
                setDrawCircleHole(false) 
                setDrawValues(false)
                mode = LineDataSet.Mode.LINEAR
                
            }

            val diaDataSet = LineDataSet(diaEntries, "DIA(mmHg)").apply {
                color = Color.parseColor("#555555")
                setCircleColor(Color.parseColor("#555555"))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawCircles(true)
                setDrawCircleHole(false)
                setDrawValues(false)               
                enableDashedLine(12f, 8f, 0f)
                mode = LineDataSet.Mode.LINEAR
            }

            val pulseDataSet = LineDataSet(pulseEntries, "PULSE(bpm)").apply {
                color = Color.parseColor("#de9c50")
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                enableDashedLine(6f, 6f, 0f)
                axisDependency = YAxis.AxisDependency.RIGHT
                mode = LineDataSet.Mode.LINEAR
            }

            chart.data = LineData(sysDataSet, diaDataSet, pulseDataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}
