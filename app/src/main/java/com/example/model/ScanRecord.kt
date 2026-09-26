package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val documentType: String,
    val confidenceScore: Float,
    val layoutType: String,
    val detectedLanguage: String,
    val summary: String,
    val rawMarkdown: String,
    val exportJson: String,
    val barcodeCount: Int,
    val tableCount: Int,
    val thumbnailKey: String? = null // Sample key or local image path
)
