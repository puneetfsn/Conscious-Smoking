package com.cigcounter.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cigcounter.app.CigApp
import com.cigcounter.app.MainActivity
import com.cigcounter.app.data.HabitSummary
import com.cigcounter.app.util.CalculationUtils

class CigWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as CigApp
        val summary = runCatching {
            app.repository.getHabitSummarySync()
        }.getOrDefault(HabitSummary())

        val settings = runCatching {
            app.preferencesRepository.getSettings()
        }.getOrDefault(com.cigcounter.app.data.UserSettings())

        provideContent {
            NothingStyleWidgetContent(summary = summary, isWhiteTheme = settings.widgetTheme == "NOTHING_WHITE")
        }
    }

    @Composable
    private fun NothingStyleWidgetContent(summary: HabitSummary, isWhiteTheme: Boolean) {
        // Nothing OS Theme Palettes
        val backgroundColor = if (isWhiteTheme) ColorProvider(Color(0xFFF4F4F5)) else ColorProvider(Color(0xFF0F0F11))
        val surfaceColor = if (isWhiteTheme) ColorProvider(Color(0xFFE4E4E7)) else ColorProvider(Color(0xFF1E1E22))
        val primaryAccent = ColorProvider(Color(0xFFFF3B30)) // Nothing Red / Accent Coral
        val ghostAccent = ColorProvider(Color(0xFF38BDF8))   // Cyan for Resisted/Ghost counter
        val textPrimary = if (isWhiteTheme) ColorProvider(Color(0xFF09090B)) else ColorProvider(Color(0xFFEDEDED))
        val textSecondary = if (isWhiteTheme) ColorProvider(Color(0xFF52525B)) else ColorProvider(Color(0xFFA1A1AA))
        val textMuted = if (isWhiteTheme) ColorProvider(Color(0xFF71717A)) else ColorProvider(Color(0xFF71717A))

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(22.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top section: Dual Quick Buttons on LEFT (Smoked + / Resisted -), Stats on RIGHT
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Main "+" and Ghost "✓"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main "+" Button
                        Box(
                            modifier = GlanceModifier
                                .size(50.dp)
                                .background(primaryAccent)
                                .cornerRadius(25.dp)
                                .clickable(actionRunCallback<LogSmokedActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                style = TextStyle(color = ColorProvider(Color.White), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        // Ghost Counter Button ("Resisted craving!")
                        Box(
                            modifier = GlanceModifier
                                .size(30.dp)
                                .background(surfaceColor)
                                .cornerRadius(15.dp)
                                .clickable(actionRunCallback<LogResistedActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🛡️",
                                style = TextStyle(fontSize = 13.sp)
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(14.dp))

                    // Stats on the RIGHT (tap opens app)
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxSize()
                            .clickable(actionStartActivity<MainActivity>()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Today:", style = TextStyle(color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium), modifier = GlanceModifier.width(44.dp))
                            Text(text = "${summary.todayCount} cigs", style = TextStyle(color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                            if (summary.todaySkippedCount > 0) {
                                Text(text = " (${summary.todaySkippedCount} resisted)", style = TextStyle(color = ghostAccent, fontSize = 10.sp))
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Week:", style = TextStyle(color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium), modifier = GlanceModifier.width(44.dp))
                            Text(text = "${summary.weekCount} cigs  ·  ${CalculationUtils.formatUnits(summary.weekSpentUnits)} u", style = TextStyle(color = textSecondary, fontSize = 11.sp))
                        }

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Pace:", style = TextStyle(color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium), modifier = GlanceModifier.width(44.dp))
                            Text(
                                text = "≈ ${String.format("%.0f", summary.paceYearlyCigs)}/yr  ·  ${CalculationUtils.formatUnits(summary.paceYearlyUnits)} u",
                                style = TextStyle(color = primaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Bottom strip: Saved today
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Could save: ",
                        style = TextStyle(color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${CalculationUtils.formatUnits(summary.todaySavedUnits)} units today",
                        style = TextStyle(color = primaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

class LogSmokedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as CigApp
        app.repository.logCigarette()
        CigWidgetReceiver.updateAll(context)
    }
}

class LogResistedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as CigApp
        app.repository.logSkippedCigarette()
        CigWidgetReceiver.updateAll(context)
    }
}
