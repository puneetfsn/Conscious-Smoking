package com.cigcounter.app.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var oldPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    var isOldPinVerified by remember { mutableStateOf(false) }
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
                val coroutineScope = rememberCoroutineScope()
                var isGeneratingPdf by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        if (isGeneratingPdf) return@Button
                        isGeneratingPdf = true
                        coroutineScope.launch(Dispatchers.IO) {
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
                                    putExtra(Intent.EXTRA_SUBJECT, "Conscious Smoking Clinical Damage Report")
                                    clipData = ClipData.newRawUri("Damage Report", uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = Intent.createChooser(shareIntent, "Share Damage Report").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                withContext(Dispatchers.Main) {
                                    context.startActivity(chooser)
                                    isGeneratingPdf = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isGeneratingPdf = false
                                    Toast.makeText(context, "Could not share report: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !isGeneratingPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isGeneratingPdf) "Generating PDF..." else "Generate & Share PDF")
                }
            }
        }

        // 2. NOTHING OS WIDGET THEME
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

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            oldPinInput = ""
                            newPinInput = ""
                            pinErrorMessage = null
                            isOldPinVerified = false
                            showPinDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (settings.pinCode.isEmpty()) "Set 4-Digit PIN Lock" else "Change / Remove PIN")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportBackup,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore JSON", maxLines = 1)
                        }
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

        // 6. SUPPORT SMOKELEDGER (DONATE VIA UPI)
        Card(
            onClick = {
                val upiUri = Uri.Builder()
                    .scheme("upi")
                    .authority("pay")
                    .appendQueryParameter("pa", HabitTasks.DONATE_UPI_ID)
                    .appendQueryParameter("pn", "Conscious Smoking")
                    .appendQueryParameter("tn", "Support Conscious Smoking development")
                    .appendQueryParameter("cu", "INR")
                    .build()
                val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                val chooser = Intent.createChooser(upiIntent, "Donate using UPI")
                try {
                    context.startActivity(chooser)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "No UPI application was found on this device.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .semantics { contentDescription = "Donate using UPI" },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = AccentAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text 
