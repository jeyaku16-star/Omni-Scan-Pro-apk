package com.example.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.model.BarcodeItem
import com.example.model.ExtractedTable
import com.example.model.KeyEntity
import com.example.model.OcrAnalysisResult

data class SampleDocPreset(
    val id: String,
    val name: String,
    val category: String,
    val iconName: String,
    val description: String,
    val precomputedResult: OcrAnalysisResult
) {
    fun generateDocumentBitmap(): Bitmap {
        val width = 1000
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.rgb(250, 250, 252))

        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(220, 225, 235)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val textPaint = Paint().apply {
            color = Color.rgb(20, 30, 50)
            isAntiAlias = true
            textSize = 28f
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(10, 25, 60)
            isAntiAlias = true
            textSize = 42f
            isFakeBoldText = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(100, 115, 130)
            isAntiAlias = true
            textSize = 22f
        }
        val accentPaint = Paint().apply {
            color = Color.rgb(2, 132, 199) // Tech Blue
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Sheet margin
        canvas.drawRoundRect(RectF(40f, 40f, 960f, 1310f), 16f, 16f, bgPaint)
        canvas.drawRoundRect(RectF(40f, 40f, 960f, 1310f), 16f, 16f, borderPaint)

        when (id) {
            "shipping_label" -> {
                // Header block
                canvas.drawRect(RectF(40f, 40f, 960f, 160f), accentPaint)
                val whiteHeader = Paint(headerPaint).apply { color = Color.WHITE }
                canvas.drawText("EXPRESS GLOBAL LOGISTICS", 80f, 110f, whiteHeader)
                canvas.drawText("PRIORITY OVERNIGHT 10:30 AM", 80f, 140f, Paint(subPaint).apply { color = Color.WHITE })

                // Addresses
                canvas.drawText("SHIP FROM:", 80f, 210f, subPaint)
                canvas.drawText("Acme Technologies Inc. Hub #402", 80f, 245f, textPaint)
                canvas.drawText("742 Evergreen Terrace, Austin TX 78701", 80f, 280f, textPaint)
                canvas.drawText("Attn: Distribution Center", 80f, 315f, textPaint)

                canvas.drawLine(80f, 340f, 920f, 340f, borderPaint)

                canvas.drawText("SHIP TO:", 80f, 380f, subPaint)
                canvas.drawText("Stark Quantum Systems LLC", 80f, 420f, Paint(headerPaint).apply { textSize = 34f })
                canvas.drawText("10880 Wilshire Blvd, Suite 2100", 80f, 460f, textPaint)
                canvas.drawText("Los Angeles, CA 90024", 80f, 495f, textPaint)
                canvas.drawText("Ref: PO-2026-98124 | Contact: (310) 555-0199", 80f, 530f, subPaint)

                // 2D QR Code on the right
                drawSimulatedQr(canvas, 680f, 370f, 220f)
                canvas.drawText("SCAN FOR ROUTING", 710f, 620f, subPaint)

                // Routing Grid
                canvas.drawLine(80f, 650f, 920f, 650f, borderPaint)
                canvas.drawText("TRK # 7812 9401 2893", 80f, 710f, Paint(headerPaint).apply { textSize = 36f })
                canvas.drawText("ZONE 4 | WT: 4.8 LBS | DIM: 14x10x6 IN", 80f, 750f, subPaint)

                // 1D Barcode
                drawSimulatedBarcode(canvas, 100f, 800f, 800f, 140f)
                val monoPaint = Paint(textPaint).apply {
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("*TRK781294012893*", 500f, 980f, monoPaint)

                // Footer Box
                canvas.drawRect(RectF(80f, 1030f, 920f, 1240f), Paint().apply { color = Color.rgb(240, 243, 248) })
                canvas.drawText("CARRIER ROUTE: LAX-HUB-09 | SORT CODE: 90024-A1", 110f, 1080f, textPaint)
                canvas.drawText("HAZMAT: NO | DECLARED VAL: $1,450.00 USD", 110f, 1130f, textPaint)
                canvas.drawText("PRINTED: 2026-09-25 18:42:10 UTC", 110f, 1180f, subPaint)
            }
            "commercial_invoice" -> {
                // Corporate Invoice Banner
                canvas.drawText("INVOICE", 80f, 120f, headerPaint)
                canvas.drawText("INV-2026-09884 | DATE: SEP 25, 2026", 80f, 160f, subPaint)

                canvas.drawText("BILL TO:", 80f, 240f, subPaint)
                canvas.drawText("Nexus Global Corp", 80f, 275f, textPaint)
                canvas.drawText("VAT: EU389201948", 80f, 310f, subPaint)

                canvas.drawText("ISSUED BY:", 540f, 240f, subPaint)
                canvas.drawText("CloudMatrix Enterprise BV", 540f, 275f, textPaint)
                canvas.drawText("IBAN: NL91ABNA0417164300", 540f, 310f, subPaint)

                // Table Header
                canvas.drawRect(RectF(80f, 360f, 920f, 410f), Paint().apply { color = Color.rgb(235, 240, 248) })
                canvas.drawText("Item Description", 100f, 395f, Paint(textPaint).apply { isFakeBoldText = true })
                canvas.drawText("Qty", 540f, 395f, Paint(textPaint).apply { isFakeBoldText = true })
                canvas.drawText("Unit Price", 640f, 395f, Paint(textPaint).apply { isFakeBoldText = true })
                canvas.drawText("Total ($)", 800f, 395f, Paint(textPaint).apply { isFakeBoldText = true })

                // Rows
                val items = listOf(
                    Triple("High-Density Server Blade X1", "4", "$1,250.00"),
                    Triple("Enterprise Fiber SFP+ 10G", "16", "$85.00"),
                    Triple("Annual Platinum SLA Support", "1", "$2,400.00"),
                    Triple("L3 Managed Switch 48-Port", "2", "$950.00")
                )
                var y = 460f
                items.forEach { (desc, qty, price) ->
                    canvas.drawText(desc, 100f, y, textPaint)
                    canvas.drawText(qty, 550f, y, textPaint)
                    canvas.drawText(price, 650f, y, textPaint)
                    val totalStr = when (qty) {
                        "4" -> "$5,000.00"
                        "16" -> "$1,360.00"
                        "1" -> "$2,400.00"
                        else -> "$1,900.00"
                    }
                    canvas.drawText(totalStr, 810f, y, textPaint)
                    canvas.drawLine(80f, y + 20f, 920f, y + 20f, borderPaint)
                    y += 65f
                }

                // Totals
                canvas.drawText("Subtotal: $10,660.00", 680f, y + 40f, textPaint)
                canvas.drawText("VAT (10%): $1,066.00", 680f, y + 80f, textPaint)
                canvas.drawText("Total Due: $11,726.00", 640f, y + 130f, Paint(headerPaint).apply { textSize = 32f })

                // EPC Payment QR
                drawSimulatedQr(canvas, 100f, y + 20f, 180f)
                canvas.drawText("EPC QR Payment Code", 100f, y + 230f, subPaint)
            }
            "multi_column_paper" -> {
                // Title
                canvas.drawText("A Comparative Study on Neural OCR Architectures", 80f, 120f, Paint(headerPaint).apply { textSize = 36f })
                canvas.drawText("Dr. Elena Vance, Dr. Marcus Holloway | AI Systems Review 2026", 80f, 160f, subPaint)
                canvas.drawLine(80f, 190f, 920f, 190f, borderPaint)

                // Abstract Box
                canvas.drawRect(RectF(80f, 210f, 920f, 310f), Paint().apply { color = Color.rgb(245, 247, 250) })
                canvas.drawText("ABSTRACT: Layout-aware optical character recognition is essential for preserving document", 100f, 250f, subPaint)
                canvas.drawText("semantics across multi-column, table, and barcode elements in unstructured PDF streams.", 100f, 285f, subPaint)

                // 2 Columns divider
                canvas.drawLine(495f, 340f, 495f, 1250f, Paint(borderPaint).apply { strokeWidth = 1.5f })

                // Column 1
                canvas.drawText("1. INTRODUCTION", 80f, 370f, Paint(textPaint).apply { isFakeBoldText = true })
                val col1Lines = listOf(
                    "Standard OCR pipelines frequently commit",
                    "reading-order errors by greedily reading",
                    "across horizontal spans, thereby merging",
                    "independent columns into incoherent text.",
                    "",
                    "Our architecture introduces dual-head spatial",
                    "attention that segments layout geometry",
                    "prior to character tokenization. We achieve",
                    "state-of-the-art token reconstruction on",
                    "multi-column academic corpora.",
                    "",
                    "2. EXPERIMENTAL DESIGN",
                    "We evaluated 1,000 dense journal articles",
                    "spanning two-column and three-column grids.",
                    "Layout fidelity reached 99.4% precision."
                )
                var y1 = 410f
                col1Lines.forEach { line ->
                    if (line.startsWith("2.")) {
                        canvas.drawText(line, 80f, y1, Paint(textPaint).apply { isFakeBoldText = true })
                    } else {
                        canvas.drawText(line, 80f, y1, subPaint)
                    }
                    y1 += 34f
                }

                // Column 2
                canvas.drawText("3. SYSTEM ARCHITECTURE", 520f, 370f, Paint(textPaint).apply { isFakeBoldText = true })
                val col2Lines = listOf(
                    "The model ingests high-resolution rasters",
                    "at 300 DPI, generating bounding boxes for:",
                    " • Body text blocks & margins",
                    " • Embedded 1D/2D symbologies",
                    " • Grid-aligned data matrices",
                    "",
                    "Barcodes and QR matrices are handled by",
                    "a dedicated sub-decoder that runs in",
                    "parallel with the text sequence decoder.",
                    "",
                    "DOI: 10.1145/389201.2026.04",
                    "ISSN: 1941-0131 (Print)"
                )
                var y2 = 410f
                col2Lines.forEach { line ->
                    if (line.startsWith("3.")) {
                        canvas.drawText(line, 520f, y2, Paint(textPaint).apply { isFakeBoldText = true })
                    } else {
                        canvas.drawText(line, 520f, y2, subPaint)
                    }
                    y2 += 34f
                }

                // Mini Barcode for Journal ID at bottom right
                drawSimulatedBarcode(canvas, 520f, y2 + 30f, 380f, 70f)
                canvas.drawText("ISSN 1941-0131-2026", 520f, y2 + 130f, subPaint)
            }
            "restaurant_receipt" -> {
                // Receipt center aligned
                val centerPaint = Paint(headerPaint).apply {
                    textAlign = Paint.Align.CENTER
                    textSize = 34f
                }
                val subCenter = Paint(subPaint).apply {
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("THE BISTRO TABLE 14", 500f, 120f, centerPaint)
                canvas.drawText("450 Market Street, San Francisco CA", 500f, 160f, subCenter)
                canvas.drawText("Server: David M. | Check #9482 | Guests: 3", 500f, 195f, subCenter)
                canvas.drawText("Date: 25-SEP-2026 8:15 PM", 500f, 230f, subCenter)

                canvas.drawLine(150f, 260f, 850f, 260f, borderPaint)

                // Items Table
                val receiptItems = listOf(
                    Triple("2x Truffle Risotto", "2", "$56.00"),
                    Triple("1x Pan-Seared Salmon", "1", "$32.00"),
                    Triple("3x San Pellegrino 750ml", "3", "$21.00"),
                    Triple("1x Tiramisu Della Casa", "1", "$14.00")
                )
                var ry = 310f
                receiptItems.forEach { (name, _, total) ->
                    canvas.drawText(name, 160f, ry, textPaint)
                    canvas.drawText(total, 840f, ry, Paint(textPaint).apply { textAlign = Paint.Align.RIGHT })
                    ry += 50f
                }

                canvas.drawLine(150f, ry + 10f, 850f, ry + 10f, borderPaint)
                canvas.drawText("Subtotal: $123.00", 840f, ry + 60f, Paint(textPaint).apply { textAlign = Paint.Align.RIGHT })
                canvas.drawText("Sales Tax (8.625%): $10.61", 840f, ry + 100f, Paint(textPaint).apply { textAlign = Paint.Align.RIGHT })
                canvas.drawText("Suggested Tip (18%): $22.14", 840f, ry + 140f, Paint(textPaint).apply { textAlign = Paint.Align.RIGHT })
                canvas.drawText("TOTAL AMOUNT: $155.75", 840f, ry + 200f, Paint(headerPaint).apply {
                    textAlign = Paint.Align.RIGHT
                    textSize = 34f
                })

                // Barcode serial at bottom
                drawSimulatedBarcode(canvas, 200f, ry + 260f, 600f, 100f)
                canvas.drawText("CHK-9482-SF-2026", 500f, ry + 390f, subCenter)
            }
            else -> {
                canvas.drawText("DOCUMENT SCAN PREVIEW", 80f, 140f, headerPaint)
                canvas.drawText("Standard Enterprise Document Template", 80f, 190f, subPaint)
                drawSimulatedQr(canvas, 100f, 300f, 240f)
                drawSimulatedBarcode(canvas, 100f, 600f, 800f, 120f)
            }
        }

        return bitmap
    }

    private fun drawSimulatedBarcode(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        val barPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        var curX = x
        val endX = x + width
        var patternToggle = true
        while (curX < endX) {
            val barW = if (patternToggle) ((curX.toInt() % 7) + 2).toFloat() else ((curX.toInt() % 5) + 3).toFloat()
            if (patternToggle) {
                canvas.drawRect(curX, y, curX + barW, y + height, barPaint)
            }
            curX += barW + (if ((curX.toInt() % 3) == 0) 3f else 1.5f)
            patternToggle = !patternToggle
        }
    }

    private fun drawSimulatedQr(canvas: Canvas, x: Float, y: Float, size: Float) {
        val darkPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        val lightPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + size, y + size, lightPaint)
        canvas.drawRect(RectF(x, y, x + size, y + size), Paint().apply {
            color = Color.rgb(200, 210, 225)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        })

        // Draw 3 corner positioning boxes
        val boxSize = size * 0.28f
        fun drawCornerBox(bx: Float, by: Float) {
            canvas.drawRect(bx, by, bx + boxSize, by + boxSize, darkPaint)
            canvas.drawRect(bx + boxSize * 0.16f, by + boxSize * 0.16f, bx + boxSize * 0.84f, by + boxSize * 0.84f, lightPaint)
            canvas.drawRect(bx + boxSize * 0.32f, by + boxSize * 0.32f, bx + boxSize * 0.68f, by + boxSize * 0.68f, darkPaint)
        }

        drawCornerBox(x + 10f, y + 10f)
        drawCornerBox(x + size - boxSize - 10f, y + 10f)
        drawCornerBox(x + 10f, y + size - boxSize - 10f)

        // Random matrix dots in between
        val dotPaint = Paint(darkPaint)
        val step = size / 16f
        for (i in 5..14) {
            for (j in 5..14) {
                if ((i * 7 + j * 13) % 3 == 0) {
                    canvas.drawRect(x + i * step, y + j * step, x + (i + 1) * step - 2f, y + (j + 1) * step - 2f, dotPaint)
                }
            }
        }
    }
}

object SampleDocuments {
    val samples: List<SampleDocPreset> = listOf(
        SampleDocPreset(
            id = "shipping_label",
            name = "Express Shipping Label",
            category = "Shipping Label",
            iconName = "LocalShipping",
            description = "Priority overnight label with Code 128 barcode, routing QR code, addresses, and weight metrics.",
            precomputedResult = OcrAnalysisResult(
                documentType = "Shipping Label",
                confidenceScore = 0.992f,
                detectedLanguage = "en",
                layoutType = "mixed",
                barcodesAndQrcodes = listOf(
                    BarcodeItem(
                        codeType = "CODE_128",
                        decodedText = "TRK781294012893",
                        printedLabelNearby = "TRK # 7812 9401 2893",
                        category = "Tracking_ID"
                    ),
                    BarcodeItem(
                        codeType = "QR_CODE",
                        decodedText = "https://ship.expresslogistics.com/track/781294012893?hub=LAX-09",
                        printedLabelNearby = "SCAN FOR ROUTING",
                        category = "URL"
                    )
                ),
                summary = "Priority Overnight shipping label from Acme Technologies Inc. (Austin, TX) to Stark Quantum Systems LLC (Los Angeles, CA). Package weight 4.8 LBS, declared value $1,450.00 USD, scheduled for 10:30 AM delivery.",
                keyEntities = listOf(
                    KeyEntity("Tracking Number", "TRK781294012893"),
                    KeyEntity("Carrier", "Express Global Logistics"),
                    KeyEntity("Service Type", "Priority Overnight 10:30 AM"),
                    KeyEntity("Shipper", "Acme Technologies Inc. Hub #402"),
                    KeyEntity("Recipient", "Stark Quantum Systems LLC"),
                    KeyEntity("Destination Address", "10880 Wilshire Blvd, Suite 2100, Los Angeles, CA 90024"),
                    KeyEntity("Reference PO", "PO-2026-98124"),
                    KeyEntity("Package Weight", "4.8 LBS"),
                    KeyEntity("Dimensions", "14 x 10 x 6 IN"),
                    KeyEntity("Declared Value", "$1,450.00 USD"),
                    KeyEntity("Sort Code", "90024-A1")
                ),
                columnsOrTables = listOf(
                    ExtractedTable(
                        tableName = "Shipment Routing Grid",
                        headers = listOf("Segment", "Origin / Dest", "Carrier Route", "Sort Code", "Status"),
                        rows = listOf(
                            listOf("Origin", "Austin TX 78701", "AUS-HUB-02", "78701-HUB", "Dispatched"),
                            listOf("Transit", "Dallas Gateway", "DFW-AIR-08", "DFW-TRANS", "En Route"),
                            listOf("Destination", "Los Angeles CA 90024", "LAX-HUB-09", "90024-A1", "Scheduled")
                        )
                    )
                ),
                formattedHtml = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 2px solid #000; padding: 16px;">
                      <div style="background: #0284c7; color: #fff; padding: 12px; font-weight: bold; font-size: 20px;">
                        EXPRESS GLOBAL LOGISTICS - PRIORITY OVERNIGHT
                      </div>
                      <div style="display: flex; justify-content: space-between; margin-top: 12px; border-bottom: 1px solid #ccc; padding-bottom: 8px;">
                        <div>
                          <strong>SHIP TO:</strong><br/>
                          Stark Quantum Systems LLC<br/>
                          10880 Wilshire Blvd, Suite 2100<br/>
                          Los Angeles, CA 90024
                        </div>
                        <div style="text-align: right;">
                          <strong>ROUTING QR:</strong><br/>
                          [QR_CODE: LAX-09]
                        </div>
                      </div>
                      <div style="margin-top: 14px; text-align: center;">
                        <div style="font-size: 22px; font-weight: bold;">TRK # 7812 9401 2893</div>
                        <div style="letter-spacing: 4px; font-family: monospace; font-size: 18px; margin-top: 4px;">|| | |||| || ||| |||| || | ||</div>
                      </div>
                    </div>
                """.trimIndent(),
                rawMarkdown = """
                    # Scan Type: Shipping Label (Confidence: 99.2%)

                    ## Detected Barcodes & QR Codes
                    * **[CODE_128]** `TRK781294012893` (Context: Tracking Number)
                    * **[QR_CODE]** `https://ship.expresslogistics.com/track/781294012893?hub=LAX-09` (Context: Routing URL)

                    ## Executive Summary
                    Express Priority Overnight shipping label generated for delivery to Stark Quantum Systems in Los Angeles, CA. Package weight is 4.8 LBS with declared value $1,450.00 USD.

                    ## Key Extracted Fields
                    * **Shipper:** Acme Technologies Inc. (Austin, TX)
                    * **Recipient:** Stark Quantum Systems LLC (Los Angeles, CA 90024)
                    * **Tracking Number:** TRK781294012893
                    * **Delivery Commitment:** Tomorrow 10:30 AM
                    * **Reference PO:** PO-2026-98124

                    ## Full Reconstructed Document
                    | Segment | Location | Route Code | Status |
                    | --- | --- | --- | --- |
                    | Origin Hub | Austin TX 78701 | AUS-HUB-02 | Dispatched |
                    | Gateway | Dallas DFW | DFW-AIR-08 | En Route |
                    | Destination | Los Angeles CA 90024 | LAX-HUB-09 | Out for Delivery |
                """.trimIndent(),
                rawJson = """
                    {
                      "document_type": "Shipping Label",
                      "detected_language": "en",
                      "layout_type": "mixed",
                      "barcodes_and_qrcodes": [
                        {
                          "code_type": "CODE_128",
                          "decoded_text": "TRK781294012893",
                          "printed_label_nearby": "TRK # 7812 9401 2893",
                          "category": "Tracking_ID"
                        },
                        {
                          "code_type": "QR_CODE",
                          "decoded_text": "https://ship.expresslogistics.com/track/781294012893?hub=LAX-09",
                          "printed_label_nearby": "SCAN FOR ROUTING",
                          "category": "URL"
                        }
                      ],
                      "summary": "Priority Overnight shipping label from Acme Technologies to Stark Quantum Systems LLC in Los Angeles, CA.",
                      "key_entities": [
                        {"label": "Tracking Number", "value": "TRK781294012893"},
                        {"label": "Recipient", "value": "Stark Quantum Systems LLC"},
                        {"label": "Destination", "value": "Los Angeles, CA 90024"},
                        {"label": "Service Level", "value": "Priority Overnight 10:30 AM"},
                        {"label": "Weight", "value": "4.8 LBS"},
                        {"label": "Declared Value", "value": "$1,450.00 USD"}
                      ],
                      "columns_or_tables": [
                        {
                          "table_name": "Shipment Routing Grid",
                          "headers": ["Segment", "Location", "Route Code", "Status"],
                          "rows": [
                            ["Origin Hub", "Austin TX 78701", "AUS-HUB-02", "Dispatched"],
                            ["Gateway", "Dallas DFW", "DFW-AIR-08", "En Route"],
                            ["Destination", "Los Angeles CA 90024", "LAX-HUB-09", "Out for Delivery"]
                          ]
                        }
                      ],
                      "formatted_html_for_word_and_pdf": "<div style=\"font-family: Arial;\"><h3>Express Logistics Label</h3><p>TRK: TRK781294012893</p></div>"
                    }
                """.trimIndent()
            )
        ),
        SampleDocPreset(
            id = "commercial_invoice",
            name = "Commercial Tax Invoice",
            category = "Invoice",
            iconName = "ReceiptLong",
            description = "B2B commercial invoice featuring itemized product table, VAT breakdown, totals, and EPC QR payment code.",
            precomputedResult = OcrAnalysisResult(
                documentType = "Invoice",
                confidenceScore = 0.995f,
                detectedLanguage = "en",
                layoutType = "table_heavy",
                barcodesAndQrcodes = listOf(
                    BarcodeItem(
                        codeType = "QR_CODE",
                        decodedText = "BCD\n002\n1\nSCT\nABNANL2A\nCloudMatrix Enterprise BV\nNL91ABNA0417164300\nEUR11726.00\n\nINV-2026-09884",
                        printedLabelNearby = "EPC QR Payment Code",
                        category = "Payment_Link"
                    )
                ),
                summary = "B2B Tax Invoice #INV-2026-09884 from CloudMatrix Enterprise BV to Nexus Global Corp for IT infrastructure hardware and SLA support. Total payable EUR 11,726.00 including 10% VAT.",
                keyEntities = listOf(
                    KeyEntity("Invoice Number", "INV-2026-09884"),
                    KeyEntity("Invoice Date", "September 25, 2026"),
                    KeyEntity("Due Date", "October 25, 2026"),
                    KeyEntity("Seller", "CloudMatrix Enterprise BV"),
                    KeyEntity("Seller IBAN", "NL91ABNA0417164300"),
                    KeyEntity("Buyer", "Nexus Global Corp"),
                    KeyEntity("Buyer VAT", "EU389201948"),
                    KeyEntity("Subtotal", "$10,660.00"),
                    KeyEntity("VAT Rate", "10.0%"),
                    KeyEntity("VAT Amount", "$1,066.00"),
                    KeyEntity("Total Amount Due", "$11,726.00")
                ),
                columnsOrTables = listOf(
                    ExtractedTable(
                        tableName = "Line Items",
                        headers = listOf("Item Description", "Qty", "Unit Price", "Total Price"),
                        rows = listOf(
                            listOf("High-Density Server Blade X1", "4", "$1,250.00", "$5,000.00"),
                            listOf("Enterprise Fiber SFP+ 10G", "16", "$85.00", "$1,360.00"),
                            listOf("Annual Platinum SLA Support", "1", "$2,400.00", "$2,400.00"),
                            listOf("L3 Managed Switch 48-Port", "2", "$950.00", "$1,900.00")
                        )
                    ),
                    ExtractedTable(
                        tableName = "Tax Summary",
                        headers = listOf("Tax Rate", "Net Amount", "Tax Amount", "Gross Amount"),
                        rows = listOf(
                            listOf("VAT 10.0%", "$10,660.00", "$1,066.00", "$11,726.00")
                        )
                    )
                ),
                formattedHtml = """
                    <div style="font-family: Arial, sans-serif; max-width: 700px; margin: auto; padding: 20px; border: 1px solid #ddd;">
                      <h2 style="color: #0f172a; margin-bottom: 4px;">COMMERCIAL TAX INVOICE</h2>
                      <p style="color: #64748b; margin-top: 0;">Invoice #: INV-2026-09884 | Date: 2026-09-25</p>
                      <table style="width: 100%; border-collapse: collapse; margin-top: 16px;">
                        <thead>
                          <tr style="background: #f1f5f9; text-align: left;">
                            <th style="padding: 8px; border: 1px solid #cbd5e1;">Description</th>
                            <th style="padding: 8px; border: 1px solid #cbd5e1;">Qty</th>
                            <th style="padding: 8px; border: 1px solid #cbd5e1;">Unit Price</th>
                            <th style="padding: 8px; border: 1px solid #cbd5e1;">Total</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr><td style="padding: 8px; border: 1px solid #cbd5e1;">High-Density Server Blade X1</td><td style="padding: 8px; border: 1px solid #cbd5e1;">4</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$1,250.00</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$5,000.00</td></tr>
                          <tr><td style="padding: 8px; border: 1px solid #cbd5e1;">Enterprise Fiber SFP+ 10G</td><td style="padding: 8px; border: 1px solid #cbd5e1;">16</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$85.00</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$1,360.00</td></tr>
                          <tr><td style="padding: 8px; border: 1px solid #cbd5e1;">Annual Platinum SLA Support</td><td style="padding: 8px; border: 1px solid #cbd5e1;">1</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$2,400.00</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$2,400.00</td></tr>
                          <tr><td style="padding: 8px; border: 1px solid #cbd5e1;">L3 Managed Switch 48-Port</td><td style="padding: 8px; border: 1px solid #cbd5e1;">2</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$950.00</td><td style="padding: 8px; border: 1px solid #cbd5e1;">$1,900.00</td></tr>
                        </tbody>
                      </table>
                      <div style="text-align: right; margin-top: 16px;">
                        <p><strong>Subtotal:</strong> $10,660.00</p>
                        <p><strong>VAT (10%):</strong> $1,066.00</p>
                        <h3 style="color: #0284c7;">Total Due: $11,726.00</h3>
                      </div>
                    </div>
                """.trimIndent(),
                rawMarkdown = """
                    # Scan Type: Commercial Invoice (Confidence: 99.5%)

                    ## Detected QR Codes & Barcodes
                    * **[QR_CODE]** `BCD...NL91ABNA0417164300...EUR11726.00` (Context: EPC European Banking Payment Code)

                    ## Executive Summary
                    Commercial Invoice #INV-2026-09884 issued to Nexus Global Corp. Net total $10,660.00 with 10% VAT ($1,066.00) resulting in grand total $11,726.00 payable via SEPA credit transfer.

                    ## Key Extracted Fields
                    * **Invoice ID:** INV-2026-09884
                    * **Date:** September 25, 2026
                    * **Total Due:** $11,726.00
                    * **Seller IBAN:** NL91ABNA0417164300

                    ## Reconstructed Line Items
                    | Item Description | Qty | Unit Price | Total Price |
                    | --- | --- | --- | --- |
                    | High-Density Server Blade X1 | 4 | $1,250.00 | $5,000.00 |
                    | Enterprise Fiber SFP+ 10G | 16 | $85.00 | $1,360.00 |
                    | Annual Platinum SLA Support | 1 | $2,400.00 | $2,400.00 |
                    | L3 Managed Switch 48-Port | 2 | $950.00 | $1,900.00 |
                """.trimIndent(),
                rawJson = """
                    {
                      "document_type": "Invoice",
                      "detected_language": "en",
                      "layout_type": "table_heavy",
                      "barcodes_and_qrcodes": [
                        {
                          "code_type": "QR_CODE",
                          "decoded_text": "BCD\\n002\\n1\\nSCT\\nABNANL2A\\nCloudMatrix Enterprise BV\\nNL91ABNA0417164300\\nEUR11726.00\\n\\nINV-2026-09884",
                          "printed_label_nearby": "EPC QR Payment Code",
                          "category": "Payment_Link"
                        }
                      ],
                      "summary": "Commercial Tax Invoice #INV-2026-09884 from CloudMatrix Enterprise BV to Nexus Global Corp for IT infrastructure hardware and SLA support.",
                      "key_entities": [
                        {"label": "Invoice Number", "value": "INV-2026-09884"},
                        {"label": "Total Amount", "value": "$11,726.00"},
                        {"label": "Subtotal", "value": "$10,660.00"},
                        {"label": "VAT (10%)", "value": "$1,066.00"},
                        {"label": "Seller IBAN", "value": "NL91ABNA0417164300"},
                        {"label": "Buyer VAT", "value": "EU389201948"}
                      ],
                      "columns_or_tables": [
                        {
                          "table_name": "Line Items",
                          "headers": ["Item Description", "Qty", "Unit Price", "Total Price"],
                          "rows": [
                            ["High-Density Server Blade X1", "4", "$1,250.00", "$5,000.00"],
                            ["Enterprise Fiber SFP+ 10G", "16", "$85.00", "$1,360.00"],
                            ["Annual Platinum SLA Support", "1", "$2,400.00", "$2,400.00"],
                            ["L3 Managed Switch 48-Port", "2", "$950.00", "$1,900.00"]
                          ]
                        }
                      ],
                      "formatted_html_for_word_and_pdf": "<div>Invoice Table</div>"
                    }
                """.trimIndent()
            )
        ),
        SampleDocPreset(
            id = "multi_column_paper",
            name = "Multi-Column Research Paper",
            category = "Academic Paper",
            iconName = "MenuBook",
            description = "Academic paper with strict dual-column layout preservation, separate heading sequences, and embedded ISSN barcode.",
            precomputedResult = OcrAnalysisResult(
                documentType = "Multi-Column Document",
                confidenceScore = 0.988f,
                detectedLanguage = "en",
                layoutType = "multi_column",
                barcodesAndQrcodes = listOf(
                    BarcodeItem(
                        codeType = "EAN_13",
                        decodedText = "9781941013124",
                        printedLabelNearby = "ISSN 1941-0131-2026",
                        category = "Product_SKU"
                    )
                ),
                summary = "Academic research paper titled 'A Comparative Study on Neural OCR Architectures' by Dr. Vance and Dr. Holloway. Dual-column structure strictly segmented into Introduction (Col 1) and System Architecture (Col 2).",
                keyEntities = listOf(
                    KeyEntity("Paper Title", "A Comparative Study on Neural OCR Architectures"),
                    KeyEntity("Authors", "Dr. Elena Vance, Dr. Marcus Holloway"),
                    KeyEntity("Publication", "AI Systems Review 2026"),
                    KeyEntity("DOI", "10.1145/389201.2026.04"),
                    KeyEntity("ISSN", "1941-0131"),
                    KeyEntity("Layout Style", "Two-Column Parallel Grid")
                ),
                columnsOrTables = listOf(
                    ExtractedTable(
                        tableName = "Column 1 & 2 Text Streams",
                        headers = listOf("Section / Column", "Content Stream", "Token Count"),
                        rows = listOf(
                            listOf("Col 1 - Section 1", "Standard OCR pipelines frequently commit reading-order errors by greedily reading across horizontal spans...", "92 words"),
                            listOf("Col 1 - Section 2", "We evaluated 1,000 dense journal articles spanning two-column and three-column grids...", "68 words"),
                            listOf("Col 2 - Section 3", "The model ingests high-resolution rasters at 300 DPI, generating bounding boxes for body text...", "114 words")
                        )
                    )
                ),
                formattedHtml = """
                    <div style="font-family: 'Times New Roman', serif; max-width: 750px; margin: auto; padding: 20px;">
                      <h1 style="text-align: center; font-size: 22px;">A Comparative Study on Neural OCR Architectures</h1>
                      <p style="text-align: center; font-style: italic; color: #555;">Dr. Elena Vance, Dr. Marcus Holloway</p>
                      <hr style="margin: 16px 0;" />
                      <div style="display: flex; gap: 24px;">
                        <div style="flex: 1; text-align: justify; font-size: 13px; line-height: 1.5;">
                          <h3 style="font-size: 14px; text-transform: uppercase;">1. Introduction</h3>
                          <p>Standard OCR pipelines frequently commit reading-order errors by greedily reading across horizontal spans, thereby merging independent columns into incoherent text.</p>
                          <h3 style="font-size: 14px; text-transform: uppercase;">2. Experimental Design</h3>
                          <p>We evaluated 1,000 dense journal articles spanning two-column and three-column grids. Layout fidelity reached 99.4% precision.</p>
                        </div>
                        <div style="width: 1px; background: #ddd;"></div>
                        <div style="flex: 1; text-align: justify; font-size: 13px; line-height: 1.5;">
                          <h3 style="font-size: 14px; text-transform: uppercase;">3. System Architecture</h3>
                          <p>The model ingests high-resolution rasters at 300 DPI, generating bounding boxes for body text blocks, margins, and embedded 1D/2D symbologies.</p>
                          <p>Barcodes and QR matrices are handled by a dedicated sub-decoder that runs in parallel with text tokenization.</p>
                        </div>
                      </div>
                    </div>
                """.trimIndent(),
                rawMarkdown = """
                    # Scan Type: Multi-Column Document (Confidence: 98.8%)

                    ## Detected Barcodes
                    * **[EAN_13]** `9781941013124` (Context: ISSN Publication Barcode)

                    ## Executive Summary
                    Academic research publication formatted in standard dual-column IEEE/ACM style. Text reading order strictly isolated to prevent column cross-contamination.

                    ---

                    ## Full Reconstructed Document (Preserved Dual Columns)

                    ### Column 1: Introduction & Experiments
                    #### 1. INTRODUCTION
                    Standard OCR pipelines frequently commit reading-order errors by greedily reading across horizontal spans, thereby merging independent columns into incoherent text.

                    Our architecture introduces dual-head spatial attention that segments layout geometry prior to character tokenization. We achieve state-of-the-art token reconstruction on multi-column academic corpora.

                    #### 2. EXPERIMENTAL DESIGN
                    We evaluated 1,000 dense journal articles spanning two-column and three-column grids. Layout fidelity reached 99.4% precision.

                    ---

                    ### Column 2: Architecture & Results
                    #### 3. SYSTEM ARCHITECTURE
                    The model ingests high-resolution rasters at 300 DPI, generating bounding boxes for:
                    * Body text blocks & margins
                    * Embedded 1D/2D symbologies
                    * Grid-aligned data matrices

                    Barcodes and QR matrices are handled by a dedicated sub-decoder that runs in parallel with the text sequence decoder.

                    * DOI: `10.1145/389201.2026.04`
                    * ISSN: `1941-0131 (Print)`
                """.trimIndent(),
                rawJson = """
                    {
                      "document_type": "Multi-Column Document",
                      "detected_language": "en",
                      "layout_type": "multi_column",
                      "barcodes_and_qrcodes": [
                        {
                          "code_type": "EAN_13",
                          "decoded_text": "9781941013124",
                          "printed_label_nearby": "ISSN 1941-0131-2026",
                          "category": "Product_SKU"
                        }
                      ],
                      "summary": "Dual-column academic research paper on spatial layout reconstruction without horizontal text bleeding.",
                      "key_entities": [
                        {"label": "Title", "value": "A Comparative Study on Neural OCR Architectures"},
                        {"label": "Authors", "value": "Dr. Elena Vance, Dr. Marcus Holloway"},
                        {"label": "DOI", "value": "10.1145/389201.2026.04"}
                      ],
                      "columns_or_tables": [
                        {
                          "table_name": "Columns Comparison",
                          "headers": ["Column", "Heading", "Key Takeaway"],
                          "rows": [
                            ["Col 1", "Introduction", "Prevents reading-order horizontal span bleed"],
                            ["Col 2", "Architecture", "Dual-head layout geometry isolation"]
                          ]
                        }
                      ],
                      "formatted_html_for_word_and_pdf": "<div>Dual Column Article</div>"
                    }
                """.trimIndent()
            )
        ),
        SampleDocPreset(
            id = "restaurant_receipt",
            name = "Restaurant POS Receipt",
            category = "Receipt",
            iconName = "Restaurant",
            description = "Detailed hospitality check with tax calculation, itemized menu orders, tip guide, and POS barcode.",
            precomputedResult = OcrAnalysisResult(
                documentType = "Receipt",
                confidenceScore = 0.991f,
                detectedLanguage = "en",
                layoutType = "table_heavy",
                barcodesAndQrcodes = listOf(
                    BarcodeItem(
                        codeType = "CODE_39",
                        decodedText = "CHK-9482-SF-2026",
                        printedLabelNearby = "CHK-9482-SF-2026",
                        category = "Tracking_ID"
                    )
                ),
                summary = "Restaurant dinner receipt from The Bistro Table 14 in San Francisco, CA. Subtotal $123.00, tax $10.61, tip $22.14, final charged amount $155.75.",
                keyEntities = listOf(
                    KeyEntity("Merchant Name", "The Bistro"),
                    KeyEntity("Address", "450 Market Street, San Francisco CA"),
                    KeyEntity("Check Number", "#9482"),
                    KeyEntity("Date / Time", "2026-09-25 8:15 PM"),
                    KeyEntity("Server", "David M."),
                    KeyEntity("Guest Count", "3 Guests"),
                    KeyEntity("Subtotal", "$123.00"),
                    KeyEntity("Sales Tax", "$10.61"),
                    KeyEntity("Gratuity / Tip", "$22.14"),
                    KeyEntity("Total Charged", "$155.75")
                ),
                columnsOrTables = listOf(
                    ExtractedTable(
                        tableName = "Ordered Items",
                        headers = listOf("Item", "Quantity", "Price"),
                        rows = listOf(
                            listOf("Truffle Risotto", "2", "$56.00"),
                            listOf("Pan-Seared Salmon", "1", "$32.00"),
                            listOf("San Pellegrino 750ml", "3", "$21.00"),
                            listOf("Tiramisu Della Casa", "1", "$14.00")
                        )
                    )
                ),
                formattedHtml = """
                    <div style="font-family: monospace; max-width: 320px; margin: auto; padding: 12px; border: 1px dashed #666;">
                      <div style="text-align: center;">
                        <strong>THE BISTRO</strong><br/>
                        450 Market St, SF CA<br/>
                        Check #9482
                      </div>
                      <hr/>
                      <table style="width: 100%;">
                        <tr><td>2x Truffle Risotto</td><td style="text-align: right;">$56.00</td></tr>
                        <tr><td>1x Salmon</td><td style="text-align: right;">$32.00</td></tr>
                        <tr><td>3x Pellegrino</td><td style="text-align: right;">$21.00</td></tr>
                        <tr><td>1x Tiramisu</td><td style="text-align: right;">$14.00</td></tr>
                      </table>
                      <hr/>
                      <div style="text-align: right;">
                        Subtotal: $123.00<br/>
                        Tax: $10.61<br/>
                        Tip (18%): $22.14<br/>
                        <strong>Total: $155.75</strong>
                      </div>
                    </div>
                """.trimIndent(),
                rawMarkdown = """
                    # Scan Type: Receipt (Confidence: 99.1%)

                    ## Detected Barcodes
                    * **[CODE_39]** `CHK-9482-SF-2026` (Context: POS Check Tracking ID)

                    ## Executive Summary
                    Hospitality POS receipt for 3 guests at The Bistro Table 14. Total charged: $155.75.

                    ## Reconstructed Items
                    | Item | Qty | Price |
                    | --- | --- | --- |
                    | Truffle Risotto | 2 | $56.00 |
                    | Pan-Seared Salmon | 1 | $32.00 |
                    | San Pellegrino 750ml | 3 | $21.00 |
                    | Tiramisu Della Casa | 1 | $14.00 |
                """.trimIndent(),
                rawJson = """
                    {
                      "document_type": "Receipt",
                      "detected_language": "en",
                      "layout_type": "table_heavy",
                      "barcodes_and_qrcodes": [
                        {
                          "code_type": "CODE_39",
                          "decoded_text": "CHK-9482-SF-2026",
                          "printed_label_nearby": "CHK-9482-SF-2026",
                          "category": "Tracking_ID"
                        }
                      ],
                      "summary": "Restaurant POS receipt with itemized dishes, tax, and gratuity.",
                      "key_entities": [
                        {"label": "Merchant", "value": "The Bistro"},
                        {"label": "Check #", "value": "9482"},
                        {"label": "Total Amount", "value": "$155.75"}
                      ],
                      "columns_or_tables": [
                        {
                          "table_name": "Receipt Items",
                          "headers": ["Item", "Qty", "Price"],
                          "rows": [
                            ["Truffle Risotto", "2", "$56.00"],
                            ["Pan-Seared Salmon", "1", "$32.00"],
                            ["San Pellegrino 750ml", "3", "$21.00"],
                            ["Tiramisu Della Casa", "1", "$14.00"]
                          ]
                        }
                      ],
                      "formatted_html_for_word_and_pdf": "<div>Receipt</div>"
                    }
                """.trimIndent()
            )
        )
    )
}
