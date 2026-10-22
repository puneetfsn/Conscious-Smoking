package com.cigcounter.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
        val textPrimary = if (isWhiteTheme) ColorProvider(Color(0xFF09090B)) else ColorProvider(Color(0xFFEDEDED))
        val textSecondary = if (isWhiteTheme) ColorProvider(Color(0xFF52525B)) else ColorProvider(Color(0xFFA1A1AA))

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(18.dp)
                .padding(14.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Main "+" and Ghost "🛡️"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Main "+" Button: logs cigarette directly without opening app
                    Box(
                        modifier = GlanceModifier
                            .size(54.dp)
                            .background(primaryAccent)
                            .cornerRadius(27.dp)
                            .clickable(actionRunCallback<LogSmokedActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

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
                            style = TextStyle(fontSize = 14.sp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.width(16.dp))

                // Stats on the RIGHT (tapping text area opens MainActivity)
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxSize()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    // GIANT "Today" Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${summary.todayCount}",
                            style = TextStyle(color = textPrimary, fontSize = 46.sp, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Column {
                            Text(
                                text = "CIGS",
                                style = TextStyle(color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "TODAY",
                                style = TextStyle(color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Highlight "Saved" Tag (Nothing OS style pill)
                    Box(
                        modifier = GlanceModifier
                            .background(surfaceColor)
                            .cornerRadius(6.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Could save: ${CalculationUtils.formatUnits(summary.todaySavedUnits)} u",
                            style = TextStyle(color = primaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    // Footer spreading out the rest of the data
                    Text(
                        text = "Week: ${summary.weekCount}  ·  Month: ${summary.monthCount}",
                        style = TextStyle(color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
