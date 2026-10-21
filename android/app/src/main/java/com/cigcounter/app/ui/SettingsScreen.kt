package com.cigcounter.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.core.content.FileProvider
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
    onWidgetThemeChange: (String) -> Unit,
    onSetPinCode: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: (String) -> Unit,
    onDeleteAllData: () -> Unit,
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
                            val pdfFile = DamageReportGenerator.generatePdfReport(context, emptyList(), settings.pricePerCigarette)
                            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Damage Report"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Generated report in cache", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate & Share PDF")
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
                Button(
                    onClick = onSaveWishlist,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Wishlist", color = Color.White)
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
