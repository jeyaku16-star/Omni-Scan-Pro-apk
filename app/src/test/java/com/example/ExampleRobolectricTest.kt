package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleDocuments
import com.example.model.BarcodeItem
import com.example.model.ExtractedTable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OmniScan", appName)
  }

  @Test
  fun `verify sample presets exist and are populated`() {
    val presets = SampleDocuments.samples
    assertTrue(presets.isNotEmpty())
    val shipping = presets.find { it.id == "shipping_label" }
    assertTrue(shipping != null)
    assertEquals("Shipping Label", shipping?.precomputedResult?.documentType)
    assertTrue(shipping?.precomputedResult?.barcodesAndQrcodes?.isNotEmpty() == true)
  }

  @Test
  fun `verify table csv conversion`() {
    val table = ExtractedTable(
      tableName = "Test Table",
      headers = listOf("Col A", "Col B"),
      rows = listOf(
        listOf("Val 1", "Val 2"),
        listOf("Comma, Value", "Quote \"Value\"")
      )
    )
    val csv = table.toCsv()
    assertTrue(csv.contains("Col A,Col B"))
    assertTrue(csv.contains("\"Comma, Value\""))
    assertTrue(csv.contains("\"Quote \"\"Value\"\"\""))
  }
}
