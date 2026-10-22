package com.cigcounter.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.cigcounter.app.data.CigaretteLog
import com.cigcounter.app.data.UserSettings
import com.cigcounter.app.ui.theme.*
import com.cigcounter.app.util.DamageReportGenerator
import com.cigcounter.app.util.HabitTasks

@Composable
fun SettingsScreen(
    settings: UserSettings,
    priceInput: String,
    priceError: String?,
    wishlistTitleInput: String,
    wishlistCostInput: String,
    onPriceChange: (String) -> Unit,
    onSavePrice: () -> Unit,
    onWishlistChange: (String, String) -> Unit,
    onSaveWishlist: () -> Unit,
    onClearWishlist: () -> Unit,
    onWidgetThemeChange: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit = {},
    onSetPinCode: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: (String) -> Unit,
    onDeleteAllData: () -> Unit,
    liveSecondsSinceLast: Long? = null,
    recentLogs: List<CigaretteLog> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinDialogInput by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)

        // GLOBAL APP THEME TOGGLE (ACCESSIBILITY)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (settings.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = AccentCoral,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "App Theme (Accessibility)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toggle between Dark and Light mode for enhanced contrast and accessibility.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = settings.isDarkMode,
                        onClick = { onDarkModeChange(true) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Dark Mode") }
                    )
                    FilterChip(
                        selected = !settings.isDarkMode,
                        onClick = { onDarkModeChange(false) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Light Mode") }
                    )
                }
            }
        }

        // 1. DAMAGE REPORT FOR DOCTOR/THERAPIST
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Doctor / Therapist Damage Report", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generates a comprehensive clinical PDF on-device with your 30-day pattern, total intake, units spent, and 1-year pace projections.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        try {
                            val pdfFile = DamageReportGenerator.generatePdfReport(
                                context = context,
                                logs = recentLogs,
                                pricePerUnit = settings.pricePerCigarette,
                                liveSecondsSinceLast = liveSecondsSinceLast
                            )
                            val authority = "${context.packageName}.fileprovider"
                            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "SmokeLedger Clinical Damage Report")
                                clipData = ClipData.newRawUri("Damage Report", uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val chooser = Intent.createChooser(shareIntent, "Share Damage Report").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(chooser)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not share report: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate & Share PDF")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkOutline))
                Spacer(modifier = Modifier.height(14.dp))

                // HEALTH BENEFITS & PHYSIOLOGICAL RECOVERY SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Health Recovery Timeline",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    val smokeFreeText = remember(liveSecondsSinceLast) {
                        if (liveSecondsSinceLast == null) "No smoke recorded"
                        else {
                            val h = liveSecondsSinceLast / 3600
                            val m = (liveSecondsSinceLast % 3600) / 60
                            if (h > 0) "${h}h ${m}m smoke-free" else "${m}m smoke-free"
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = smokeFreeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tangible bodily recovery milestones based on time elapsed since your last cigarette:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HEALTH_BENEFIT_MILESTONES.forEach { milestone ->
                        HealthMilestoneItem(
                            milestone = milestone,
                            liveSecondsSinceLast = liveSecondsSinceLast
                        )
                    }
                }
            }
        }

        // 2. WISHLIST MOTIVATION GOAL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Wishlist Goal Setup", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Turn abstract guilt into a tangible dream. (e.g. Headphones in 34 days).", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = wishlistTitleInput,
                    onValueChange = { onWishlistChange(it, wishlistCostInput) },
                    label = { Text("Item Name (e.g. Wireless Headphones)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = wishlistCostInput,
                    onValueChange = { onWishlistChange(wishlistTitleInput, it) },
                    label = { Text("Target Cost (in units)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClearWishlist,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Clear wishlist", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSaveWishlist,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Wishlist", color = Color.White)
                    }
                }
            }
        }

        // POSITIVE SHIFT TASKS (Collapsible list of all tasks)
        var isTasksExpanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isTasksExpanded = !isTasksExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Positive Shift Tasks", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${HabitTasks.allTasks.size} tasks (tap to ${if (isTasksExpanded) "collapse" else "view all"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { isTasksExpanded = !isTasksExpanded }) {
                        Icon(
                            imageVector = if (isTasksExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isTasksExpanded) "Collapse" else "Expand",
                            tint = TextPrimary
                        )
                    }
                }

                if (isTasksExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HabitTasks.allTasks.forEach { task ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = task.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. NOTHING OS WIDGET THEME
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Nothing OS Widget Theme", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Select high-contrast aesthetic matching Nothing OS dark / light design:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = settings.widgetTheme == "NOTHING_DARK",
                        onClick = { onWidgetThemeChange("NOTHING_DARK") },
                        label = { Text("Matrix Dark (Obsidian)") }
                    )
                    FilterChip(
                        selected = settings.widgetTheme == "NOTHING_WHITE",
                        onClick = { onWidgetThemeChange("NOTHING_WHITE") },
                        label = { Text("Nothing Light (Monochrome)") }
                    )
                }
            }
        }

        // 4. PRIVACY PIN LOCK + OFFLINE BACKUP
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Privacy & Local JSON Backup", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Protect data from roommates with PIN, and backup/restore without cloud.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showPinDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (settings.pinCode.isEmpty()) "Set 4-Digit PIN" else "Change/Remove PIN")
                    }

                    OutlinedButton(
                        onClick = onExportBackup,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Export JSON")
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Restore JSON")
                    }
                }
            }
        }

        // 5. PRICE CONFIGURATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Price per Cigarette (Units)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = onPriceChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onSavePrice,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save")
                    }
                }
                if (priceError != null) {
                    Text(text = priceError, color = ErrorRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // 6. DONATE TO DEVELOPER & SCHIZOPHRENIA AWARENESS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Donate & Schizophrenia Awareness", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Direct support to developer Puneet towards app maintenance and schizophrenia awareness.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkSurfaceVariant).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = HabitTasks.DONATE_UPI_ID, fontWeight = FontWeight.Bold, color = AccentAmber)
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", HabitTasks.DONATE_UPI_ID))
                            Toast.makeText(context, "Copied ${HabitTasks.DONATE_UPI_ID}", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Copy UPI")
                    }
                }
            }
        }

        // 7. DANGER ZONE
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Danger Zone", style = MaterialTheme.typography.titleMedium, color = ErrorRed)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Permanently wipe all cigarette rows from device.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete All Data", color = ErrorRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(text = "Set / Change PIN", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a 4-digit PIN to lock SmokeLedger. Leave empty to disable lock.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = pinDialogInput,
                        onValueChange = { if (it.length <= 4) pinDialogInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPinDialog = false
                        onSetPinCode(pinDialogInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // JSON Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(text = "Restore JSON Backup", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste previously exported SmokeLedger JSON:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportDialog = false
                        onImportBackup(importJsonInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Delete all data?", color = TextPrimary) },
            text = { Text(text = "Permanently erases all logged rows from local database.", color = TextSecondary) },
            confirmButton = {
                Button(onClick = { showDeleteConfirmDialog = false; onDeleteAllData() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                    Text("Delete Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = DarkSurface
        )
    }
}

data class HealthMilestone(
    val title: String,
    val targetSeconds: Long,
    val targetLabel: String,
    val description: String
)

val HEALTH_BENEFIT_MILESTONES = listOf(
    HealthMilestone(
        title = "Heart Rate & Blood Pressure",
        targetSeconds = 20 * 60L,
        targetLabel = "20 min",
        description = "Pulse and blood pressure normalize to baseline; peripheral circulation begins warming."
    ),
    HealthMilestone(
        title = "Carbon Monoxide Normalization",
        targetSeconds = 8 * 3600L,
        targetLabel = "8 hours",
        description = "Blood CO drops by 50%; arterial oxygen saturation returns back to healthy baseline."
    ),
    HealthMilestone(
        title = "Cardiac Risk & Lung Debris Clearance",
        targetSeconds = 24 * 3600L,
        targetLabel = "24 hours",
        description = "Sudden heart attack risk starts falling; lungs begin clearing out mucus and smoke debris."
    ),
    HealthMilestone(
        title = "Nicotine Elimination & Nerve Regrowth",
        targetSeconds = 48 * 3600L,
        targetLabel = "48 hours",
        description = "100% of nicotine is eliminated from body; damaged nerve endings regrow; taste and smell sharpen."
    ),
    HealthMilestone(
        title = "Bronchial Tube Relaxation",
        targetSeconds = 72 * 3600L,
        targetLabel = "72 hours",
        description = "Bronchial airways relax, breathing becomes noticeably effortless; lung volume expands."
    ),
    HealthMilestone(
        title = "Circulation & Physical Stamina",
        targetSeconds = 14 * 86400L,
        targetLabel = "2 weeks",
        description = "Blood circulation to limbs and organs surges; aerobic stamina improves by up to 30%."
    )
)

@Composable
fun HealthMilestoneItem(
    milestone: HealthMilestone,
    liveSecondsSinceLast: Long?
) {
    val progress = remember(liveSecondsSinceLast, milestone.targetSeconds) {
        if (liveSecondsSinceLast == null) 0f
        else (liveSecondsSinceLast.toFloat() / milestone.targetSeconds.toFloat()).coerceIn(0f, 1f)
    }
    val isAchieved = progress >= 1f
    val percentInt = (progress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isAchieved) Icons.Default.CheckCircle else Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (isAchieved) SuccessGreen else Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isAchieved) SuccessGreen.copy(alpha = 0.15f) else Color(0xFF38BDF8).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isAchieved) "RESTORED" else milestone.targetLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAchieved) SuccessGreen else Color(0xFF38BDF8)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = milestone.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Visual Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkOutline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress.coerceAtLeast(0.01f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isAchieved) SuccessGreen else Color(0xFF38BDF8))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusLabel = if (isAchieved) {
                "Fully normalized"
            } else if (liveSecondsSinceLast == null) {
                "Waiting for smoke event"
            } else {
                val remainingSec = (milestone.targetSeconds - liveSecondsSinceLast).coerceAtLeast(0L)
                val remH = remainingSec / 3600
                val remM = (remainingSec % 3600) / 60
                val remText = if (remH > 0) "${remH}h ${remM}m left" else "${remM}m left"
                "$percentInt% reached • $remText"
            }

            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (isAchieved) SuccessGreen else TextMuted,
                fontSize = 11.sp
            )

            Text(
                text = "$percentInt%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isAchieved) SuccessGreen else TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
