package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExtractedTable
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900

@Composable
fun TableGridRenderer(
    table: ExtractedTable,
    onCopy: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hScrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("table_grid_${table.tableName.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Table Title & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Table",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = table.tableName.ifBlank { "Structured Table" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${table.headers.size} Columns • ${table.rows.size} Rows",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Copy CSV Button
                FilledTonalButton(
                    onClick = { onCopy(table.toCsv(), "${table.tableName} CSV") },
                    modifier = Modifier.testTag("copy_csv_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy CSV",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy CSV", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontally Scrollable Spreadsheet Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                    .horizontalScroll(hScrollState)
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(Slate800)
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        table.headers.forEachIndexed { index, header ->
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 120.dp, max = 220.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Data Rows
                    table.rows.forEachIndexed { rowIndex, row ->
                        val isEven = rowIndex % 2 == 0
                        Row(
                            modifier = Modifier
                                .background(if (isEven) Slate900 else Slate850)
                                .border(
                                    width = 0.5.dp,
                                    color = Slate800.copy(alpha = 0.7f)
                                )
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            row.forEachIndexed { colIndex, cellValue ->
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 120.dp, max = 220.dp)
                                        .padding(horizontal = 8.dp)
                                        .clickable {
                                            onCopy(cellValue, "Cell: $cellValue")
                                        }
                                ) {
                                    Text(
                                        text = cellValue,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Format Exports
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCopy(table.toTsv(), "${table.tableName} TSV for Excel") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Copy Excel TSV", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { onCopy(table.toMarkdown(), "${table.tableName} Markdown") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Copy Markdown", fontSize = 11.sp)
                }
            }
        }
    }
}
