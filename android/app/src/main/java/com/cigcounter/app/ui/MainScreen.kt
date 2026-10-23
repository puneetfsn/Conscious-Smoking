package com.cigcounter.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cigcounter.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(viewModel: CigViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val habitSummary by viewModel.habitSummary.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val dailyStats by viewModel.dailyStats.collectAsState()
    val hourlyTriggerData by viewModel.hourlyTriggerData.collectAsState()
    val selectedDays by viewModel.selectedHistoryDays.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val priceInput by viewModel.priceInput.collectAsState()
    val priceError by viewModel.priceError.collectAsState()
    val wishlistTitle by viewModel.wishlistTitleInput.collectAsState()
    val wishlistCost by viewModel.wishlistCostInput.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val liveSeconds by viewModel.liveSecondsSinceLast.collectAsState()
    val milestoneDialog by viewModel.milestoneDialog.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // PIN LOCK SCREEN (If enabled and locked)
    if (!isUnlocked && settings.pinCode.isNotEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBackground).padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(54.dp))
                Text(text = "Conscious Smoking Locked", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Enter your 4-digit PIN to access private logs:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 4) {
                            enteredPin = it
                            pinError = false
                            if (it.length == 4) {
                                val success = viewModel.verifyPin(it)
                                if (!success) {
                                    pinError = true
                                    enteredPin = ""
                                }
                            }
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = pinError,
                    supportingText = { if (pinError) Text("Incorrect PIN. Try again.", color = ErrorRed) }
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = DarkSurface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = if (selectedTab == 0) Icons.Filled.PlusOne else Icons.Outlined.PlusOne, contentDescription = "Counter") },
                    label = { Text("Counter") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentCoral, selectedTextColor = AccentCoral, indicatorColor = DarkSurfaceVariant)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = if (selectedTab == 1) Icons.Filled.BarChart else Icons.Outlined.BarChart, contentDescription = "History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentCoral, selectedTextColor = AccentCoral, indicatorColor = DarkSurfaceVariant)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentCoral, selectedTextColor = AccentCoral, indicatorColor = DarkSurfaceVariant)
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                summary = habitSummary,
                settings = settings,
                liveSecondsSinceLast = liveSeconds,
                onLogClick = { viewModel.logCigarette() },
                onLogSkippedClick = { viewModel.logSkippedCigarette() },
                onUndoClick = { viewModel.undoLast() },
                onAddPastDayClick = { daysAgo, count -> viewModel.logPastDay(daysAgo, count) },
                milestoneDialog = milestoneDialog,
                onDismissMilestoneDialog = { viewModel.dismissMilestoneDialog() },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> HistoryScreen(
                dailyStats = dailyStats,
                selectedDays = selectedDays,
                onDaysSelected = { viewModel.selectHistoryDays(it) },
                recentLogs = recentLogs,
                hourlyTriggerData = hourlyTriggerData,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> SettingsScreen(
                settings = settings,
                priceInput = priceInput,
                priceError = priceError,
                wishlistTitleInput = wishlistTitle,
                wishlistCostInput = wishlistCost,
                onPriceChange = { viewModel.onPriceInputChanged(it) },
                onSavePrice = { viewModel.savePrice() },
                onWishlistChange = { title, cost -> viewModel.onWishlistInputsChanged(title, cost) },
                onSaveWishlist = { viewModel.saveWishlist() },
                onClearWishlist = { viewModel.clearWishlist() },
                onWidgetThemeChange = { viewModel.setWidgetTheme(it) },
                onDarkModeChange = { viewModel.setDarkMode(it) },
                onSetPinCode = { viewModel.setPinCode(it) },
                onExportBackup = { viewModel.exportBackupJson { /* copy */ } },
                onImportBackup = { viewModel.importBackupJson(it) },
                onDeleteAllData = { viewModel.deleteAllData() },
                liveSecondsSinceLast = liveSeconds,
                recentLogs = recentLogs,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
