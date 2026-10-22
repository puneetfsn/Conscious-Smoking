package com.cigcounter.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cigcounter.app.MainActivity
import com.cigcounter.app.R
import com.cigcounter.app.data.CigaretteRepository
import com.cigcounter.app.data.HabitSummary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CigWidget : GlanceAppWidget() {

    override suspend fun provideContent(context: Context, id: GlanceId) {
        // Retrieve database singleton via application Context
        val entryPoint = EntryPointAccess.get(context.applicationContext)
        val repository = entryPoint.repository

        val summary = runBlocking { repository.getHabitSummaryFlow().first() }

        provideContent {
            WidgetContent(context, summary)
        }
    }

    @Composable
    private fun WidgetContent(context: Context, summary: HabitSummary) {
        val backgroundColor = ColorProvider(android.R.color.background_dark)
        val textPrimary = ColorProvider(android.R.color.white)
        val textMuted = ColorProvider(android.R.color.darker_gray)
        val accentRed = ColorProvider(android.R.color.holo_red_light)
        val accentGreen = ColorProvider(android.R.color.holo_green_light)

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            // LEFT COLUMN: Compact, Beautiful circular Log Button
            Box(
                modifier = GlanceModifier
                    .size(54.dp)
                    .background(ImageProvider(R.drawable.bg_widget_plus))
                    .clickable(actionStartActivity<MainActivity>()), // Click to log/open
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            // RIGHT COLUMN: The Stats Panel (Entire column is clickable to open App)
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxSize()
                    .clickable(actionStartActivity<MainActivity>()), // Task 2: Click opens app
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today Summary row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today: ",
                        style = TextStyle(color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${summary.todayCount} cigs",
                        style = TextStyle(color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    )
                    if (summary.todaySkippedCount > 0) {
                        Text(
                            text = " (${summary.todaySkippedCount} saved)",
                            style = TextStyle(color = accentGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(3.dp))

                // Week summary row
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = "Week: ",
                        style = TextStyle(color = textMuted, fontSize = 11.sp)
                    )
                    Text(
                        text = "${summary.weekCount} cigs  ·  ${String.format("%.1f", summary.weekSpentUnits)} u",
                        style = TextStyle(color = textPrimary, fontSize = 11.sp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(3.dp))

                // Projections Row
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = "Pace: ",
                        style = TextStyle(color = textMuted, fontSize = 11.sp)
                    )
                    Text(
                        text = "≈${Math.round(summary.paceYearlyCigs)}/yr  ·  ${String.format("%.1f", summary.paceYearlyUnits)} u",
                        style = TextStyle(color = accentRed, fontSize = 11.sp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Bottom strip: Could have saved
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Could save: ",
                        style = TextStyle(color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${String.format("%.1f", summary.todaySavedUnits)} units today",
                        style = TextStyle(color = accentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

class CigWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CigWidget()
}
