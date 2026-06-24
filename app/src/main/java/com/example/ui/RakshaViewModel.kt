package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.ScamScanEngine
import com.example.data.BlacklistItemEntity
import com.example.data.RakshaDatabase
import com.example.data.RakshaRepository
import com.example.data.ScamHistoryEntity
import com.example.data.ScamReportEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RakshaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RakshaRepository

    // Database Flows
    val historyState: StateFlow<List<ScamHistoryEntity>>
    val reportsState: StateFlow<List<ScamReportEntity>>
    val blacklistState: StateFlow<List<BlacklistItemEntity>>

    // UI Navigation State
    private val _currentScreen = MutableStateFlow("splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Language State: ENGLISH, HINDI, TELUGU
    private val _currentLanguage = MutableStateFlow("ENGLISH")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // User Profile State
    private val _userName = MutableStateFlow("Rajesh Kumar")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow("+91 98765 01234")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(true)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _userRole = MutableStateFlow("USER") // USER or ADMIN
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Active Scanning Loading States
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<ScamScanEngine.ScanResult?>(null)
    val scanResult: StateFlow<ScamScanEngine.ScanResult?> = _scanResult.asStateFlow()

    init {
        val database = RakshaDatabase.getDatabase(application)
        repository = RakshaRepository(database.scamDao())

        historyState = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        reportsState = repository.allReports.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        blacklistState = repository.blacklist.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.prepopulateBlacklistIfEmpty()
        }
    }

    // Navigation Helper
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        _scanResult.value = null // clear transient scan results when switching screens
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang.uppercase()
    }

    fun login(name: String, phone: String, role: String) {
        _userName.value = if (name.isBlank()) "User" else name
        _userPhone.value = if (phone.isBlank()) "+91 99999 88888" else phone
        _userRole.value = role.uppercase()
        _isLoggedIn.value = true
        _currentScreen.value = "home"
    }

    fun logout() {
        _isLoggedIn.value = false
        _userRole.value = "USER"
        _currentScreen.value = "login"
    }

    fun toggleBiometrics() {
        _isBiometricEnabled.value = !_isBiometricEnabled.value
    }

    // Core Scan Methods
    fun runCallScan(number: String, transcript: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            // Check local blacklist first for rapid detection!
            val trimmedNumber = number.replace(" ", "").trim()
            val blacklisted = repository.checkLocalBlacklist(trimmedNumber)

            val result = if (blacklisted != null) {
                // Offline hit
                ScamScanEngine.ScanResult(
                    type = "CALL",
                    input = "$number|$transcript",
                    riskLevel = "HIGH",
                    confidenceScore = 100,
                    category = "Offline Blacklist Alert",
                    explanation = "ALERT: This caller number is on the RakshaAI Local Blacklist. Reason: ${blacklisted.reason}",
                    language = _currentLanguage.value
                )
            } else {
                ScamScanEngine.performScan("CALL", "$number|$transcript", _currentLanguage.value)
            }

            // Save to Local DB
            repository.insertHistory(result.toEntity())
            _scanResult.value = result
            _isScanning.value = false
        }
    }

    fun runSmsScan(sender: String, message: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            val result = ScamScanEngine.performScan("SMS", "$sender|$message", _currentLanguage.value)
            repository.insertHistory(result.toEntity())
            _scanResult.value = result
            _isScanning.value = false
        }
    }

    fun runWhatsAppScan(text: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            val result = ScamScanEngine.performScan("WHATSAPP", text, _currentLanguage.value)
            repository.insertHistory(result.toEntity())
            _scanResult.value = result
            _isScanning.value = false
        }
    }

    fun runUrlScan(url: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            // Check local domain blacklist
            val cleanUrl = url.trim().lowercase()
            val blacklisted = repository.checkLocalBlacklist(cleanUrl)

            val result = if (blacklisted != null) {
                ScamScanEngine.ScanResult(
                    type = "URL",
                    input = url,
                    riskLevel = "HIGH",
                    confidenceScore = 100,
                    category = "Blacklisted Phishing Link",
                    explanation = "WARNING: This domain matches a confirmed phishing/scam website in our offline database. Description: ${blacklisted.reason}",
                    language = _currentLanguage.value
                )
            } else {
                ScamScanEngine.performScan("URL", url, _currentLanguage.value)
            }

            repository.insertHistory(result.toEntity())
            _scanResult.value = result
            _isScanning.value = false
        }
    }

    fun runPaymentScan(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            val result = ScamScanEngine.performScan("PAYMENT", "UPI_SCREENSHOT", _currentLanguage.value, bitmap)
            repository.insertHistory(result.toEntity())
            _scanResult.value = result
            _isScanning.value = false
        }
    }

    fun runSafetyExplainer(query: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            val result = ScamScanEngine.performScan("EXPLAINER", query, _currentLanguage.value)
            repository.insertHistory(result.toEntity())
            _scanResult.value = result
            _isScanning.value = false
        }
    }

    // Scam Reporting
    fun submitReport(reportType: String, target: String, category: String, description: String) {
        viewModelScope.launch {
            val report = ScamReportEntity(
                reportType = reportType,
                target = target,
                category = category,
                description = description
            )
            repository.insertReport(report)

            // If a number or url is reported, dynamically add it to the local blacklist so the user is immediately protected locally!
            if (reportType == "NUMBER" || reportType == "WEBSITE") {
                repository.insertBlacklistItem(
                    BlacklistItemEntity(
                        type = reportType,
                        value = target,
                        category = category,
                        reason = "User Reported: $description"
                    )
                )
            }
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteReportItem(id: Int) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    fun addToBlacklist(type: String, value: String, category: String, reason: String) {
        viewModelScope.launch {
            repository.insertBlacklistItem(
                BlacklistItemEntity(type = type, value = value, category = category, reason = reason)
            )
        }
    }

    fun deleteBlacklistItem(id: Int) {
        viewModelScope.launch {
            repository.deleteBlacklist(id)
        }
    }
}
