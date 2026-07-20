package com.example.crattendance.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.crattendance.ui.main.CRAttendanceViewModel
import com.example.crattendance.utils.ExportHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: CRAttendanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val collegeConfig by viewModel.collegeConfig.collectAsState()
    val students by viewModel.students.collectAsState()
    val records by viewModel.attendanceRecords.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var exportFormat by remember { mutableStateOf("PDF") }
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Export Format", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PDF", "Excel", "CSV").forEach { format ->
                        FilterChip(
                            selected = exportFormat == format,
                            onClick = { exportFormat = format },
                            label = { Text(format) },
                            leadingIcon = if (exportFormat == format) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("College", collegeConfig?.collegeName ?: "-")
                    InfoRow("Department", collegeConfig?.department ?: "-")
                    InfoRow("Course", collegeConfig?.course ?: "-")
                    InfoRow("Semester", "${collegeConfig?.semester ?: "-"} - ${collegeConfig?.section ?: ""}")
                    InfoRow("Students", "${students.size}")
                    InfoRow("Total Records", "${records.size}")
                    InfoRow("Generated", SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (students.isEmpty()) {
                        Toast.makeText(context, "No students to export", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isExporting = true
                    scope.launch {
                        try {
                            val title = "CR Attendance Report - ${collegeConfig?.course ?: "Class"}"
                            val headers = arrayOf("Student ID/RRN", "Student Name", "Present", "Absent", "Attendance %")
                            val rowData = students.map { student ->
                                val studRecs = records.filter { it.studentRrn == student.rrn }
                                val total = studRecs.size
                                val pres = studRecs.count { it.status == "Present" }
                                val abs = studRecs.count { it.status == "Absent" }
                                val pct = if (total == 0) "N/A" else "${(pres.toFloat() / total * 100).toInt()}%"
                                arrayOf(student.rrn, student.name, "$pres", "$abs", pct)
                            }
                            val infoList = listOf(
                                "College: ${collegeConfig?.collegeName ?: ""}",
                                "Department: ${collegeConfig?.department ?: ""}",
                                "Course: ${collegeConfig?.course ?: ""}",
                                "Semester: ${collegeConfig?.semester ?: ""} - ${collegeConfig?.section ?: ""}",
                                "Date: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())}"
                            )

                            val file = when (exportFormat) {
                                "PDF" -> ExportHelper.exportToPDF(context, "Attendance_Report", title, infoList, headers, rowData)
                                "Excel" -> ExportHelper.exportToExcel(context, "Attendance_Report", "Attendance", headers, rowData)
                                else -> ExportHelper.exportToCSV(context, "Attendance_Report", headers, rowData)
                            }

                            isExporting = false

                            if (file != null) {
                                Toast.makeText(context, "Report exported: ${file.name}", Toast.LENGTH_LONG).show()
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context, "${context.packageName}.provider", file
                                )
                                val mime = when (exportFormat) {
                                    "PDF" -> "application/pdf"
                                    "Excel" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    else -> "text/csv"
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = mime
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Report"))
                            } else {
                                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            isExporting = false
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exporting...")
                } else {
                    Icon(
                        imageVector = when (exportFormat) {
                            "PDF" -> Icons.Default.PictureAsPdf
                            "Excel" -> Icons.Default.TableChart
                            else -> Icons.Default.Share
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share Report", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
