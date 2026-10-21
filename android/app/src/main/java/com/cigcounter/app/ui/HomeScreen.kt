package com.cigcounter.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cigcounter.app.data.HabitSummary
import com.cigcounter.app.data.UserSettings
import com.cigcounter.app.ui.theme.*
import com.cigcounter.app.util.CalculationUtils
import com.cigcounter.app.util.HabitTasks

@Composable
fun HomeScreen(
    summary: HabitSummary,
    settings: UserSettings,
    liveSecondsSinceLast: Long?,
    onLogClick: () -> Unit,
    onLogSkippedClick: () -> Unit,
    onUndoClick: () -> Unit,
    onAddPastDayClick: (daysAgo: Long, count: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isPressedSmoked by remember { mutableStateOf(false) }
    var isPressedSkipped by remember { mutableStateOf(false) }
    var completedTasks by remember { mutableStateOf(setOf<String>()) }
    var showEditPastDaysDialog by remember { mutableStateOf(false) }

    val scaleSmoked by animateFloatAsState(targetValue = if (isPressedSmoked) 0.90f else 1f, label = "button_scale_smoked")
    val scaleSkipped by animateFloatAsState(targetValue = if (isPressedSkipped) 0.90f else 1f, label = "button_scale_skipped")

    // Format live timer: hh:mm:ss
    val timerText = remember(liveSecondsSinceLast) {
        if (liveSecondsSinceLast == null) "No smoke recorded yet"
        else {
            val h = liveSecondsSinceLast / 3600
            val m = (liveSecondsSinceLast % 3600) / 60
            val s = liveSecondsSinceLast % 60
            String.format("%02dh %02dm %02ds", h, m, s)
        }
    }

    // Wishlist calculations
    val wishlistDaysIfReduceTwo = remember(settings.wishlistCost, settings.pricePerCigarette) {
        val dailySavingsFromTwo = 2.0 * settings.pricePerCigarette
        if (dailySavingsFromTwo > 0) Math.ceil(settings.wishlistCost / dailySavingsFromTwo).toInt() else 30
    }

    val suggestedTasks = HabitTasks.getSuggestedTasks(summary.todayCount)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header
        Text(text = "SmokeLedger", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(text = "Zero guilt • Pure math • Habit shift", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

        // 1. LIVE TIMER: TIME SINCE LAST CIGARETTE
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "SMOKE-FREE DURATION", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(text = timerText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TODAY'S HERO DISPLAY
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Smoked counter
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "TODAY SMOKED", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(text = "${summary.todayCount}", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = if (summary.todayCount > 0) AccentCoral else TextPrimary)
                Text(text = "${CalculationUtils.formatUnits(summary.todaySpentUnits)} units", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).height(50.dp).background(DarkOutline))

            // Ghost Counter (Resisted cravings)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "GHOST COUNTER", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8))
                Text(text = "${summary.todaySkippedCount}", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                Text(text = "${CalculationUtils.formatUnits(summary.todaySkippedSavedUnits)} units saved", style = MaterialTheme.typography.bodySmall, color = Color(0xFF38BDF8))
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // DUAL ACTION BUTTONS: Smoked (+) and Resisted Cravings (Ghost Counter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Button: Log Cigarette
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(scaleSmoked)
                        .clip(CircleShape)
                        .background(AccentCoral)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                isPressedSmoked = true
                                tryAwaitRelease()
                                isPressedSmoked = false
                                onLogClick()
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Smoked", tint = Color.White, modifier = Modifier.size(46.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "I Smoked", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }

            Spacer(modifier = Modifier.width(36.dp))

            // Ghost Button: "Resisted One!"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(scaleSkipped)
                        .clip(CircleShape)
                        .background(Color(0xFF0369A1))
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                isPressedSkipped = true
                                tryAwaitRelease()
                                isPressedSkipped = false
                                onLogSkippedClick()
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Resisted craving", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Resisted! (Ghost)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary controls row: Undo & Edit Past Days
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onUndoClick,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "Undo", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }

            OutlinedButton(
                onClick = { showEditPastDaysDialog = true },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "Edit Past Day", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. "THE MIRROR" — PACE PROJECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "The Mirror — Pace Projection", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on your 7-day average pace:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkSurfaceVariant).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Monthly pace", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(text = "≈ ${String.format("%.0f", summary.paceMonthlyCigs)} cigs", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(text = "→", color = TextMuted)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Annual pace", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(text = "${CalculationUtils.formatUnits(summary.paceYearlyUnits)} units", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = AccentCoral)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. WISHLIST GOAL: "HEADPHONES IN 34 DAYS"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Wishlist Motivation", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Goal: ${settings.wishlistName} (${CalculationUtils.formatUnits(settings.wishlistCost)} units)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkSurfaceVariant).padding(10.dp)
                ) {
                    Text(
                        text = "🎧 Smoke 2 less per day = ${settings.wishlistName} in $wishlistDaysIfReduceTwo days!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. POSITIVE SHIFT TASKS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Positive Shift Tasks", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(text = if (summary.todayCount >= 8) "Easy Micro-Habits" else "Gentle Action", style = MaterialTheme.typography.labelSmall, color = AccentAmber)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestedTasks.take(3).forEach { task ->
                        val isDone = completedTasks.contains(task.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .padding(10.dp)
                                .pointerInput(task.id) {
                                    detectTapGestures {
                                        completedTasks = if (isDone) completedTasks - task.id else completedTasks + task.id
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isDone) SuccessGreen else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = task.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (isDone) TextMuted else TextPrimary)
                                Text(text = task.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Edit Past Days Dialog
    if (showEditPastDaysDialog) {
        var selectedDaysAgo by remember { mutableStateOf(1L) }
        var inputCount by remember { mutableStateOf(1) }

        AlertDialog(
            onDismissRequest = { showEditPastDaysDialog = false },
            title = { Text(text = "Edit Past Day's Log", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Forgot to log yesterday or earlier? Add records without losing your streak.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedDaysAgo == 1L,
                            onClick = { selectedDaysAgo = 1L },
                            label = { Text("Yesterday") }
                        )
                        FilterChip(
                            selected = selectedDaysAgo == 2L,
                            onClick = { selectedDaysAgo = 2L },
                            label = { Text("2 days ago") }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Cigarettes to add:", color = TextPrimary)
                        IconButton(onClick = { if (inputCount > 1) inputCount-- }) {
                            Text(text = "-", fontSize = 20.sp, color = TextPrimary)
                        }
                        Text(text = "$inputCount", fontWeight = FontWeight.Bold, color = AccentCoral)
                        IconButton(onClick = { inputCount++ }) {
                            Text(text = "+", fontSize = 20.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditPastDaysDialog = false
                        onAddPastDayClick(selectedDaysAgo, inputCount)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                ) {
                    Text("Add Records")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPastDaysDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
