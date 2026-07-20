package com.example.crattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.crattendance.ui.main.CRAttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailsScreen(
    viewModel: CRAttendanceViewModel,
    rrn: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val records by viewModel.attendanceRecords.collectAsState()

    val student = remember(students, rrn) { students.find { it.rrn == rrn } }
    val studentRecords = remember(records, rrn) { records.filter { it.studentRrn == rrn } }

    val totalClasses = studentRecords.size
    val presentCount = remember(studentRecords) { studentRecords.count { it.status == "Present" } }
    val absentCount = remember(studentRecords) { studentRecords.count { it.status == "Absent" } }
    val mlCount = remember(studentRecords) { studentRecords.count { it.status == "Medical Leave" } }
    val odCount = remember(studentRecords) { studentRecords.count { it.status == "On Duty" } }
    val lateCount = remember(studentRecords) { studentRecords.count { it.status == "Late" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(student?.name ?: "Student Details", fontWeight = FontWeight.Bold) },
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
        if (student == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Student not found.", style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Text(student.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("RRN: ${student.rrn}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!student.phone.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(student.phone, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text("Attendance Metrics", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))

            DetailStatRow("Total Periods", "$totalClasses")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DetailStatRow("Present", "$presentCount", MaterialTheme.colorScheme.primary)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DetailStatRow("Absent", "$absentCount", MaterialTheme.colorScheme.error)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DetailStatRow("Medical Leave", "$mlCount")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DetailStatRow("On Duty", "$odCount")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DetailStatRow("Late", "$lateCount")

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailStatRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
