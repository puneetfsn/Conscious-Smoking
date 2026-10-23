export interface ProjectFile {
  path: string;
  category: 'gradle' | 'manifest' | 'kotlin' | 'res' | 'ci' | 'docs';
  description: string;
  content: string;
}

export const ANDROID_PROJECT_FILES: ProjectFile[] = [
  {
    path: "app/src/main/java/com/cigcounter/app/ui/SettingsScreen.kt",
    category: "kotlin",
    description: "Settings screen with PIN privacy lock, currency, and hidden UPI 1-click donation card",
    content: "package com.cigcounter.app.ui\n\nimport android.content.ActivityNotFoundException\nimport android.content.ClipData\nimport android.content.Context\nimport android.content.Intent\nimport android.net.Uri\nimport android.widget.Toast\nimport androidx.compose.ui.semantics.contentDescription\nimport androidx.compose.ui.semantics.semantics\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.text.KeyboardOptions\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.*\nimport androidx.compose.material3.*\nimport androidx.compose.runtime.*\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.input.KeyboardType\nimport androidx.compose.ui.text.input.PasswordVisualTransformation\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport androidx.core.content.FileProvider\nimport com.cigcounter.app.data.CigaretteLog\nimport com.cigcounter.app.data.UserSettings\nimport com.cigcounter.app.ui.theme.*\nimport com.cigcounter.app.util.DamageReportGenerator\nimport com.cigcounter.app.util.HabitTasks\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.withContext\n\n@Composable\nfun SettingsScreen(\n    settings: UserSettings,\n    priceInput: String,\n    priceError: String?,\n    wishlistTitleInput: String,\n    wishlistCostInput: String,\n    onPriceChange: (String) -> Unit,\n    onSavePrice: () -> Unit,\n    onWishlistChange: (String, String) -> Unit,\n    onSaveWishlist: () -> Unit,\n    onClearWishlist: () -> Unit,\n    onWidgetThemeChange: (String) -> Unit,\n    onDarkModeChange: (Boolean) -> Unit = {},\n    onSetPinCode: (String) -> Unit,\n    onExportBackup: () -> Unit,\n    onImportBackup: (String) -> Unit,\n    onDeleteAllData: () -> Unit,\n    liveSecondsSinceLast: Long? = null,\n    recentLogs: List<CigaretteLog> = emptyList(),\n    modifier: Modifier = Modifier\n) {\n    var showDeleteConfirmDialog by remember { mutableStateOf(false) }\n    var showPinDialog by remember { mutableStateOf(false) }\n    var pinDialogInput by remember { mutableStateOf(\"\") }\n    var showImportDialog by remember { mutableStateOf(false) }\n    var importJsonInput by remember { mutableStateOf(\"\") }\n\n    val scrollState = rememberScrollState()\n    val context = LocalContext.current\n\n    Column(\n        modifier = modifier\n            .fillMaxSize()\n            .verticalScroll(scrollState)\n            .padding(horizontal = 20.dp, vertical = 16.dp),\n        verticalArrangement = Arrangement.spacedBy(16.dp)\n    ) {\n        Text(text = \"Settings\", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)\n\n        // GLOBAL APP THEME TOGGLE (ACCESSIBILITY)\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Icon(\n                        imageVector = if (settings.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,\n                        contentDescription = null,\n                        tint = AccentCoral,\n                        modifier = Modifier.size(20.dp)\n                    )\n                    Spacer(modifier = Modifier.width(8.dp))\n                    Text(text = \"App Theme (Accessibility)\", style = MaterialTheme.typography.titleMedium, color = TextPrimary)\n                }\n                Spacer(modifier = Modifier.height(4.dp))\n                Text(\n                    text = \"Toggle between Dark and Light mode for enhanced contrast and accessibility.\",\n                    style = MaterialTheme.typography.bodySmall,\n                    color = TextSecondary\n                )\n                Spacer(modifier = Modifier.height(10.dp))\n\n                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {\n                    FilterChip(\n                        selected = settings.isDarkMode,\n                        onClick = { onDarkModeChange(true) },\n                        leadingIcon = {\n                            Icon(\n                                imageVector = Icons.Default.DarkMode,\n                                contentDescription = null,\n                                modifier = Modifier.size(16.dp)\n                            )\n                        },\n                        label = { Text(\"Dark Mode\") }\n                    )\n                    FilterChip(\n                        selected = !settings.isDarkMode,\n                        onClick = { onDarkModeChange(false) },\n                        leadingIcon = {\n                            Icon(\n                                imageVector = Icons.Default.LightMode,\n                                contentDescription = null,\n                                modifier = Modifier.size(16.dp)\n                            )\n                        },\n                        label = { Text(\"Light Mode\") }\n                    )\n                }\n            }\n        }\n\n        // 1. DAMAGE REPORT FOR DOCTOR/THERAPIST\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))\n                    Spacer(modifier = Modifier.width(8.dp))\n                    Text(text = \"Doctor / Therapist Damage Report\", style = MaterialTheme.typography.titleMedium, color = TextPrimary)\n                }\n                Spacer(modifier = Modifier.height(4.dp))\n                Text(\n                    text = \"Generates a comprehensive clinical PDF on-device with your 30-day pattern, total intake, units spent, and 1-year pace projections.\",\n                    style = MaterialTheme.typography.bodySmall,\n                    color = TextSecondary\n                )\n                Spacer(modifier = Modifier.height(10.dp))\n                val coroutineScope = rememberCoroutineScope()\n                var isGeneratingPdf by remember { mutableStateOf(false) }\n\n                Button(\n                    onClick = {\n                        if (isGeneratingPdf) return@Button\n                        isGeneratingPdf = true\n                        coroutineScope.launch(Dispatchers.IO) {\n                            try {\n                                val pdfFile = DamageReportGenerator.generatePdfReport(\n                                    context = context,\n                                    logs = recentLogs,\n                                    pricePerUnit = settings.pricePerCigarette,\n                                    liveSecondsSinceLast = liveSecondsSinceLast\n                                )\n                                val authority = \"${context.packageName}.fileprovider\"\n                                val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)\n                                val shareIntent = Intent(Intent.ACTION_SEND).apply {\n                                    type = \"application/pdf\"\n                                    putExtra(Intent.EXTRA_STREAM, uri)\n                                    putExtra(Intent.EXTRA_SUBJECT, \"Conscious Smoking Clinical Damage Report\")\n                                    clipData = ClipData.newRawUri(\"Damage Report\", uri)\n                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)\n                                }\n                                val chooser = Intent.createChooser(shareIntent, \"Share Damage Report\").apply {\n                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)\n                                }\n                                withContext(Dispatchers.Main) {\n                                    context.startActivity(chooser)\n                                    isGeneratingPdf = false\n                                }\n                            } catch (e: Exception) {\n                                withContext(Dispatchers.Main) {\n                                    isGeneratingPdf = false\n                                    Toast.makeText(context, \"Could not share report: ${e.localizedMessage ?: \"Unknown error\"}\", Toast.LENGTH_LONG).show()\n                                }\n                            }\n                        }\n                    },\n                    enabled = !isGeneratingPdf,\n                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),\n                    shape = RoundedCornerShape(10.dp)\n                ) {\n                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))\n                    Spacer(modifier = Modifier.width(6.dp))\n                    Text(if (isGeneratingPdf) \"Generating PDF...\" else \"Generate & Share PDF\")\n                }\n\n                Spacer(modifier = Modifier.height(16.dp))\n                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkOutline))\n                Spacer(modifier = Modifier.height(14.dp))\n\n                // HEALTH BENEFITS & PHYSIOLOGICAL RECOVERY SECTION\n                Row(\n                    modifier = Modifier.fillMaxWidth(),\n                    horizontalArrangement = Arrangement.SpaceBetween,\n                    verticalAlignment = Alignment.CenterVertically\n                ) {\n                    Row(\n                        verticalAlignment = Alignment.CenterVertically,\n                        modifier = Modifier.weight(1f)\n                    ) {\n                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))\n                        Spacer(modifier = Modifier.width(8.dp))\n                        Text(\n                            text = \"Health Recovery Timeline\",\n                            style = MaterialTheme.typography.titleSmall,\n                            fontWeight = FontWeight.Bold,\n                            color = TextPrimary\n                        )\n                    }\n\n                    val smokeFreeText = remember(liveSecondsSinceLast) {\n                        if (liveSecondsSinceLast == null) \"No smoke recorded\"\n                        else {\n                            val h = liveSecondsSinceLast / 3600\n                            val m = (liveSecondsSinceLast % 3600) / 60\n                            if (h > 0) \"${h}h ${m}m smoke-free\" else \"${m}m smoke-free\"\n                        }\n                    }\n\n                    Box(\n                        modifier = Modifier\n                            .clip(RoundedCornerShape(8.dp))\n                            .background(DarkSurfaceVariant)\n                            .padding(horizontal = 8.dp, vertical = 3.dp)\n                    ) {\n                        Text(\n                            text = smokeFreeText,\n                            style = MaterialTheme.typography.labelSmall,\n                            color = SuccessGreen,\n                            fontWeight = FontWeight.Medium\n                        )\n                    }\n                }\n\n                Spacer(modifier = Modifier.height(4.dp))\n                Text(\n                    text = \"Tangible bodily recovery milestones based on time elapsed since your last cigarette:\",\n                    style = MaterialTheme.typography.bodySmall,\n                    color = TextSecondary,\n                    fontSize = 12.sp\n                )\n\n                Spacer(modifier = Modifier.height(12.dp))\n\n                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {\n                    HEALTH_BENEFIT_MILESTONES.forEach { milestone ->\n                        HealthMilestoneItem(\n                            milestone = milestone,\n                            liveSecondsSinceLast = liveSecondsSinceLast\n                        )\n                    }\n                }\n            }\n        }\n\n        // 2. WISHLIST MOTIVATION GOAL\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))\n                    Spacer(modifier = Modifier.width(8.dp))\n                    Text(text = \"Wishlist Goal Setup\", style = MaterialTheme.typography.titleMedium, color = TextPrimary)\n                }\n                Spacer(modifier = Modifier.height(4.dp))\n                Text(text = \"Turn abstract guilt into a tangible dream. (e.g. Headphones in 34 days).\", style = MaterialTheme.typography.bodySmall, color = TextSecondary)\n                Spacer(modifier = Modifier.height(10.dp))\n\n                OutlinedTextField(\n                    value = wishlistTitleInput,\n                    onValueChange = { onWishlistChange(it, wishlistCostInput) },\n                    label = { Text(\"Item Name (e.g. Wireless Headphones)\") },\n                    singleLine = true,\n                    modifier = Modifier.fillMaxWidth()\n                )\n                Spacer(modifier = Modifier.height(8.dp))\n                OutlinedTextField(\n                    value = wishlistCostInput,\n                    onValueChange = { onWishlistChange(wishlistTitleInput, it) },\n                    label = { Text(\"Target Cost (in units)\") },\n                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),\n                    singleLine = true,\n                    modifier = Modifier.fillMaxWidth()\n                )\n                Spacer(modifier = Modifier.height(10.dp))\n                Row(\n                    modifier = Modifier.fillMaxWidth(),\n                    horizontalArrangement = Arrangement.End,\n                    verticalAlignment = Alignment.CenterVertically\n                ) {\n                    OutlinedButton(\n                        onClick = onClearWishlist,\n                        shape = RoundedCornerShape(10.dp)\n                    ) {\n                        Text(\"Clear wishlist\", color = TextSecondary)\n                    }\n                    Spacer(modifier = Modifier.width(8.dp))\n                    Button(\n                        onClick = onSaveWishlist,\n                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),\n                        shape = RoundedCornerShape(10.dp)\n                    ) {\n                        Text(\"Save Wishlist\", color = Color.White)\n                    }\n                }\n            }\n        }\n\n        // POSITIVE SHIFT TASKS (Collapsible list of all tasks)\n        var isTasksExpanded by remember { mutableStateOf(false) }\n\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Row(\n                    modifier = Modifier\n                        .fillMaxWidth()\n                        .clickable { isTasksExpanded = !isTasksExpanded },\n                    horizontalArrangement = Arrangement.SpaceBetween,\n                    verticalAlignment = Alignment.CenterVertically\n                ) {\n                    Column(modifier = Modifier.weight(1f)) {\n                        Row(verticalAlignment = Alignment.CenterVertically) {\n                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))\n                            Spacer(modifier = Modifier.width(8.dp))\n                            Text(text = \"Positive Shift Tasks\", style = MaterialTheme.typography.titleMedium, color = TextPrimary)\n                        }\n                        Spacer(modifier = Modifier.height(4.dp))\n                        Text(\n                            text = \"${HabitTasks.allTasks.size} tasks (tap to ${if (isTasksExpanded) \"collapse\" else \"view all\"})\",\n                            style = MaterialTheme.typography.bodySmall,\n                            color = TextSecondary\n                        )\n                    }\n                    IconButton(onClick = { isTasksExpanded = !isTasksExpanded }) {\n                        Icon(\n                            imageVector = if (isTasksExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,\n                            contentDescription = if (isTasksExpanded) \"Collapse\" else \"Expand\",\n                            tint = TextPrimary\n                        )\n                    }\n                }\n\n                if (isTasksExpanded) {\n                    Spacer(modifier = Modifier.height(12.dp))\n                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {\n                        HabitTasks.allTasks.forEach { task ->\n                            Column(\n                                modifier = Modifier\n                                    .fillMaxWidth()\n                                    .clip(RoundedCornerShape(10.dp))\n                                    .background(DarkSurfaceVariant)\n                                    .padding(10.dp)\n                            ) {\n                                Row(\n                                    modifier = Modifier.fillMaxWidth(),\n                                    horizontalArrangement = Arrangement.SpaceBetween,\n                                    verticalAlignment = Alignment.CenterVertically\n                                ) {\n                                    Text(\n                                        text = task.title,\n                                        style = MaterialTheme.typography.bodySmall,\n                                        fontWeight = FontWeight.SemiBold,\n                                        color = TextPrimary,\n                                        modifier = Modifier.weight(1f)\n                                    )\n                                    Text(\n                                        text = task.category,\n                                        style = MaterialTheme.typography.labelSmall,\n                                        color = AccentAmber\n                                    )\n                                }\n                                Spacer(modifier = Modifier.height(2.dp))\n                                Text(\n                                    text = task.description,\n                                    style = MaterialTheme.typography.labelSmall,\n                                    color = TextSecondary\n                                )\n                            }\n                        }\n                    }\n                }\n            }\n        }\n\n        // 3. NOTHING OS WIDGET THEME\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Text(text = \"Nothing OS Widget Theme\", style = MaterialTheme.typography.titleMedium, color = TextPrimary)\n                Spacer(modifier = Modifier.height(4.dp))\n                Text(text = \"Select high-contrast aesthetic matching Nothing OS dark / light design:\", style = MaterialTheme.typography.bodySmall, color = TextSecondary)\n                Spacer(modifier = Modifier.height(10.dp))\n\n                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {\n                    FilterChip(\n                        selected = settings.widgetTheme == \"NOTHING_DARK\",\n                        onClick = { onWidgetThemeChange(\"NOTHING_DARK\") },\n                        label = { Text(\"Matrix Dark (Obsidian)\") }\n                    )\n                    FilterChip(\n                        selected = settings.widgetTheme == \"NOTHING_WHITE\",\n                        onClick = { onWidgetThemeChange(\"NOTHING_WHITE\") },\n                        label = { Text(\"Nothing Light (Monochrome)\") }\n                    )\n                }\n            }\n        }\n\n        // 4. PRIVACY PIN LOCK + OFFLINE BACKUP\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))\n                    Spacer(modifier = Modifier.width(8.dp))\n                    Text(text = \"Privacy & Local JSON Backup\", style = MaterialTheme.typography.titleMedium, color = TextPrimary)\n                }\n                Spacer(modifier = Modifier.height(4.dp))\n                Text(text = \"Protect data from roommates with PIN, and backup/restore without cloud.\", style = MaterialTheme.typography.bodySmall, color = TextSecondary)\n                Spacer(modifier = Modifier.height(10.dp))\n\n                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {\n                    OutlinedButton(\n                        onClick = { showPinDialog = true },\n                        shape = RoundedCornerShape(10.dp)\n                    ) {\n                        Text(if (settings.pinCode.isEmpty()) \"Set 4-Digit PIN\" else \"Change/Remove PIN\")\n                    }\n\n                    OutlinedButton(\n                        onClick = onExportBackup,\n                        shape = RoundedCornerShape(10.dp)\n                    ) {\n                        Text(\"Export JSON\")\n                    }\n\n                    OutlinedButton(\n                        onClick = { showImportDialog = true },\n                        shape = RoundedCornerShape(10.dp)\n                    ) {\n                        Text(\"Restore JSON\")\n                    }\n                }\n            }\n        }\n\n        // 5. PRICE CONFIGURATION\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Text(text = \"Price per Cigarette (Units)\", style = MaterialTheme.typography.titleMedium, color = TextPrimary)\n                Spacer(modifier = Modifier.height(8.dp))\n                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {\n                    OutlinedTextField(\n                        value = priceInput,\n                        onValueChange = onPriceChange,\n                        singleLine = true,\n                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),\n                        modifier = Modifier.weight(1f)\n                    )\n                    Button(\n                        onClick = onSavePrice,\n                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),\n                        shape = RoundedCornerShape(10.dp)\n                    ) {\n                        Text(\"Save\")\n                    }\n                }\n                if (priceError != null) {\n                    Text(text = priceError, color = ErrorRed, style = MaterialTheme.typography.labelSmall)\n                }\n            }\n        }\n\n        // 6. SUPPORT SMOKELEDGER (DONATE VIA UPI)\n        Card(\n            onClick = {\n                val upiUri = Uri.Builder()\n                    .scheme(\"upi\")\n                    .authority(\"pay\")\n                    .appendQueryParameter(\"pa\", HabitTasks.DONATE_UPI_ID)\n                    .appendQueryParameter(\"pn\", \"Conscious Smoking\")\n                    .appendQueryParameter(\"tn\", \"Support Conscious Smoking development\")\n                    .appendQueryParameter(\"cu\", \"INR\")\n                    .build()\n                val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)\n                val chooser = Intent.createChooser(upiIntent, \"Donate using UPI\")\n                try {\n                    context.startActivity(chooser)\n                } catch (e: ActivityNotFoundException) {\n                    Toast.makeText(\n                        context,\n                        \"No UPI application was found on this device.\",\n                        Toast.LENGTH_SHORT\n                    ).show()\n                }\n            },\n            modifier = Modifier\n                .fillMaxWidth()\n                .defaultMinSize(minHeight = 48.dp)\n                .semantics { contentDescription = \"Donate using UPI\" },\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Row(\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .padding(16.dp),\n                verticalAlignment = Alignment.CenterVertically\n            ) {\n                Icon(\n                    imageVector = Icons.Default.VolunteerActivism,\n                    contentDescription = null,\n                    tint = AccentAmber,\n                    modifier = Modifier.size(24.dp)\n                )\n                Spacer(modifier = Modifier.width(16.dp))\n                Column(modifier = Modifier.weight(1f)) {\n                    Text(\n                        text = \"Support Conscious Smoking\",\n                        style = MaterialTheme.typography.titleMedium,\n                        fontWeight = FontWeight.Bold,\n                        color = TextPrimary\n                    )\n                    Spacer(modifier = Modifier.height(2.dp))\n                    Text(\n                        text = \"Donate using UPI\",\n                        style = MaterialTheme.typography.bodySmall,\n                        color = TextSecondary\n                    )\n                }\n            }\n        }\n\n        // 7. DANGER ZONE\n        Card(\n            modifier = Modifier.fillMaxWidth(),\n            colors = CardDefaults.cardColors(containerColor = DarkSurface),\n            shape = RoundedCornerShape(16.dp)\n        ) {\n            Column(modifier = Modifier.padding(16.dp)) {\n                Text(text = \"Danger Zone\", style = MaterialTheme.typography.titleMedium, color = ErrorRed)\n                Spacer(modifier = Modifier.height(4.dp))\n                Text(text = \"Permanently wipe all cigarette rows from device.\", style = MaterialTheme.typography.bodySmall, color = TextSecondary)\n                Spacer(modifier = Modifier.height(10.dp))\n                OutlinedButton(\n                    onClick = { showDeleteConfirmDialog = true },\n                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),\n                    shape = RoundedCornerShape(10.dp)\n                ) {\n                    Text(\"Delete All Data\", color = ErrorRed)\n                }\n            }\n        }\n\n        Spacer(modifier = Modifier.height(24.dp))\n    }\n\n    // PIN Dialog\n    if (showPinDialog) {\n        AlertDialog(\n            onDismissRequest = { showPinDialog = false },\n            title = { Text(text = \"Set / Change PIN\", color = TextPrimary) },\n            text = {\n                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {\n                    Text(\"Enter a 4-digit PIN to lock Conscious Smoking. Leave empty to disable lock.\", style = MaterialTheme.typography.bodySmall, color = TextSecondary)\n                    OutlinedTextField(\n                        value = pinDialogInput,\n                        onValueChange = { if (it.length <= 4) pinDialogInput = it },\n                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),\n                        visualTransformation = PasswordVisualTransformation(),\n                        singleLine = true\n                    )\n                }\n            },\n            confirmButton = {\n                Button(\n                    onClick = {\n                        showPinDialog = false\n                        onSetPinCode(pinDialogInput)\n                    },\n                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)\n                ) {\n                    Text(\"Save PIN\")\n                }\n            },\n            dismissButton = {\n                TextButton(onClick = { showPinDialog = false }) {\n                    Text(\"Cancel\", color = TextSecondary)\n                }\n            },\n            containerColor = DarkSurface\n        )\n    }\n\n    // JSON Import Dialog\n    if (showImportDialog) {\n        AlertDialog(\n            onDismissRequest = { showImportDialog = false },\n            title = { Text(text = \"Restore JSON Backup\", color = TextPrimary) },\n            text = {\n                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {\n                    Text(\"Paste previously exported Conscious Smoking JSON:\", style = MaterialTheme.typography.bodySmall, color = TextSecondary)\n                    OutlinedTextField(\n                        value = importJsonInput,\n                        onValueChange = { importJsonInput = it },\n                        modifier = Modifier.fillMaxWidth().height(140.dp)\n                    )\n                }\n            },\n            confirmButton = {\n                Button(\n                    onClick = {\n                        showImportDialog = false\n                        onImportBackup(importJsonInput)\n                    },\n                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)\n                ) {\n                    Text(\"Restore\")\n                }\n            },\n            dismissButton = {\n                TextButton(onClick = { showImportDialog = false }) {\n                    Text(\"Cancel\", color = TextSecondary)\n                }\n            },\n            containerColor = DarkSurface\n        )\n    }\n\n    // Delete Confirmation Dialog\n    if (showDeleteConfirmDialog) {\n        AlertDialog(\n            onDismissRequest = { showDeleteConfirmDialog = false },\n            title = { Text(text = \"Delete all data?\", color = TextPrimary) },\n            text = { Text(text = \"Permanently erases all logged rows from local database.\", color = TextSecondary) },\n            confirmButton = {\n                Button(onClick = { showDeleteConfirmDialog = false; onDeleteAllData() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {\n                    Text(\"Delete Everything\", color = Color.White)\n                }\n            },\n            dismissButton = {\n                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text(\"Cancel\", color = TextSecondary) }\n            },\n            containerColor = DarkSurface\n        )\n    }\n}\n\ndata class HealthMilestone(\n    val title: String,\n    val targetSeconds: Long,\n    val targetLabel: String,\n    val description: String\n)\n\nval HEALTH_BENEFIT_MILESTONES = listOf(\n    HealthMilestone(\n        title = \"Heart Rate & Blood Pressure\",\n        targetSeconds = 20 * 60L,\n        targetLabel = \"20 min\",\n        description = \"Pulse and blood pressure normalize to baseline; peripheral circulation begins warming.\"\n    ),\n    HealthMilestone(\n        title = \"Carbon Monoxide Normalization\",\n        targetSeconds = 8 * 3600L,\n        targetLabel = \"8 hours\",\n        description = \"Blood CO drops by 50%; arterial oxygen saturation returns back to healthy baseline.\"\n    ),\n    HealthMilestone(\n        title = \"Cardiac Risk & Lung Debris Clearance\",\n        targetSeconds = 24 * 3600L,\n        targetLabel = \"24 hours\",\n        description = \"Sudden heart attack risk starts falling; lungs begin clearing out mucus and smoke debris.\"\n    ),\n    HealthMilestone(\n        title = \"Nicotine Elimination & Nerve Regrowth\",\n        targetSeconds = 48 * 3600L,\n        targetLabel = \"48 hours\",\n        description = \"100% of nicotine is eliminated from body; damaged nerve endings regrow; taste and smell sharpen.\"\n    ),\n    HealthMilestone(\n        title = \"Bronchial Tube Relaxation\",\n        targetSeconds = 72 * 3600L,\n        targetLabel = \"72 hours\",\n        description = \"Bronchial airways relax, breathing becomes noticeably effortless; lung volume expands.\"\n    ),\n    HealthMilestone(\n        title = \"Circulation & Physical Stamina\",\n        targetSeconds = 14 * 86400L,\n        targetLabel = \"2 weeks\",\n        description = \"Blood circulation to limbs and organs surges; aerobic stamina improves by up to 30%.\"\n    )\n)\n\n@Composable\nfun HealthMilestoneItem(\n    milestone: HealthMilestone,\n    liveSecondsSinceLast: Long?\n) {\n    val progress = remember(liveSecondsSinceLast, milestone.targetSeconds) {\n        if (liveSecondsSinceLast == null) 0f\n        else (liveSecondsSinceLast.toFloat() / milestone.targetSeconds.toFloat()).coerceIn(0f, 1f)\n    }\n    val isAchieved = progress >= 1f\n    val percentInt = (progress * 100).toInt()\n\n    Column(\n        modifier = Modifier\n            .fillMaxWidth()\n            .clip(RoundedCornerShape(12.dp))\n            .background(DarkSurfaceVariant)\n            .padding(12.dp)\n    ) {\n        Row(\n            modifier = Modifier.fillMaxWidth(),\n            horizontalArrangement = Arrangement.SpaceBetween,\n            verticalAlignment = Alignment.CenterVertically\n        ) {\n            Row(\n                verticalAlignment = Alignment.CenterVertically,\n                modifier = Modifier.weight(1f)\n            ) {\n                Icon(\n                    imageVector = if (isAchieved) Icons.Default.CheckCircle else Icons.Default.Timer,\n                    contentDescription = null,\n                    tint = if (isAchieved) SuccessGreen else Color(0xFF38BDF8),\n                    modifier = Modifier.size(16.dp)\n                )\n                Spacer(modifier = Modifier.width(8.dp))\n                Text(\n                    text = milestone.title,\n                    style = MaterialTheme.typography.bodyMedium,\n                    fontWeight = FontWeight.SemiBold,\n                    color = TextPrimary\n                )\n            }\n            Spacer(modifier = Modifier.width(6.dp))\n            Box(\n                modifier = Modifier\n                    .clip(RoundedCornerShape(6.dp))\n                    .background(if (isAchieved) SuccessGreen.copy(alpha = 0.15f) else Color(0xFF38BDF8).copy(alpha = 0.15f))\n                    .padding(horizontal = 8.dp, vertical = 2.dp)\n            ) {\n                Text(\n                    text = if (isAchieved) \"RESTORED\" else milestone.targetLabel,\n                    style = MaterialTheme.typography.labelSmall,\n                    fontWeight = FontWeight.Bold,\n                    color = if (isAchieved) SuccessGreen else Color(0xFF38BDF8)\n                )\n            }\n        }\n\n        Spacer(modifier = Modifier.height(4.dp))\n\n        Text(\n            text = milestone.description,\n            style = MaterialTheme.typography.bodySmall,\n            color = TextSecondary,\n            fontSize = 12.sp,\n            lineHeight = 16.sp\n        )\n\n        Spacer(modifier = Modifier.height(8.dp))\n\n        // Visual Progress Bar\n        Box(\n            modifier = Modifier\n                .fillMaxWidth()\n                .height(6.dp)\n                .clip(RoundedCornerShape(3.dp))\n                .background(DarkOutline)\n        ) {\n            Box(\n                modifier = Modifier\n                    .fillMaxHeight()\n                    .fillMaxWidth(fraction = progress.coerceAtLeast(0.01f))\n                    .clip(RoundedCornerShape(3.dp))\n                    .background(if (isAchieved) SuccessGreen else Color(0xFF38BDF8))\n            )\n        }\n\n        Spacer(modifier = Modifier.height(4.dp))\n\n        Row(\n            modifier = Modifier.fillMaxWidth(),\n            horizontalArrangement = Arrangement.SpaceBetween,\n            verticalAlignment = Alignment.CenterVertically\n        ) {\n            val statusLabel = if (isAchieved) {\n                \"Fully normalized\"\n            } else if (liveSecondsSinceLast == null) {\n                \"Waiting for smoke event\"\n            } else {\n                val remainingSec = (milestone.targetSeconds - liveSecondsSinceLast).coerceAtLeast(0L)\n                val remH = remainingSec / 3600\n                val remM = (remainingSec % 3600) / 60\n                val remText = if (remH > 0) \"${remH}h ${remM}m left\" else \"${remM}m left\"\n                \"$percentInt% reached • $remText\"\n            }\n\n            Text(\n                text = statusLabel,\n                style = MaterialTheme.typography.labelSmall,\n                color = if (isAchieved) SuccessGreen else TextMuted,\n                fontSize = 11.sp\n            )\n\n            Text(\n                text = \"$percentInt%\",\n                style = MaterialTheme.typography.labelSmall,\n                fontWeight = FontWeight.Bold,\n                color = if (isAchieved) SuccessGreen else TextSecondary,\n                fontSize = 11.sp\n            )\n        }\n    }\n}\n"
  },

  {
    path: "settings.gradle.kts",
    category: "gradle",
    description: "Configures Gradle plugins repository and Conscious Smoking root module",
    content: `pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Conscious Smoking"
include(":app")`
  },
  {
    path: "build.gradle.kts",
    category: "gradle",
    description: "Root Gradle build script registering Android, Kotlin, and KSP plugins",
    content: `// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}`
  },
  {
    path: "gradle.properties",
    category: "gradle",
    description: "JVM arguments and AndroidX flags",
    content: `# Project-wide Gradle settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official`
  },
  {
    path: "gradle/libs.versions.toml",
    category: "gradle",
    description: "Gradle Version Catalog with verified compatible versions",
    content: `[versions]
agp = "8.4.2"
kotlin = "1.9.24"
ksp = "1.9.24-1.0.20"
composeCompiler = "1.5.14"
composeBom = "2024.05.00"
coreKtx = "1.13.1"
lifecycleRuntimeKtx = "2.8.0"
activityCompose = "1.9.0"
navigationCompose = "2.7.7"
room = "2.6.1"
datastore = "1.1.1"
glance = "1.1.0"
workManager = "2.9.0"
coroutines = "1.8.1"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Room (KSP)
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# DataStore Preferences
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Jetpack Glance
androidx-glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
androidx-glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Tests
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`
  },
  {
    path: "app/build.gradle.kts",
    category: "gradle",
    description: "App module build script: SDK 34, Compose, Room KSP, Glance, WorkManager",
    content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cigcounter.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cigcounter.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}`
  },
  {
    path: "app/src/main/AndroidManifest.xml",
    category: "manifest",
    description: "App manifest: Zero internet permission, Evening summary notifications, and Boot receivers",
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Strictly offline: Zero INTERNET permission -->
    <uses-permission android:name="android.intent.action.BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />

    <application
        android:name=".CigApp"
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@drawable/ic_cig_logo"
        android:label="@string/app_name"
        android:roundIcon="@drawable/ic_cig_logo"
        android:supportsRtl="true"
        android:theme="@style/Theme.CigCounter">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.CigCounter">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver
            android:name=".widget.CigWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_info" />
        </receiver>

        <receiver
            android:name=".widget.BootReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

    </application>

</manifest>`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/data/CigaretteLog.kt",
    category: "kotlin",
    description: "Room Entity storing timestamp and isSkipped flag for Ghost Counter (resisted cravings)",
    content: `package com.cigcounter.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cigarette_logs")
data class CigaretteLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isSkipped: Boolean = false
)`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/util/DamageReportGenerator.kt",
    category: "kotlin",
    description: "Generates offline vector PDF clinical report for doctor/therapist consultation",
    content: `package com.cigcounter.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.cigcounter.app.data.CigaretteLog
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DamageReportGenerator {

    fun generatePdfReport(context: Context, logs: List<CigaretteLog>, pricePerUnit: Double, zoneId: ZoneId = ZoneId.systemDefault()): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply { color = Color.BLACK; textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val sectionPaint = Paint().apply { color = Color.rgb(220, 50, 20); textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val bodyPaint = Paint().apply { color = Color.BLACK; textSize = 10f; isAntiAlias = true }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

        var y = 50f
        canvas.drawText("Conscious Smoking — Clinical Damage Report", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Generated on: \${LocalDate.now(zoneId)}  •  Confidential Patient Profile", 40f, y, bodyPaint)
        y += 30f

        val thirtyDaysAgo = DateRangeUtils.getMonthStartMillis(zoneId)
        val smokedLogs = logs.filter { !it.isSkipped && it.timestampMillis >= thirtyDaysAgo }
        val totalSmoked = smokedLogs.size
        val totalSpent = totalSmoked * pricePerUnit
        val dailyAvg = totalSmoked / 30.0

        canvas.drawRect(40f, y, 555f, y + 90f, boxPaint)
        canvas.drawText("30-DAY BEHAVIOUR SUMMARY", 55f, y + 22f, sectionPaint)
        canvas.drawText("Total Smoked (30 Days): $totalSmoked cigarettes (\${String.format(\"%.1f\", dailyAvg)}/day)", 55f, y + 42f, bodyPaint)
        canvas.drawText("Total Units Spent: \${CalculationUtils.formatUnits(totalSpent)} units", 55f, y + 60f, bodyPaint)
        canvas.drawText("Ghost Counter: \${logs.count { it.isSkipped && it.timestampMillis >= thirtyDaysAgo }} cravings successfully resisted", 55f, y + 78f, bodyPaint)

        y += 115f
        canvas.drawText("THE MIRROR — 1-YEAR PACE PROJECTION", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("• Projected 1-year consumption: \${String.format(\"%.0f\", dailyAvg * 365)} cigarettes", 45f, y, bodyPaint)
        y += 16f
        canvas.drawText("• Projected financial expenditure: \${CalculationUtils.formatUnits(dailyAvg * 365 * pricePerUnit)} units", 45f, y, bodyPaint)

        pdfDocument.finishPage(page)
        val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val outputFile = File(reportsDir, "Conscious_Smoking_Damage_Report_\${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out -> pdfDocument.writeTo(out) }
        pdfDocument.close()
        return outputFile
    }
}`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/widget/EveningSummaryWorker.kt",
    category: "kotlin",
    description: "Daily 9 PM offline notification worker comparing today vs yesterday's habit count",
    content: `package com.cigcounter.app.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.cigcounter.app.CigApp
import com.cigcounter.app.R
import com.cigcounter.app.util.CalculationUtils
import com.cigcounter.app.util.DateRangeUtils
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class EveningSummaryWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CigApp
        val settings = app.preferencesRepository.getSettings()

        val startOfToday = DateRangeUtils.getStartOfTodayMillis()
        val startOfYesterday = DateRangeUtils.getDaysAgoStartOfDayMillis(1)

        val todayCount = app.repository.getSmokedCountFrom(startOfToday)
        val yesterdayCount = app.repository.getSmokedCountBetween(startOfYesterday, startOfToday)
        val todaySpent = CalculationUtils.calculateSpent(todayCount, settings.pricePerCigarette)

        val message = when {
            todayCount == 0 -> "Smoke-free day! 0 cigarettes logged. Incredible milestone! 🎉"
            todayCount < yesterdayCount -> "Today: $todayCount cigs (\${CalculationUtils.formatUnits(todaySpent)} u). That's \${yesterdayCount - todayCount} less than yesterday! 👏"
            else -> "Today: $todayCount cigs (\${CalculationUtils.formatUnits(todaySpent)} u). Rest well; tomorrow is a fresh start."
        }

        showNotification(applicationContext, "Evening Habit Summary", message)
        schedule(applicationContext)
        return Result.success()
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "evening_summary_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily 9 PM Summary", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_cig_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(9001, notification)
    }

    companion object {
        fun schedule(context: Context) {
            val now = ZonedDateTime.now()
            var target = now.withHour(21).withMinute(0).withSecond(0).withNano(0)
            if (now.isAfter(target)) target = target.plusDays(1)
            val delay = Duration.between(now, target).toMillis()

            val workRequest = OneTimeWorkRequestBuilder<EveningSummaryWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("smokeledger_evening_summary", ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/widget/CigWidget.kt",
    category: "kotlin",
    description: "Jetpack Glance Nothing OS widget: NDot Matrix Dark & Light Monochrome themes with Ghost button",
    content: `package com.cigcounter.app.widget

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
        val summary = runCatching { app.repository.getHabitSummarySync() }.getOrDefault(HabitSummary())
        val settings = runCatching { app.preferencesRepository.getSettings() }.getOrDefault(com.cigcounter.app.data.UserSettings())

        provideContent {
            val isWhite = settings.widgetTheme == "NOTHING_WHITE"
            val bg = if (isWhite) ColorProvider(Color(0xFFF4F4F5)) else ColorProvider(Color(0xFF0F0F11))
            val textCol = if (isWhite) ColorProvider(Color(0xFF09090B)) else ColorProvider(Color(0xFFEDEDED))
            val subText = if (isWhite) ColorProvider(Color(0xFF52525B)) else ColorProvider(Color(0xFFA1A1AA))
            val accentRed = ColorProvider(Color(0xFFFF3B30))

            Box(modifier = GlanceModifier.fillMaxSize().background(bg).cornerRadius(22.dp).padding(12.dp)) {
                Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = GlanceModifier.size(50.dp).background(accentRed).cornerRadius(25.dp).clickable(actionRunCallback<LogSmokedActionCallback>()), contentAlignment = Alignment.Center) {
                                Text("+", style = TextStyle(color = ColorProvider(Color.White), fontSize = 32.sp, fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = GlanceModifier.height(5.dp))
                            Box(modifier = GlanceModifier.size(28.dp).background(if (isWhite) ColorProvider(Color(0xFFE4E4E7)) else ColorProvider(Color(0xFF1E1E24))).cornerRadius(14.dp).clickable(actionRunCallback<LogResistedActionCallback>()), contentAlignment = Alignment.Center) {
                                Text("🛡️", style = TextStyle(fontSize = 12.sp))
                            }
                        }
                        Spacer(modifier = GlanceModifier.width(14.dp))
                        Column(modifier = GlanceModifier.defaultWeight().fillMaxSize().clickable(actionStartActivity<MainActivity>())) {
                            Text("Today: \${summary.todayCount} cigs (\${summary.todaySkippedCount} resisted)", style = TextStyle(color = textCol, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text("Week: \${summary.weekCount} cigs · \${CalculationUtils.formatUnits(summary.weekSpentUnits)} u", style = TextStyle(color = subText, fontSize = 11.sp))
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text("Pace: ≈ \${String.format(\"%.0f\", summary.paceYearlyCigs)}/yr · \${CalculationUtils.formatUnits(summary.paceYearlyUnits)} u", style = TextStyle(color = accentRed, fontSize = 11.sp, fontWeight = FontWeight.Medium))
                        }
                    }
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
}`
  },
  {
    path: ".github/workflows/build.yml",
    category: "ci",
    description: "GitHub Actions CI building debug APK on push/pull request and uploading artifact",
    content: `name: Build Conscious Smoking Android APK

on:
  push:
    branches: [ "main", "master" ]
  workflow_dispatch:

concurrency:
  group: \${{ github.workflow }}-\${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: 8.7
      - name: Ensure Wrapper Exists
        run: |
          if [ ! -f "gradlew" ]; then
            gradle wrapper --gradle-version 8.7
          fi
          chmod +x gradlew
      - run: ./gradlew testDebugUnitTest --continue
      - run: ./gradlew assembleDebug --stacktrace
      - uses: actions/upload-artifact@v4
        with:
          name: Conscious-Smoking-Debug-APK
          path: app/build/outputs/apk/debug/*.apk`
  },
  {
    path: "README.md",
    category: "docs",
    description: "Complete README with 9 new features, Nothing OS themes, and privacy documentation",
    content: `# Conscious Smoking 🚬⏱️

A fully offline, privacy-first habit awareness app and Nothing OS home-screen widget.

## 🌟 9 Core Features
1. **Ghost Counter (Positive Tracking Mode):** Log every time you resist a craving; tracks total cigarettes avoided and units saved.
2. **Damage Report (Doctor/Therapist PDF):** One-tap generates a clean, printable vector PDF with 30-day totals, pace, and clinical notes.
3. **The Mirror (Pace Projection):** Multiplies 7-day average across 1 year ("At this pace: 14,400 units/year").
4. **Time Since Last Cigarette:** Live ticking timer with physiological recovery milestones.
5. **Edit Past Days:** Easily add forgotten logs to yesterday or 2 days ago without breaking data fidelity.
6. **Wishlist Goal:** "Smoke 2 less per day = Wireless Headphones in 34 days."
7. **Nothing-Style Widget Themes:** Native Matrix Dark (Obsidian) and Nothing Light (Monochrome White) with dot-matrix styling.
8. **Local Evening Summary (No Internet):** Daily 9 PM notification via WorkManager comparing today vs yesterday.
9. **PIN Lock + Local JSON Backup:** 4-digit PIN lock to keep roommates out + zero-cloud JSON export/restore.`
  }
];
