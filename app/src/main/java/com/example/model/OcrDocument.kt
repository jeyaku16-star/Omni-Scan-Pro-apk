package com.example.model

data class BarcodeItem(
    val codeType: String,
    val decodedText: String,
    val printedLabelNearby: String = "",
    val category: String = "Text" // URL, Text, Wi-Fi, Contact, Product_SKU, Tracking_ID, Payment_Link, etc.
)

data class KeyEntity(
    val label: String,
    val value: String
)

data class ExtractedTable(
    val tableName: String,
    val headers: List<String>,
    val rows: List<List<String>>
) {
    fun toCsv(): String {
        val sb = StringBuilder()
        sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")
        rows.forEach { row ->
            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
        }
        return sb.toString()
    }

    fun toTsv(): String {
        val sb = StringBuilder()
        sb.append(headers.joinToString("\t")).append("\n")
        rows.forEach { row ->
            sb.append(row.joinToString("\t")).append("\n")
        }
        return sb.toString()
    }

    fun toMarkdown(): String {
        val sb = StringBuilder()
        if (tableName.isNotBlank()) {
            sb.append("### ").append(tableName).append("\n\n")
        }
        sb.append("| ").append(headers.joinToString(" | ")).append(" |\n")
        sb.append("| ").append(headers.joinToString(" | ") { "---" }).append(" |\n")
        rows.forEach { row ->
            sb.append("| ").append(row.joinToString(" | ")).append(" |\n")
        }
        sb.append("\n")
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}

data class OcrAnalysisResult(
    val documentType: String,
    val confidenceScore: Float,
    val detectedLanguage: String,
    val layoutType: String, // single_column | multi_column | table_heavy | barcode_or_qr_only | mixed
    val barcodesAndQrcodes: List<BarcodeItem>,
    val summary: String,
    val keyEntities: List<KeyEntity>,
    val columnsOrTables: List<ExtractedTable>,
    val formattedHtml: String,
    val rawMarkdown: String,
    val rawJson: String
) {
    fun getFullClipboardText(): String {
        return buildString {
            append("PART 1: VISUAL & CLIPBOARD READY\n\n")
            append(rawMarkdown.trim())
            append("\n\n---\nPART 2: EXPORT-READY DATA BLOCK (JSON)\n\n")
            append("```json\n")
            append(rawJson.trim())
            append("\n```\n")
        }
    }

    fun getExportSummaryCsv(): String {
        val sb = StringBuilder()
        sb.append("Type,Field,Value\n")
        sb.append("Metadata,Document Type,").append(documentType).append("\n")
        sb.append("Metadata,Confidence,").append(String.format("%.1f%%", confidenceScore * 100)).append("\n")
        sb.append("Metadata,Layout Type,").append(layoutType).append("\n")
        sb.append("Metadata,Language,").append(detectedLanguage).append("\n")
        keyEntities.forEach { entity ->
            sb.append("Entity,\"").append(entity.label).append("\",\"").append(entity.value.replace("\"", "\"\"")).append("\"\n")
        }
        barcodesAndQrcodes.forEach { code ->
            sb.append("Code,\"").append(code.codeType).append(" (").append(code.category).append(")\",\"")
                .append(code.decodedText.replace("\"", "\"\"")).append("\"\n")
        }
        return sb.toString()
    }
}
