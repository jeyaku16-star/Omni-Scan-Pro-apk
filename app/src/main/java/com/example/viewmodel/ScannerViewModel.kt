package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleDocPreset
import com.example.data.SampleDocuments
import com.example.data.ScanDatabase
import com.example.data.ScanRepository
import com.example.model.ExtractedTable
import com.example.model.OcrAnalysisResult
import com.example.model.ScanRecord
import com.example.network.GeminiVisionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ScanUiState {
    object Idle : ScanUiState
    data class Processing(val step: String, val progress: Float = 0.5f) : ScanUiState
    data class Success(
        val result: OcrAnalysisResult,
        val bitmap: Bitmap? = null,
        val savedId: Long? = null
    ) : ScanUiState
    data class Error(val message: String, val fallbackSample: SampleDocPreset? = null) : ScanUiState
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScanRepository
    private val visionService = GeminiVisionService()
    private val prefs = application.getSharedPreferences("omniscan_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _apiKeyOverride = MutableStateFlow(prefs.getString("custom_api_key", "").orEmpty())
    val apiKeyOverride: StateFlow<String> = _apiKeyOverride.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    val historyScans: StateFlow<List<ScanRecord>>

    init {
        val dao = ScanDatabase.getDatabase(application).scanDao()
        repository = ScanRepository(dao)

        historyScans = combine(repository.allScans, _searchQuery) { scans, query ->
            if (query.isBlank()) {
                scans
            } else {
                scans.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.documentType.contains(query, ignoreCase = true) ||
                    it.summary.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveCustomApiKey(key: String) {
        _apiKeyOverride.value = key.trim()
        prefs.edit().putString("custom_api_key", key.trim()).apply()
    }

    fun scanUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Processing("Loading high-resolution image...")
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val stream = getApplication<Application>().contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(stream)
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                processBitmapScan(bitmap, "Scanned Image")
            } else {
                _uiState.value = ScanUiState.Error("Unable to decode the selected image file.")
            }
        }
    }

    fun scanBitmap(bitmap: Bitmap, title: String? = null) {
        viewModelScope.launch {
            processBitmapScan(bitmap, title)
        }
    }

    fun scanUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Processing("Downloading document image from URL...")
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val connection = java.net.URL(url).openConnection()
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    val input = connection.getInputStream()
                    BitmapFactory.decodeStream(input)
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                processBitmapScan(bitmap, "Online Document")
            } else {
                _uiState.value = ScanUiState.Error("Failed to download or decode image from URL. Please check the link.")
            }
        }
    }

    private suspend fun processBitmapScan(bitmap: Bitmap, defaultTitle: String?) {
        _uiState.value = ScanUiState.Processing("Detecting multi-column layout, tables & barcodes...", 0.4f)

        val apiResult = visionService.analyzeDocument(
            bitmap = bitmap,
            apiKeyOverride = _apiKeyOverride.value
        )

        apiResult.fold(
            onSuccess = { analysis ->
                _uiState.value = ScanUiState.Processing("Saving reconstructed extraction to database...", 0.85f)
                val savedId = repository.saveScan(
                    result = analysis,
                    title = defaultTitle ?: "${analysis.documentType} Scan"
                )
                _uiState.value = ScanUiState.Success(
                    result = analysis,
                    bitmap = bitmap,
                    savedId = savedId
                )
            },
            onFailure = { error ->
                // Provide user with clear message and ready fallback preset
                val fallback = SampleDocuments.samples.firstOrNull()
                _uiState.value = ScanUiState.Error(
                    message = error.message ?: "OCR extraction failed. Check connection or API key.",
                    fallbackSample = fallback
                )
            }
        )
    }

    fun loadSample(sample: SampleDocPreset) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Processing("Simulating enterprise scan on '${sample.name}'...", 0.5f)
            val bitmap = withContext(Dispatchers.Default) {
                sample.generateDocumentBitmap()
            }
            // Save to DB
            val savedId = repository.saveScan(
                result = sample.precomputedResult,
                title = sample.name,
                thumbnailKey = sample.id
            )
            _uiState.value = ScanUiState.Success(
                result = sample.precomputedResult,
                bitmap = bitmap,
                savedId = savedId
            )
        }
    }

    fun loadHistoryRecord(record: ScanRecord) {
        viewModelScope.launch {
            val result = repository.parseRecordToResult(record)
            // If it corresponds to a sample, generate sample preview bitmap
            val sample = SampleDocuments.samples.find { it.id == record.thumbnailKey }
            val bitmap = sample?.generateDocumentBitmap()
            _uiState.value = ScanUiState.Success(
                result = result,
                bitmap = bitmap,
                savedId = record.id
            )
        }
    }

    fun returnToHome() {
        _uiState.value = ScanUiState.Idle
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
            if (_uiState.value is ScanUiState.Success && (_uiState.value as ScanUiState.Success).savedId == id) {
                _uiState.value = ScanUiState.Idle
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllScans()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "OmniScan Copied") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareContent(context: Context, content: String, title: String = "Share Document Data") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }
}
