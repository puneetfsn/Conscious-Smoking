package com.cigcounter.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cigcounter.app.data.CigaretteLog
import com.cigcounter.app.data.DayStat
import com.cigcounter.app.data.HourlyTriggerData
import com.cigcounter.app.ui.theme.AccentCoral
import com.cigcounter.app.ui.theme.DarkOutline
import com.cigcounter.app.ui.theme.DarkSurface
import com.cigcounter.app.ui.theme.DarkSurfaceVariant
import com.cigcounter.app.ui.theme.LocalIsDarkTheme
import com.cigcounter.app.ui.theme.TextMuted
import com.cigcounter.app.ui.theme.TextPrimary
import com.cigcounter.app.ui.theme.TextSecondary
import com.cigcounter.app.util.CalculationUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    dailyStats: List<DayStat>,
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit,
    recentLogs: List<CigaretteLog>,
    hourlyTriggerData: HourlyTriggerData = HourlyTriggerData(),
    modifier: Modifier = Modifier
) {
    val totalCount = dailyStats.sumOf { it.count }
    val totalSpent = dailyStats.sumOf { it.spentUnits }
    val maxCount = dailyStats.maxOfOrNull { it.count } ?: 0
    val avgCount = if (dailyStats.isNotEmpty()) totalCount.toDouble() / dailyStats.size else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "History & Trends",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Track your daily patterns over time.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Period selector tabs: 7 Days vs 30 Days
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(4.dp)
            ) {
                PeriodTab(
                    text = "Last 7 Days",
                    isSelected = selectedDays == 7,
                    onClick = { onDaysSelected(7) },
                    modifier = Modifier.weight(1f)
                )
                PeriodTab(
                    text = "Last 30 Days",
                    isSelected = selectedDays == 30,
                    onClick = { onDaysSelected(30) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Bar Chart Card (drawn with pure Compose Canvas)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Count",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Avg: ${String.format("%.1f", avgCount)}/day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentCoral
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ComposeBarChart(
                        stats = dailyStats,
                        maxCount = maxCount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }

        // Summary Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Total logged",
                    value = "$totalCount",
                    sub = "in $selectedDays days",
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Total impact",
                    value = CalculationUtils.formatUnits(totalSpent),
                    sub = "units spent",
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Peak day",
                    value = "$maxCount",
                    sub = "cigarettes",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 24-Hour Trigger Hour / Heatmap
        item {
            TriggerHourHeatmapCard(
                triggerData = hourlyTriggerData,
                selectedDays = selectedDays
            )
        }

        // Recent Logs Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Logs",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (recentLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No logs recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(recentLogs) { log ->
                LogItemRow(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ComposeBarChart(
    stats: List<DayStat>,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val barProgress = remember { Animatable(0f) }

    LaunchedEffect(stats) {
        barProgress.snapTo(0f)
        barProgress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    }

    val displayMax = maxOf(maxCount, 5)
    val baselineColor = DarkOutline
    val barColor = AccentCoral
    val barInactiveColor = DarkSurfaceVariant
    val isDark = LocalIsDarkTheme.current
    val textPaintColor = if (isDark) android.graphics.Color.parseColor("#A1A1AA") else android.graphics.Color.parseColor("#64748B")
    val countPaintColor = if (isDark) android.graphics.Color.parseColor("#EDEDED") else android.graphics.Color.parseColor("#0F172A")

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val bottomMargin = 40f
        val topMargin = 30f
        val chartHeight = height - bottomMargin - topMargin
        val barCount = stats.size

        if (barCount == 0) return@Canvas

        val availableWidth = width
        val slotWidth = availableWidth / barCount
        val barWidth = (slotWidth * 0.65f).coerceIn(4f, 28f)

        // Draw baseline axis
        drawLine(
            color = baselineColor,
            start = Offset(0f, height - bottomMargin),
            end = Offset(width, height - bottomMargin),
            strokeWidth = 2f
        )

        val textPaint = android.graphics.Paint().apply {
            color = textPaintColor
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val countPaint = android.graphics.Paint().apply {
            color = countPaintColor
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        stats.forEachIndexed { index, stat ->
            val centerX = (index * slotWidth) + (slotWidth / 2f)
            val left = centerX - (barWidth / 2f)
            val right = centerX + (barWidth / 2f)

            val barHeightFraction = (stat.count.toFloat() / displayMax.toFloat())
            val barPixelHeight = chartHeight * barHeightFraction * barProgress.value
            val top = height - bottomMargin - barPixelHeight

            // Draw Bar
            drawRoundRect(
                color = if (stat.count > 0) barColor else barInactiveColor,
                topLeft = Offset(left, if (stat.count > 0) top else height - bottomMargin - 4f),
                size = Size(barWidth, if (stat.count > 0) barPixelHeight else 4f),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Draw Count on top if count > 0 and 7-day view or spaced enough
            if (stat.count > 0 && (barCount <= 14 || index % 3 == 0)) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${stat.count}",
                    centerX,
                    top - 8f,
                    countPaint
                )
            }

            // Draw Date label under baseline
            if (barCount <= 7 || index % 5 == 0 || index == barCount - 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    stat.label,
                    centerX,
                    height - 10f,
                    textPaint
                )
            }
        }
    }
}

@Composable
private fun PeriodTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AccentCoral else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LogItemRow(log: CigaretteLog) {
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(log.timestampMillis),
        ZoneId.systemDefault()
    )
    val timeFormatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val dateFormatted = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentCoral)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "1 cigarette logged",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun TriggerHourHeatmapCard(
    triggerData: HourlyTriggerData,
    selectedDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = AccentCoral,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "24-Hour Trigger Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Last $selectedDays days",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Identifies exactly when cravings strike for actionable self-knowledge.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Peak Trigger Callout
            if (triggerData.peakHour != null && triggerData.peakCount > 0) {
                val peakH = triggerData.peakHour
                val nextH = (peakH + 1) % 24
                val timeLabel = String.format("%02d:00 - %02d:00", peakH, nextH)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = AccentCoral,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Peak Trigger Window: ",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                            Text(
                                text = timeLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentCoral
                            )
                        }
                        Text(
                            text = "${triggerData.peakCount} cigarettes logged here. Plan a healthy distraction before this hour.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "No cravings or logs recorded in the selected period.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 24-Hour Grid: 4 rows of 6 hours
            val timeBlocks = listOf(
                "Night (00:00 - 05:00)" to (0..5),
                "Morning (06:00 - 11:00)" to (6..11),
                "Afternoon (12:00 - 17:00)" to (12..17),
                "Evening (18:00 - 23:00)" to (18..23)
            )

            val maxVal = maxOf(triggerData.peakCount, 1)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                timeBlocks.forEach { (blockTitle, hourRange) ->
                    Column {
                        Text(
                            text = blockTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            hourRange.forEach { hour ->
                                val count = triggerData.hourCounts.getOrElse(hour) { 0 }
                                val intensity = if (count > 0) (count.toFloat() / maxVal).coerceIn(0f, 1f) else 0f

                                val cellBg = if (count == 0) {
                                    DarkSurfaceVariant
                                } else {
                                    AccentCoral.copy(alpha = 0.25f + 0.75f * intensity)
                                }

                                val isPeak = triggerData.peakHour == hour && count > 0

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(cellBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d", hour),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = if (count > 0 && intensity > 0.5f) Color.White else TextMuted
                                        )
                                        Text(
                                            text = if (count > 0) "$count" else "·",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            fontWeight = if (isPeak) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (count > 0) (if (intensity > 0.5f) Color.White else AccentCoral) else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Low", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted)
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(DarkSurfaceVariant))
                Spacer(modifier = Modifier.width(2.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentCoral.copy(alpha = 0.35f)))
                Spacer(modifier = Modifier.width(2.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentCoral.copy(alpha = 0.70f)))
                Spacer(modifier = Modifier.width(2.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentCoral))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "High", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted)
            }
        }
    }
}
