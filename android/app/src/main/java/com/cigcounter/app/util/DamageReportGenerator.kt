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
        liveSecondsSinceLast: Long? = null,
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
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            isAntiAlias = true
        }

        val boxPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        var y = 45f

        // Header
        canvas.drawText("SmokeLedger — Clinical Damage Report", 40f, y, titlePaint)
        y += 16f
        val nowStr = LocalDate.now(zoneId).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
        canvas.drawText("Generated on: $nowStr  •  Confidential Medical / Therapy Profile", 40f, y, subPaint)
        y += 24f

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
        canvas.drawRect(40f, y, 555f, y + 96f, boxPaint)
        canvas.drawText("30-DAY BEHAVIOUR SUMMARY", 55f, y + 20f, sectionPaint)

        canvas.drawText("Total Smoked (30 Days):", 55f, y + 38f, bodyPaint)
        canvas.drawText("$totalSmoked cigarettes (${String.format("%.1f", dailyAvg)}/day avg)", 250f, y + 38f, titlePaint.apply { textSize = 10.5f })

        canvas.drawText("Total Units Spent (30 Days):", 55f, y + 55f, bodyPaint)
        canvas.drawText("${CalculationUtils.formatUnits(totalSpent)} units", 250f, y + 55f, bodyPaint)

        canvas.drawText("Craving Resistances Logged:", 55f, y + 72f, bodyPaint)
        canvas.drawText("$totalSkipped cigarettes resisted (Ghost Counter)", 250f, y + 72f, bodyPaint)

        canvas.drawText("Estimated Tar / Nicotine Intake:", 55f, y + 89f, bodyPaint)
        canvas.drawText("Approx. ${totalSmoked * 11} mg tar · ${totalSmoked} mg nicotine", 250f, y + 89f, bodyPaint)

        y += 112f

        // Health Benefits & Physiological Recovery (Since Last Cigarette)
        canvas.drawText("PHYSIOLOGICAL RECOVERY & HEALTH BENEFITS (SINCE LAST SMOKE)", 40f, y, sectionPaint)
        y += 14f
        val elapsedSec = liveSecondsSinceLast ?: run {
            val lastSmoked = logs.filter { !it.isSkipped }.maxByOrNull { it.timestampMillis }
            if (lastSmoked != null) (System.currentTimeMillis() - lastSmoked.timestampMillis) / 1000L else null
        }
        val durationLabel = if (elapsedSec == null) "No smoke event recorded yet" else {
            val h = elapsedSec / 3600
            val m = (elapsedSec % 3600) / 60
            "${h}h ${m}m smoke-free"
        }
        canvas.drawText("Current Smoke-Free Duration: $durationLabel", 40f, y, subPaint)
        y += 16f

        val benefits = listOf(
            Triple("Heart Rate & Blood Pressure (20m)", 20 * 60L, "Pulse stabilizes & returns to resting baseline"),
            Triple("Carbon Monoxide Normalization (8h)", 8 * 3600L, "CO drops by 50%; blood oxygen normalizes"),
            Triple("Cardiac Risk & Mucus Clearance (24h)", 24 * 3600L, "Heart attack risk falls; lungs purge debris"),
            Triple("Nicotine Cleared & Nerve Regrowth (48h)", 48 * 3600L, "Nicotine eliminated; taste/smell senses sharpen"),
            Triple("Bronchial Tube Relaxation (72h)", 72 * 3600L, "Airway relaxes; functional lung capacity expands")
        )

        benefits.forEach { (name, targetSec, desc) ->
            val prog = if (elapsedSec == null) 0f else (elapsedSec.toFloat() / targetSec.toFloat()).coerceIn(0f, 1f)
            val isRestored = prog >= 1f
            val percent = (prog * 100).toInt()
            val status = if (isRestored) "[RESTORED 100%]" else "[IN PROGRESS $percent%]"

            val statPaint = Paint(bodyPaint).apply {
                color = if (isRestored) Color.rgb(22, 101, 52) else Color.rgb(3, 105, 161)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 9f
            }
            canvas.drawText(status, 45f, y, statPaint)
            canvas.drawText(name, 165f, y, titlePaint.apply { textSize = 9.5f })
            canvas.drawText(desc, 345f, y, subPaint.apply { textSize = 8.5f })
            y += 14f
        }

        y += 18f

        // "The Mirror" — 1-Year Pace Projection
        canvas.drawText("THE MIRROR — 1-YEAR PACE PROJECTION", 40f, y, sectionPaint)
        y += 14f
        canvas.drawText(
            "If current 30-day habits continue without change over the next 12 months:",
            40f, y, subPaint
        )
        y += 15f
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

        // Save PDF to cache directory
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        } else {
            // Purge previous reports to prevent cache bloat
            reportsDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("SmokeLedger_Damage_Report")) {
                    file.delete()
                }
            }
        }
        val outputFile = File(reportsDir, "SmokeLedger_Damage_Report.pdf")

        try {
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            return outputFile
        } finally {
            pdfDocument.close()
        }
    }
}
