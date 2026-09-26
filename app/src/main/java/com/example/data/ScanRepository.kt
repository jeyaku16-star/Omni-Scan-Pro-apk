package com.example.data

import com.example.model.BarcodeItem
import com.example.model.ExtractedTable
import com.example.model.KeyEntity
import com.example.model.OcrAnalysisResult
import com.example.model.ScanRecord
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class ScanRepository(private val scanDao: ScanDao) {
    val allScans: Flow<List<ScanRecord>> = scanDao.getAllScans()

    fun searchScans(query: String): Flow<List<ScanRecord>> = scanDao.searchScans(query)

    suspend fun getScanById(id: Long): ScanRecord? = scanDao.getScanById(id)

    suspend fun saveScan(
        result: OcrAnalysisResult,
        title: String? = null,
        thumbnailKey: String? = null
    ): Long {
        val displayTitle = title?.takeIf { it.isNotBlank() }
            ?: "${result.documentType} - ${result.barcodesAndQrcodes.firstOrNull()?.decodedText?.take(16) ?: result.keyEntities.firstOrNull()?.value?.take(16) ?: "Scan"}"

        val record = ScanRecord(
            title = displayTitle,
            timestamp = System.currentTimeMillis(),
            documentType = result.documentType,
            confidenceScore = result.confidenceScore,
            layoutType = result.layoutType,
            detectedLanguage = result.detectedLanguage,
            summary = result.summary,
            rawMarkdown = result.rawMarkdown,
            exportJson = result.rawJson,
            barcodeCount = result.barcodesAndQrcodes.size,
            tableCount = result.columnsOrTables.size,
            thumbnailKey = thumbnailKey
        )
        return scanDao.insertScan(record)
    }

    suspend fun deleteScan(id: Long) {
        scanDao.deleteScanById(id)
    }

    suspend fun deleteAllScans() {
        scanDao.deleteAllScans()
    }

    fun parseRecordToResult(record: ScanRecord): OcrAnalysisResult {
        return try {
            val json = JSONObject(record.exportJson)
            val docType = json.optString("document_type", record.documentType)
            val lang = json.optString("detected_language", record.detectedLanguage)
            val layout = json.optString("layout_type", record.layoutType)
            val summary = json.optString("summary", record.summary)
            val formattedHtml = json.optString("formatted_html_for_word_and_pdf", "")

            val barcodes = mutableListOf<BarcodeItem>()
            val barcodeArray = json.optJSONArray("barcodes_and_qrcodes") ?: JSONArray()
            for (i in 0 until barcodeArray.length()) {
                val obj = barcodeArray.optJSONObject(i) ?: continue
                barcodes.add(
                    BarcodeItem(
                        codeType = obj.optString("code_type", "OTHER"),
                        decodedText = obj.optString("decoded_text", ""),
                        printedLabelNearby = obj.optString("printed_label_nearby", ""),
                        category = obj.optString("category", "Text")
                    )
                )
            }

            val keyEntities = mutableListOf<KeyEntity>()
            val entitiesArray = json.optJSONArray("key_entities") ?: JSONArray()
            for (i in 0 until entitiesArray.length()) {
                val obj = entitiesArray.optJSONObject(i) ?: continue
                keyEntities.add(
                    KeyEntity(
                        label = obj.optString("label", ""),
                        value = obj.optString("value", "")
                    )
                )
            }

            val tables = mutableListOf<ExtractedTable>()
            val tablesArray = json.optJSONArray("columns_or_tables") ?: JSONArray()
            for (i in 0 until tablesArray.length()) {
                val obj = tablesArray.optJSONObject(i) ?: continue
                val tableName = obj.optString("table_name", "Table ${i + 1}")
                val headers = mutableListOf<String>()
                val headersArray = obj.optJSONArray("headers") ?: JSONArray()
                for (h in 0 until headersArray.length()) {
                    headers.add(headersArray.optString(h, ""))
                }
                val rows = mutableListOf<List<String>>()
                val rowsArray = obj.optJSONArray("rows") ?: JSONArray()
                for (r in 0 until rowsArray.length()) {
                    val rowArray = rowsArray.optJSONArray(r) ?: continue
                    val rowItems = mutableListOf<String>()
                    for (c in 0 until rowArray.length()) {
                        rowItems.add(rowArray.optString(c, ""))
                    }
                    rows.add(rowItems)
                }
                tables.add(ExtractedTable(tableName, headers, rows))
            }

            OcrAnalysisResult(
                documentType = docType,
                confidenceScore = record.confidenceScore,
                detectedLanguage = lang,
                layoutType = layout,
                barcodesAndQrcodes = barcodes,
                summary = summary,
                keyEntities = keyEntities,
                columnsOrTables = tables,
                formattedHtml = formattedHtml,
                rawMarkdown = record.rawMarkdown,
                rawJson = record.exportJson
            )
        } catch (e: Exception) {
            OcrAnalysisResult(
                documentType = record.documentType,
                confidenceScore = record.confidenceScore,
                detectedLanguage = record.detectedLanguage,
                layoutType = record.layoutType,
                barcodesAndQrcodes = emptyList(),
                summary = record.summary,
                keyEntities = emptyList(),
                columnsOrTables = emptyList(),
                formattedHtml = "",
                rawMarkdown = record.rawMarkdown,
                rawJson = record.exportJson
            )
        }
    }
}
