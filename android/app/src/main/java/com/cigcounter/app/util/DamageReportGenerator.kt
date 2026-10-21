package com.cigcounter.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.cigcounter.app.data.CigaretteLog
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Generates an offline "Damage Report" PDF for sharing with a doctor or therapist.
 * Uses native Android PdfDocument without any external libraries.
 */
object DamageReportGenerator {

    fun generatePdfReport(
        context: Context,
        logs: List<CigaretteLog>,
        pricePerUnit: Double,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard point size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(220, 50, 20)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val boxPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        var y = 50f

        // Header
        canvas.drawText("SmokeLedger — Clinical Damage Report", 40f, y, titlePaint)
        y += 18f
        val nowStr = LocalDate.now(zoneId).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
        canvas.drawText("Generated on: $nowStr  •  Confidential Medical / Therapy Profile", 40f, y, subPaint)
        y += 28f

        // Calculations for last 30 days
        val thirtyDaysAgo = DateRangeUtils.getMonthStartMillis(zoneId)
        val smokedLogs = logs.filter { !it.isSkipped && it.timestampMillis >= thirtyDaysAgo }
        val skippedLogs = logs.filter { it.isSkipped && it.timestampMillis >= thirtyDaysAgo }

        val totalSmoked = smokedLogs.size
        val totalSkipped = skippedLogs.size
        val totalSpent = totalSmoked * pricePerUnit
        val dailyAvg = totalSmoked / 30.0
        val yearlyProjectedCigs = dailyAvg * 365.0
        val yearlyProjectedUnits = yearlyProjectedCigs * pricePerUnit

        // Metric Box
        canvas.drawRect(40f, y, 555f, y + 105f, boxPaint)
        canvas.drawText("30-DAY BEHAVIOUR SUMMARY", 55f, y + 22f, sectionPaint)

        canvas.drawText("Total Smoked (30 Days):", 55f, y + 42f, bodyPaint)
        canvas.drawText("$totalSmoked cigarettes (${String.format("%.1f", dailyAvg)}/day avg)", 250f, y + 42f, titlePaint.apply { textSize = 11f })

        canvas.drawText("Total Units Spent (30 Days):", 55f, y + 60f, bodyPaint)
        canvas.drawText("${CalculationUtils.formatUnits(totalSpent)} units", 250f, y + 60f, bodyPaint)

        canvas.drawText("Craving Resistances Logged:", 55f, y + 78f, bodyPaint)
        canvas.drawText("$totalSkipped cigarettes resisted (Ghost Counter)", 250f, y + 78f, bodyPaint)

        canvas.drawText("Estimated Tar / Nicotine Intake:", 55f, y + 96f, bodyPaint)
        canvas.drawText("Approx. ${totalSmoked * 11} mg tar · ${totalSmoked} mg nicotine", 250f, y + 96f, bodyPaint)

        y += 130f

        // "The Mirror" — 1-Year Pace Projection
        canvas.drawText("THE MIRROR — 1-YEAR PACE PROJECTION", 40f, y, sectionPaint)
        y += 16f
        canvas.drawText(
            "If current 30-day habits continue without change over the next 12 months:",
            40f, y, subPaint
        )
        y += 18f
        canvas.drawText(
            "• Projected annual consumption: ${String.format("%.0f", yearlyProjectedCigs)} cigarettes",
            45f, y, bodyPaint
        )
        y += 16f
        canvas.drawText(
            "• Projected financial impact: ${CalculationUtils.formatUnits(yearlyProjectedUnits)} units spent",
            45f, y, bodyPaint
        )
        y += 16f
        canvas.drawText(
            "• Potential savings if reduced by 2 cigs/day: ${CalculationUtils.formatUnits(2.0 * 365.0 * pricePerUnit)} units/year",
            45f, y, bodyPaint
        )

        y += 36f

        // Recent 7 days breakdown table
        canvas.drawText("LAST 7 DAYS BREAKDOWN", 40f, y, sectionPaint)
        y += 16f

        canvas.drawText("Date", 50f, y, subPaint)
        canvas.drawText("Smoked", 180f, y, subPaint)
        canvas.drawText("Resisted (Ghost)", 280f, y, subPaint)
        canvas.drawText("Spent Units", 420f, y, subPaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, boxPaint)
        y += 15f

        val windows = DateRangeUtils.getDailyTimeWindows(7, zoneId)
        windows.reversed().forEach { window ->
            val daySmoked = logs.count { !it.isSkipped && it.timestampMillis in window.startMillis..window.endMillis }
            val daySkipped = logs.count { it.isSkipped && it.timestampMillis in window.startMillis..window.endMillis }
            val dateLabel = window.date.format(DateTimeFormatter.ofPattern("EEE, MMM dd"))

            canvas.drawText(dateLabel, 50f, y, bodyPaint)
            canvas.drawText("$daySmoked cigs", 180f, y, bodyPaint)
            canvas.drawText("$daySkipped avoided", 280f, y, bodyPaint)
            canvas.drawText(CalculationUtils.formatUnits(daySmoked * pricePerUnit), 420f, y, bodyPaint)
            y += 18f
        }

        y += 30f
        canvas.drawText(
            "Doctor/Therapist Notes & Observations:",
            40f, y, sectionPaint
        )
        y += 15f
        for (i in 0..4) {
            canvas.drawLine(40f, y + (i * 24), 555f, y + (i * 24), boxPaint)
        }

        pdfDocument.finishPage(page)

        // Save PDF to cache or documents
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val outputFile = File(reportsDir, "SmokeLedger_Damage_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }
}
