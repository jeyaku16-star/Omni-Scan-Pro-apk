package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ProcessingOverlay
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScanResultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ScanUiState
import com.example.viewmodel.ScannerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OmniScanApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OmniScanApp(viewModel: ScannerViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyScans by viewModel.historyScans.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val apiKeyOverride by viewModel.apiKeyOverride.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ScanUiState.Idle -> {
                    HomeScreen(
                        historyScans = historyScans,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onScanBitmap = { bitmap, title -> viewModel.scanBitmap(bitmap, title) },
                        onScanUri = { uri -> viewModel.scanUri(uri) },
                        onScanUrl = { url -> viewModel.scanUrl(url) },
                        onSelectSample = { sample -> viewModel.loadSample(sample) },
                        onSelectHistory = { record -> viewModel.loadHistoryRecord(record) },
                        onDeleteHistory = { id -> viewModel.deleteScan(id) },
                        onOpenSettings = { showSettingsDialog = true }
                    )
                }

                is ScanUiState.Processing -> {
                    ProcessingOverlay(
                        step = state.step,
                        progress = state.progress
                    )
                }

                is ScanUiState.Success -> {
                    ScanResultScreen(
                        result = state.result,
                        sourceBitmap = state.bitmap,
                        activeTab = activeTab,
                        onTabSelected = { viewModel.setActiveTab(it) },
                        onBack = { viewModel.returnToHome() },
                        onCopy = { text, label -> viewModel.copyToClipboard(context, text, label) },
                        onShare = { text, title -> viewModel.shareContent(context, text, title) }
                    )
                }

                is ScanUiState.Error -> {
                    // Show error dialog with option to configure API key or try Enterprise Preset
                    AlertDialog(
                        onDismissRequest = { viewModel.returnToHome() },
                        title = { Text("Scan / OCR Notice") },
                        text = {
                            Text(
                                text = "${state.message}\n\nWould you like to configure your Gemini API Key in Settings, or load a realistic Enterprise Sample (Shipping Label / Invoice)?"
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (state.fallbackSample != null) {
                                    viewModel.loadSample(state.fallbackSample)
                                } else {
                                    showSettingsDialog = true
                                    viewModel.returnToHome()
                                }
                            }) {
                                Text(if (state.fallbackSample != null) "Load Sample Preset" else "Open Settings")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showSettingsDialog = true
                                viewModel.returnToHome()
                            }) {
                                Text("Configure Key")
                            }
                        }
                    )
                }
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    currentApiKey = apiKeyOverride,
                    onSaveApiKey = { key -> viewModel.saveCustomApiKey(key) },
                    onClearHistory = { viewModel.clearAllHistory() },
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}
