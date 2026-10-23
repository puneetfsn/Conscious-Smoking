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
            NothingStyleWidgetContent(
                summary = summary,
                isWhiteTheme = settings.widgetTheme == "NOTHING_WHITE"
            )
        }
    }

    @Composable
    private fun NothingStyleWidgetContent(
        summary: HabitSummary,
        isWhiteTheme: Boolean
    ) {
        val backgroundColor =
            if (isWhiteTheme) {
                ColorProvider(Color(0xFFF4F4F5))
            } else {
                ColorProvider(Color(0xFF0F0F11))
            }

        val surfaceColor =
            if (isWhiteTheme) {
                ColorProvider(Color(0xFFE4E4E7))
            } else {
                ColorProvider(Color(0xFF1E1E22))
            }

        val textPrimary =
            if (isWhiteTheme) {
                ColorProvider(Color(0xFF09090B))
            } else {
                ColorProvider(Color(0xFFEDEDED))
            }

        val textSecondary =
            if (isWhiteTheme) {
                ColorProvider(Color(0xFF52525B))
            } else {
                ColorProvider(Color(0xFFA1A1AA))
            }

        val primaryAccent = ColorProvider(Color(0xFFFF3B30))
        val blueAccent = ColorProvider(Color(0xFF4FC3F7))
        val savedAccent = ColorProvider(Color(0xFF34C759))
        val tarAccent = ColorProvider(Color(0xFFFBBF24))

        /*
         * Informational estimate only.
         * Uses an average machine-measured tar yield of approximately
         * 10 mg per cigarette. Actual exposure varies significantly.
         */
        val estimatedTarMg = summary.todayCount * 10

        val tarDisplay = if (estimatedTarMg >= 1000) {
            String.format("%.1fg", estimatedTarMg / 1000.0)
        } else {
            "${estimatedTarMg}mg"
        }

        val timeSince = summary.lastSmokedTimestamp
            ?.takeIf { it > 0L }
            ?.let { timestamp ->
                val difference =
                    (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)

                val totalMinutes = difference / 60_000L
                val hours = totalMinutes / 60L
                val minutes = totalMinutes % 60L

                when {
                    totalMinutes < 1L -> "now"
                    hours < 1L -> "${minutes}m"
                    hours < 24L -> "${hours}h ${minutes}m"
                    else -> "${hours / 24L}d ${hours % 24L}h"
                }
            } ?: "--"

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(18.dp)
                .padding(10.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left-side controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Log a smoked cigarette
                    Box(
                        modifier = GlanceModifier
                            .size(52.dp)
                            .background(primaryAccent)
                            .cornerRadius(26.dp)
                            .clickable(
                                actionRunCallback<LogSmokedActionCallback>()
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Log a resisted craving
                        Box(
                            modifier = GlanceModifier
                                .size(29.dp)
                                .background(surfaceColor)
                                .cornerRadius(15.dp)
                                .clickable(
                                    actionRunCallback<LogResistedActionCallback>()
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🛡️",
                                style = TextStyle(fontSize = 13.sp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(5.dp))

                        // Undo the latest logged action
                        Box(
                            modifier = GlanceModifier
                                .size(29.dp)
                                .background(surfaceColor)
                                .cornerRadius(15.dp)
                                .clickable(
                                    actionRunCallback<UndoLastActionCallback>()
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "↶",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                // Tapping anywhere in this section opens the app
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxSize()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Main cigarette counter
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${summary.todayCount}",
                            style = TextStyle(
                                color = textPrimary,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        Column {
                            Text(
                                text = "CIGS",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Text(
                                text = "TODAY",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(5.dp))

                    // Actual spending and saved amount
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(surfaceColor)
                                .cornerRadius(6.dp)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Spent: ${
                                    CalculationUtils.formatUnits(
                                        summary.todaySpentUnits
                                    )
                                } u",
                                style = TextStyle(
                                    color = primaryAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(5.dp))

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(surfaceColor)
                                .cornerRadius(6.dp)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Saved: ${
                                    CalculationUtils.formatUnits(
                                        summary.todaySkippedSavedUnits
                                    )
                                } u",
                                style = TextStyle(
                                    color = savedAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(5.dp))

                    // Resisted cravings and tar estimate
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(surfaceColor)
                                .cornerRadius(6.dp)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🛡 Resisted: ${summary.todaySkippedCount}",
                                style = TextStyle(
                                    color = blueAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(5.dp))

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(surfaceColor)
                                .cornerRadius(6.dp)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Tar avg. est.: ~$tarDisplay",
                                style = TextStyle(
                                    color = tarAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Compact summary footer
                    Text(
                        text = "Last: $timeSince  ·  Week: ${summary.weekCount}  ·  Month: ${summary.monthCount}",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = "Pace: ~${summary.paceYearlyCigs.toInt()} cigs/year",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Refreshes the widget that was tapped immediately, then synchronizes
 * any other Conscious Smoking widgets that might be on the home screen.
 */
private suspend fun refreshWidget(
    context: Context,
    glanceId: GlanceId
) {
    CigWidget().update(context, glanceId)
    CigWidgetReceiver.updateAll(context)
}

class LogSmokedActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val app = context.applicationContext as CigApp
        app.repository.logCigarette()
        refreshWidget(context, glanceId)
    }
}

class LogResistedActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val app = context.applicationContext as CigApp
        app.repository.logSkippedCigarette()
        refreshWidget(context, glanceId)
    }
}

class UndoLastActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val app = context.applicationContext as CigApp
        app.repository.undoLast()
        refreshWidget(context, glanceId)
    }
}
