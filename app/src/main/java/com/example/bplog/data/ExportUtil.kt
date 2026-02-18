package com.example.bplog.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId

object ExportUtil {

    fun exportCsv(context: Context, measurements: List<Measurement>): Uri? {
        val fileName = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date()) + "_BPLog.csv"

        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/BPLog"
            )
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: return null

        resolver.openOutputStream(uri)?.use { output ->
            val writer = OutputStreamWriter(output)

            writer.appendLine("timestamp,date,time,sys,dia,pulse")

            val zone = ZoneId.systemDefault()

            measurements.forEach { m ->
                val dt = Instant.ofEpochMilli(m.timestamp).atZone(zone)
                writer.appendLine(
                    "${m.timestamp},${dt.toLocalDate()},${dt.toLocalTime()},${m.sys},${m.dia},${m.pulse}"
                )
            }

            writer.flush()
        }

        return uri
    }

    fun exportPdf(
        context: Context,
        chartBitmap: Bitmap,
        titleSuffix: String,
        measurements: List<Measurement>
    ): Uri? {
        val fileName = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date()) + "_BPLog.pdf"

        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/BPLog"
            )
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: return null

        val pdf = PdfDocument()

        // A4 Landscape in points (approx): 842 x 595
        val pageWidth = 842
        val pageHeight = 595

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
        }

        // Title
        canvas.drawText("Blood Pressure Log ($titleSuffix)", 40f, 40f, paint)

        // Layout
        val margin = 40
        val top = 70
        val gap = 20

        val contentW = pageWidth - margin * 2
        val contentH = pageHeight - top - margin

        // Split: left chart, right list
        val listW = 240                 // Width for the list on the right
        val chartW = contentW - gap - listW

        // ----- Draw chart (left) -----
        val bmpW = chartBitmap.width
        val bmpH = chartBitmap.height

        val scale = minOf(
            chartW.toFloat() / bmpW.toFloat(),
            contentH.toFloat() / bmpH.toFloat()
        )

        val drawW = (bmpW * scale).toInt()
        val drawH = (bmpH * scale).toInt()

        val chartX = margin + (chartW - drawW) / 2
        val chartY = top + (contentH - drawH) / 2

        canvas.drawBitmap(
            Bitmap.createScaledBitmap(chartBitmap, drawW, drawH, true),
                chartX.toFloat(),
            chartY.toFloat(),
            null
        )

        // ----- Draw list (right) -----
        val listX = (margin + chartW + gap + 40).toFloat()
        var textY = top.toFloat()

        val zone = ZoneId.systemDefault()
        val fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f }

        canvas.drawText("Entries", listX, textY, headerPaint)
        textY += 16f

        val maxRows = ((pageHeight - margin - textY) / 14f).toInt().coerceAtLeast(0)

        measurements
            .sortedByDescending { it.timestamp }
            .take(maxRows.coerceAtMost(26))
            .forEach { m ->
                val dt = Instant.ofEpochMilli(m.timestamp).atZone(zone).toLocalDateTime().format(fmt)
                val line = "$dt  ${m.sys}/${m.dia}  ${m.pulse}"
                canvas.drawText(line, listX, textY, rowPaint)
                textY += 14f
            }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
        }

        canvas.drawText(
            "Generated by BPLog — TruliVerse",
            margin.toFloat(),
            (pageHeight - 20).toFloat(),
            footerPaint
        )

        pdf.finishPage(page)

        resolver.openOutputStream(uri)?.use { out ->
            pdf.writeTo(out)
        }
        
        pdf.close()

        return uri
    }

}