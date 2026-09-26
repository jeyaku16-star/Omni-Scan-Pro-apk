package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.BarcodeItem
import com.example.model.ExtractedTable
import com.example.model.KeyEntity
import com.example.model.OcrAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiVisionService {
    private val tag = "GeminiVisionService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        You are an advanced, enterprise-grade Document Scanner, OCR, Layout Reconstruction, and Barcode/QR Engine.
        Your primary goal is to extract text, tables, QR codes, and barcodes from uploaded images or PDFs with 100% accuracy while strictly preserving the original spatial layout and multi-column structure.

        CORE EXTRACTION RULES:
        1. Multi-Column & Layout Preservation:
           - Detect whether the document uses a single-column, multi-column (newspaper, academic paper, invoice, catalogue), or tabular layout.
           - NEVER merge text horizontally across separate visual columns unless it belongs to the same table row.
           - Reconstruct exact grid and column structures using Markdown tables (for preview/Word/PDF) and structured arrays (for Excel/CSV export).

        2. QR Code & Barcode Extraction:
           - Detect and decode all visible 1D Barcodes (UPC, EAN, Code 128, Code 39, ITF) and 2D Codes (QR Code, Data Matrix, PDF417, Aztec) in the image, whether standalone or embedded inside a document.
           - Extract the exact raw text, URL, serial number, SKU, vCard, or Wi-Fi payload encoded in the barcode/QR code, plus any human-readable numbers printed directly beneath or beside the barcode.
           - Identify the context of each code (e.g., "Product SKU", "Payment Link", "Tracking Number", "Invoice QR").

        3. High-Accuracy OCR & Pro Enhancement:
           - Accurately transcribe printed text, handwriting, stamps, headers, footers, and serial numbers.
           - Auto-correct obvious OCR artifacts caused by shadows, creases, or skewed angles while remaining faithful to the source wording.
           - Flag any illegible text or damaged/unreadable barcodes as [ILLEGIBLE] rather than guessing.

        4. Pro Smart Features:
           - Auto-Categorization: Identify the scan type (e.g., Standalone QR/Barcode, Shipping Label, Invoice, Receipt, ID Card, Contract, Multi-Column Document, Financial Table).
           - Key Entities Summary: Extract critical metadata (Decoded QR/Barcode values, Dates, Total Amounts, Names, Emails, Phone Numbers, Reference IDs).

        OUTPUT FORMAT:
        Always return your response in two parts:

        PART 1: VISUAL & CLIPBOARD READY (Markdown)
        - Scan Type & Confidence Score
        - Detected QR Codes & Barcodes (Listed clearly in a dedicated block for immediate one-click copy)
        - Executive Summary / Key Extracted Fields
        - Full Reconstructed Document (using exact Markdown tables for column/grid data and clean headings/paragraphs for prose).

        PART 2: EXPORT-READY DATA BLOCK (JSON)
        Provide a clean ```json block at the end with this exact schema:
        {
          "document_type": "string",
          "detected_language": "string",
          "layout_type": "single_column | multi_column | table_heavy | barcode_or_qr_only | mixed",
          "confidence_score": 0.98,
          "barcodes_and_qrcodes": [
            {
              "code_type": "QR_CODE | EAN_13 | UPC_A | CODE_128 | CODE_39 | DATA_MATRIX | OTHER",
              "decoded_text": "string (exact text, URL, or number to copy)",
              "printed_label_nearby": "string",
              "category": "URL | Text | Wi-Fi | Contact | Product_SKU | Tracking_ID | Payment_Link"
            }
          ],
          "summary": "string",
          "key_entities": [{"label": "string", "value": "string"}],
          "columns_or_tables": [
            {
              "table_name": "string",
              "headers": ["Col 1", "Col 2"],
              "rows": [["Val 1", "Val 2"]]
            }
          ],
          "formatted_html_for_word_and_pdf": "string (HTML with inline CSS)"
        }
    """.trimIndent()

    suspend fun analyzeDocument(
        bitmap: Bitmap,
        apiKeyOverride: String? = null,
        modelName: String = "gemini-3.5-flash"
    ): Result<OcrAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val key = apiKeyOverride?.takeIf { it.isNotBlank() }
                ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
                ?: return@withContext Result.failure(
                    IllegalStateException("Gemini API key is not configured. Please enter your API key in Settings or add it via AI Studio Secrets panel.")
                )

            val base64Image = bitmapToBase64(bitmap)
            val requestJson = JSONObject().apply {
                // systemInstruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                // contents
                val partsArray = JSONArray().apply {
                    put(JSONObject().put("text", "Please analyze this document image according to the rules and output both PART 1 (Markdown) and PART 2 (clean ```json codeblock)."))
                    put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Image)
                        })
                    })
                }
                put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))

                // generationConfig
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1) // Low temperature for high accuracy OCR & deterministic extraction
                    put("topP", 0.95)
                    put("maxOutputTokens", 8192)
                })
            }

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$key"
            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                Log.e(tag, "Gemini API error ($response.code): $errorBody")
                return@withContext Result.failure(Exception("API Error ${response.code}: $errorBody"))
            }

            val rawResponseText = response.body?.string().orEmpty()
            val parsedResult = parseGeminiResponse(rawResponseText)
            Result.success(parsedResult)
        } catch (e: Exception) {
            Log.e(tag, "Failed to analyze document", e)
            Result.failure(e)
        }
    }

    private fun parseGeminiResponse(jsonResponseText: String): OcrAnalysisResult {
        val root = JSONObject(jsonResponseText)
        val candidates = root.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")

        val fullTextBuilder = StringBuilder()
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i)
                fullTextBuilder.append(p?.optString("text", "").orEmpty())
            }
        }
        val fullText = fullTextBuilder.toString().trim()

        // Extract JSON codeblock
        val jsonPattern = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val jsonMatch = jsonPattern.findAll(fullText).lastOrNull()
        val jsonString = jsonMatch?.groupValues?.get(1)?.trim() ?: extractFirstJsonObject(fullText)

        // Part 1: Visual markdown is everything before the JSON block or the full response without the json block
        val markdownPart = if (jsonMatch != null) {
            val idx = fullText.indexOf(jsonMatch.value)
            if (idx > 0) fullText.substring(0, idx).trim() else fullText
        } else {
            fullText
        }

        return parseExportJson(jsonString, markdownPart)
    }

    private fun extractFirstJsonObject(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            text.substring(start, end + 1)
        } else {
            "{}"
        }
    }

    private fun parseExportJson(jsonString: String, markdown: String): OcrAnalysisResult {
        return try {
            val json = JSONObject(jsonString)
            val docType = json.optString("document_type", "Document")
            val confidence = json.optDouble("confidence_score", 0.98).toFloat()
            val language = json.optString("detected_language", "en")
            val layout = json.optString("layout_type", "single_column")
            val summary = json.optString("summary", "Document scan analysis completed.")
            val formattedHtml = json.optString("formatted_html_for_word_and_pdf", "")

            val barcodes = mutableListOf<BarcodeItem>()
            val barcodesArr = json.optJSONArray("barcodes_and_qrcodes") ?: JSONArray()
            for (i in 0 until barcodesArr.length()) {
                val b = barcodesArr.optJSONObject(i) ?: continue
                barcodes.add(
                    BarcodeItem(
                        codeType = b.optString("code_type", "OTHER"),
                        decodedText = b.optString("decoded_text", ""),
                        printedLabelNearby = b.optString("printed_label_nearby", ""),
                        category = b.optString("category", "Text")
                    )
                )
            }

            val keyEntities = mutableListOf<KeyEntity>()
            val entitiesArr = json.optJSONArray("key_entities") ?: JSONArray()
            for (i in 0 until entitiesArr.length()) {
                val e = entitiesArr.optJSONObject(i) ?: continue
                keyEntities.add(
                    KeyEntity(
                        label = e.optString("label", ""),
                        value = e.optString("value", "")
                    )
                )
            }

            val tables = mutableListOf<ExtractedTable>()
            val tablesArr = json.optJSONArray("columns_or_tables") ?: JSONArray()
            for (i in 0 until tablesArr.length()) {
                val t = tablesArr.optJSONObject(i) ?: continue
                val name = t.optString("table_name", "Table ${i + 1}")
                val headers = mutableListOf<String>()
                val hArr = t.optJSONArray("headers") ?: JSONArray()
                for (h in 0 until hArr.length()) headers.add(hArr.optString(h, ""))

                val rows = mutableListOf<List<String>>()
                val rArr = t.optJSONArray("rows") ?: JSONArray()
                for (r in 0 until rArr.length()) {
                    val rowArr = rArr.optJSONArray(r) ?: continue
                    val rowItems = mutableListOf<String>()
                    for (c in 0 until rowArr.length()) rowItems.add(rowArr.optString(c, ""))
                    rows.add(rowItems)
                }
                tables.add(ExtractedTable(name, headers, rows))
            }

            OcrAnalysisResult(
                documentType = docType,
                confidenceScore = confidence,
                detectedLanguage = language,
                layoutType = layout,
                barcodesAndQrcodes = barcodes,
                summary = summary,
                keyEntities = keyEntities,
                columnsOrTables = tables,
                formattedHtml = formattedHtml,
                rawMarkdown = markdown,
                rawJson = jsonString
            )
        } catch (e: Exception) {
            Log.e(tag, "JSON parsing fallback error: ${e.message}")
            OcrAnalysisResult(
                documentType = "Document",
                confidenceScore = 0.95f,
                detectedLanguage = "en",
                layoutType = "single_column",
                barcodesAndQrcodes = emptyList(),
                summary = "Scan completed. Raw output available in Markdown view.",
                keyEntities = emptyList(),
                columnsOrTables = emptyList(),
                formattedHtml = "",
                rawMarkdown = markdown,
                rawJson = jsonString
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Downscale large bitmaps to max dimension 1600 for optimal OCR balance and memory safety
        val maxDim = 1600
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            val newW = (bitmap.width * scale).toInt()
            val newH = (bitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
        val bytes = out.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
