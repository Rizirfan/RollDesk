package com.example.crattendance.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.crattendance.data.database.AttendanceRecordEntity
import com.example.crattendance.data.database.StudentEntity
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

object ExportHelper {

    suspend fun exportToCSV(
        context: Context,
        fileName: String,
        headers: Array<String>,
        data: List<Array<String>>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "$fileName.csv")
            CSVWriter(FileWriter(file)).use { writer ->
                writer.writeNext(headers)
                data.forEach { row ->
                    writer.writeNext(row)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportToExcel(
        context: Context,
        fileName: String,
        sheetName: String,
        headers: Array<String>,
        data: List<Array<String>>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "$fileName.xlsx")
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(sheetName)

            // Header row
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // Data rows
            data.forEachIndexed { rowIndex, rowData ->
                val row = sheet.createRow(rowIndex + 1)
                rowData.forEachIndexed { colIndex, cellValue ->
                    val cell = row.createCell(colIndex)
                    cell.setCellValue(cellValue)
                }
            }

            FileOutputStream(file).use { out ->
                workbook.write(out)
            }
            workbook.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportToPDF(
        context: Context,
        fileName: String,
        title: String,
        infoList: List<String>,
        headers: Array<String>,
        data: List<Array<String>>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "$fileName.pdf")

            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            // Prepend S.No. to headers and auto-increment numbers to data rows
            val finalHeaders = arrayOf("S.No.") + headers
            val finalData = data.mapIndexed { idx, row ->
                arrayOf((idx + 1).toString()) + row
            }

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val primaryColor = 0xFF008080.toInt() // Teal accent color matching UI
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.rgb(220, 220, 220)
                strokeWidth = 0.8f
                style = Paint.Style.STROKE
            }
            val rectPaint = Paint().apply {
                isAntiAlias = true
            }

            var y = 40f

            // Document Header
            rectPaint.color = primaryColor
            canvas.drawRect(30f, y, 565f, y + 4f, rectPaint) // Accent top band
            y += 24f

            textPaint.apply {
                color = primaryColor
                textSize = 16f
                isFakeBoldText = true
            }
            canvas.drawText(title, 35f, y, textPaint)
            y += 20f

            // Metadata info list
            textPaint.apply {
                color = Color.DKGRAY
                textSize = 9f
                isFakeBoldText = false
            }
            infoList.forEach { info ->
                canvas.drawText(info, 35f, y, textPaint)
                y += 15f
            }
            y += 15f

            // Draw headers helper function
            fun drawTableHeaders(c: Canvas, currentY: Float) {
                rectPaint.color = Color.rgb(240, 243, 246)
                c.drawRect(30f, currentY, 565f, currentY + 22f, rectPaint) // Header background
                
                rectPaint.color = primaryColor
                c.drawRect(30f, currentY, 565f, currentY + 1f, rectPaint) // Header top border
                c.drawRect(30f, currentY + 22f, 565f, currentY + 23f, rectPaint) // Header bottom border

                textPaint.apply {
                    color = Color.BLACK
                    textSize = 9.5f
                    isFakeBoldText = true
                }
                
                var currentX = 35f
                // Distribute column widths. Give S.No. 40px, distribute the rest of 490px evenly
                val availableWidth = 490f
                val colWidth = availableWidth / (finalHeaders.size - 1)

                finalHeaders.forEachIndexed { index, header ->
                    c.drawText(header, currentX, currentY + 15f, textPaint)
                    currentX += if (index == 0) 45f else colWidth
                }
            }

            // Draw table headers for first page
            drawTableHeaders(canvas, y)
            y += 23f

            // Print rows
            textPaint.apply {
                color = Color.BLACK
                textSize = 9f
                isFakeBoldText = false
            }

            finalData.forEach { row ->
                if (y > 780f) {
                    // Save current page and spawn new page
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f
                    // Draw headers on the new page
                    drawTableHeaders(canvas, y)
                    y += 23f
                    textPaint.apply {
                        color = Color.BLACK
                        textSize = 9f
                        isFakeBoldText = false
                    }
                }

                // Row content drawing
                var currentX = 35f
                val colWidth = 490f / (finalHeaders.size - 1)
                
                row.forEachIndexed { colIdx, cell ->
                    val truncatedCell = if (colIdx > 0 && cell.length > 24) cell.substring(0, 21) + "..." else cell
                    canvas.drawText(truncatedCell, currentX, y + 14f, textPaint)
                    currentX += if (colIdx == 0) 45f else colWidth
                }

                // Row divider line
                canvas.drawLine(30f, y + 20f, 565f, y + 20f, linePaint)
                y += 20f
            }

            pdfDocument.finishPage(page)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
