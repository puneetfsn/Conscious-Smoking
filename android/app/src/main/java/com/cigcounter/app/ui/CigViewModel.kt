package com.cigcounter.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cigcounter.app.CigApp
import com.cigcounter.app.data.CigaretteLog
import com.cigcounter.app.data.CigaretteRepository
import com.cigcounter.app.data.DayStat
import com.cigcounter.app.data.HabitSummary
import com.cigcounter.app.data.HourlyTriggerData
import com.cigcounter.app.data.UserSettings
import com.cigcounter.app.util.CalculationUtils
import com.cigcounter.app.util.DamageReportGenerator
import com.cigcounter.app.util.DateRangeUtils
import com.cigcounter.app.util.HabitTask
import com.cigcounter.app.util.HabitTasks
import com.cigcounter.app.util.PriceValidationResult
import com.cigcounter.app.widget.CigWidgetReceiver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class MilestoneDialogData(
    val milestone: Int,
    val task: HabitTask
)

@OptIn(ExperimentalCoroutinesApi::class)
class CigViewModel(
    application: Application,
    private val repository: CigaretteRepository
) : AndroidViewModel(application) {

    val habitSummary: StateFlow<HabitSummary> = repository.getHabitSummaryFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HabitSummary()
        )

    val settings: StateFlow<UserSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    private val _selectedHistoryDays = MutableStateFlow(7)
    val selectedHistoryDays: StateFlow<Int> = _selectedHistoryDays.asStateFlow()

    val dailyStats: StateFlow<List<DayStat>> = _selectedHistoryDays
        .flatMapLatest { days -> repository.getDailyStatsFlow(days) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val hourlyTriggerData: StateFlow<HourlyTriggerData> = _selectedHistoryDays
        .flatMapLatest { days -> repository.getHourlyTriggerFlow(days) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HourlyTriggerData()
        )

    val recentLogs: StateFlow<List<CigaretteLog>> = repository.getRecentLogsFlow(limit = 40)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Live Smoke-Free Timer ticker (seconds elapsed since lastSmokedTimestamp)
    private val _liveSecondsSinceLast = MutableStateFlow<Long?>(null)
    val liveSecondsSinceLast: StateFlow<Long?> = _liveSecondsSinceLast.asStateFlow()

    private val _priceInput = MutableStateFlow("")
    val priceInput: StateFlow<String> = _priceInput.asStateFlow()

    private val _priceError = MutableStateFlow<String?>(null)
    val priceError: StateFlow<String?> = _priceError.asStateFlow()

    // Wishlist state: defaults to empty strings (no hardcoded fallback)
    private val _wishlistTitleInput = MutableStateFlow("")
    val wishlistTitleInput: StateFlow<String> = _wishlistTitleInput.asStateFlow()

    private val _wishlistCostInput = MutableStateFlow("")
    val wishlistCostInput: StateFlow<String> = _wishlistCostInput.asStateFlow()

    // Positive Shift Milestone dialog state
    private val _milestoneDialog = MutableStateFlow<MilestoneDialogData?>(null)
    val milestoneDialog: StateFlow<MilestoneDialogData?> = _milestoneDialog.asStateFlow()

    // PIN lock state
    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        // Ticker loop for live smoke-free timer
        viewModelScope.launch {
            while (isActive) {
                val lastTimestamp = habitSummary.value.lastSmokedTimestamp
                if (lastTimestamp != null) {
                    val diffSec = (System.currentTimeMillis() - lastTimestamp) / 1000L
                    _liveSecondsSinceLast.value = diffSec.coerceAtLeast(0L)
                } else {
                    _liveSecondsSinceLast.value = null
                }
                delay(1000L)
            }
        }

        viewModelScope.launch {
            var isInitialBoot = true
            repository.settingsFlow.collect { currentSettings ->
                if (_priceInput.value.isEmpty()) {
                    _priceInput.value = CalculationUtils.formatUnits(currentSettings.pricePerCigarette)
                }
                if (_wishlistTitleInput.value.isEmpty() && currentSettings.wishlistName.isNotEmpty()) {
                    _wishlistTitleInput.value = currentSettings.wishlistName
                }
                if (_wishlistCostInput.value.isEmpty() && currentSettings.wishlistCost > 0.0) {
                    _wishlistCostInput.value = CalculationUtils.formatUnits(currentSettings.wishlistCost)
                }
                // Only lock on cold boot if PIN is set; never re-lock an already authenticated session
                if (isInitialBoot && currentSettings.pinCode.isNotEmpty()) {
                    _isUnlocked.value = false
                }
                isInitialBoot = false
            }
        }

        // Positive shift milestone monitor:
        // Triggers ONE random task every 10 smoked cigarettes in total (10, 20, 30...)
        // If logged from the widget, triggers the next time the app opens.
        viewModelScope.launch {
            combine(
                repository.getTotalSmokedCountFlow(),
                repository.settingsFlow
            ) { totalSmoked, currentSettings ->
                Pair(totalSmoked, currentSettings.lastMilestoneShown)
            }.collectLatest { (totalSmoked, lastMilestoneShown) ->
                val milestone = (totalSmoked / 10) * 10
                if (milestone >= 10 && milestone > lastMilestoneShown) {
                    if (_milestoneDialog.value?.milestone != milestone) {
                        _milestoneDialog.value = MilestoneDialogData(
                            milestone = milestone,
                            task = HabitTasks.getRandomTask()
                        )
                    }
                }
            }
        }
    }

    fun dismissMilestoneDialog() {
        val current = _milestoneDialog.value
        _milestoneDialog.value = null
        if (current != null) {
            viewModelScope.launch {
                val app = getApplication<CigApp>()
                app.preferencesRepository.setLastMilestoneShown(current.milestone)
            }
        }
    }

    fun selectHistoryDays(days: Int) {
        _selectedHistoryDays.value = days
    }

    fun onPriceInputChanged(newInput: String) {
        _priceInput.value = newInput
        if (_priceError.value != null) _priceError.value = null
    }

    fun onWishlistInputsChanged(title: String, cost: String) {
        _wishlistTitleInput.value = title
        _wishlistCostInput.value = cost
    }

    fun verifyPin(entered: String): Boolean {
        val expected = settings.value.pinCode
        if (expected.isEmpty() || entered == expected) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun setPinCode(newPin: String) {
        viewModelScope.launch {
            val app = getApplication<CigApp>()
            app.preferencesRepository.setPinCode(newPin)
            _snackbarEvent.emit(if (newPin.isEmpty()) "PIN lock removed" else "PIN lock enabled")
        }
    }

    fun logCigarette() {
        viewModelScope.launch {
            repository.logCigarette()
            CigWidgetReceiver.updateAll(getApplication())
        }
    }

    // Ghost Counter: log resisted craving!
    fun logSkippedCigarette() {
        viewModelScope.launch {
            repository.logSkippedCigarette()
            _snackbarEvent.emit("Craving resisted! Ghost Counter updated (+1)")
            CigWidgetReceiver.updateAll(getApplication())
        }
    }

    // Edit Past Days: Add missed cigarette to past day
    fun logPastDay(daysAgo: Long, count: Int, isSkipped: Boolean = false) {
        viewModelScope.launch {
            val targetTimestamp = DateRangeUtils.getDaysAgoStartOfDayMillis(daysAgo) + (14 * 3600 * 1000) // approx 2 PM
            for (i in 0 until count) {
                repository.logManual(targetTimestamp, isSkipped)
            }
            _snackbarEvent.emit("Added $count log(s) to $daysAgo day(s) ago.")
            CigWidgetReceiver.updateAll(getApplication())
        }
    }

    fun undoLast() {
        viewModelScope.launch {
            val wasDeleted = repository.undoLast()
            if (wasDeleted) {
                _snackbarEvent.emit("Removed last entry.")
                CigWidgetReceiver.updateAll(getApplication())
            } else {
                _snackbarEvent.emit("No logs found to undo.")
            }
        }
    }

    fun savePrice() {
        when (val result = CalculationUtils.validatePrice(_priceInput.value)) {
            is PriceValidationResult.Error -> {
                _priceError.value = result.message
            }
            is PriceValidationResult.Success -> {
                viewModelScope.launch {
                    val app = getApplication<CigApp>()
                    app.preferencesRepository.setPricePerCigarette(result.value)
                    _priceError.value = null
                    _snackbarEvent.emit("Price saved: ${CalculationUtils.formatUnits(result.value)} units per cig")
                    CigWidgetReceiver.updateAll(getApplication())
                }
            }
        }
    }

    fun saveWishlist() {
        val rawTitle = _wishlistTitleInput.value.trim()
        val rawCost = _wishlistCostInput.value.toDoubleOrNull() ?: 0.0
        // If name or cost is left empty, save empty/0. Never save defaults like "My Goal" or 5000.
        val finalTitle = if (rawTitle.isEmpty() || rawCost <= 0.0) "" else rawTitle
        val finalCost = if (finalTitle.isEmpty() || rawCost <= 0.0) 0.0 else rawCost

        viewModelScope.launch {
            val app = getApplication<CigApp>()
            app.preferencesRepository.setWishlist(finalTitle, finalCost)
            if (finalTitle.isNotEmpty() && finalCost > 0.0) {
                _snackbarEvent.emit("Wishlist saved: $finalTitle (${CalculationUtils.formatUnits(finalCost)} units)")
            } else {
                _snackbarEvent.emit("Wishlist cleared")
            }
        }
    }

    fun clearWishlist() {
        _wishlistTitleInput.value = ""
        _wishlistCostInput.value = ""
        viewModelScope.launch {
            val app = getApplication<CigApp>()
            app.preferencesRepository.clearWishlist()
            _snackbarEvent.emit("Wishlist cleared")
        }
    }

    fun setWidgetTheme(theme: String) {
        viewModelScope.launch {
            val app = getApplication<CigApp>()
            app.preferencesRepository.setWidgetTheme(theme)
            _snackbarEvent.emit("Widget theme updated to ${if (theme == "NOTHING_WHITE") "Nothing Light" else "Nothing Dark"}")
            CigWidgetReceiver.updateAll(getApplication())
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            val app = getApplication<CigApp>()
            app.preferencesRepository.setDarkMode(isDark)
            _snackbarEvent.emit(if (isDark) "Switched to Dark Mode" else "Switched to Light Mode")
        }
    }

    fun setCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            val app = getApplication<CigApp>()
            app.preferencesRepository.setCurrency(code, symbol)
            _snackbarEvent.emit("Currency updated to $code ($symbol)")
            CigWidgetReceiver.updateAll(getApplication())
        }
    }

    fun generateDamageReportPdf(): File {
        val summary = habitSummary.value
        val app = getApplication<CigApp>()
        // Generate on-device PDF
        return DamageReportGenerator.generatePdfReport(
            context = app,
            logs = recentLogs.value,
            pricePerUnit = summary.pricePerUnit,
            
            liveSecondsSinceLast = liveSecondsSinceLast.value
        )
    }

    fun exportBackupJson(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onSuccess(json)
            _snackbarEvent.emit("Backup exported successfully (${json.length} bytes)")
        }
    }

    fun importBackupJson(jsonString: String) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            if (success) {
                _snackbarEvent.emit("Backup restored successfully!")
                CigWidgetReceiver.updateAll(getApplication())
            } else {
                _snackbarEvent.emit("Invalid backup JSON format.")
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAll()
            _snackbarEvent.emit("All cigarette history deleted.")
            CigWidgetReceiver.updateAll(getApplication())
        }
    }

    class Factory(private val app: CigApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CigViewModel::class.java)) {
                return CigViewModel(app, app.repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
