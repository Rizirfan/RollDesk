package com.example.crattendance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.crattendance.data.database.CollegeConfigEntity
import com.example.crattendance.ui.main.CRAttendanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CRAttendanceViewModel,
    onBack: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val collegeConfig by viewModel.collegeConfig.collectAsState()
    val students by viewModel.students.collectAsState()
    val records by viewModel.attendanceRecords.collectAsState()
    val threshold by viewModel.attendanceThreshold.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var collegeName by remember(collegeConfig) { mutableStateOf(collegeConfig?.collegeName ?: "") }
    var department by remember(collegeConfig) { mutableStateOf(collegeConfig?.department ?: "") }
    var course by remember(collegeConfig) { mutableStateOf(collegeConfig?.course ?: "") }
    var semester by remember(collegeConfig) { mutableStateOf(collegeConfig?.semester ?: "") }
    var section by remember(collegeConfig) { mutableStateOf(collegeConfig?.section ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            // Appearance — flat theme selector
            Text("Appearance", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))

            val options = listOf(
                Triple(0, "System Default", "Follow system setting"),
                Triple(1, "Light", "Always use light theme"),
                Triple(2, "Dark", "Always use dark theme")
            )
            options.forEachIndexed { idx, (mode, label, description) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setThemeMode(mode) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (themeMode == mode) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (idx < options.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // College Profile
            Text("College Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = collegeName,
                onValueChange = { collegeName = it },
                label = { Text("College Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                label = { Text("Course") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it },
                    label = { Text("Semester") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (collegeName.isNotBlank()) {
                        viewModel.saveCollegeConfig(
                            CollegeConfigEntity(
                                collegeName = collegeName.trim(),
                                department = department.trim(),
                                course = course.trim(),
                                semester = semester.trim(),
                                section = section.trim()
                            )
                        )
                        Toast.makeText(context, "College profile saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "College name is required", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            var dangerZoneTaps by remember { mutableStateOf(0) }
            val isDangerZoneRevealed = dangerZoneTaps >= 7

            // Stats — flat rows
            Text(
                "Database Stats",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) {
                    dangerZoneTaps++
                    if (dangerZoneTaps in 4..6) {
                        Toast.makeText(context, "Tap ${7 - dangerZoneTaps} more times to reveal Developer options", Toast.LENGTH_SHORT).show()
                    } else if (dangerZoneTaps == 7) {
                        Toast.makeText(context, "Developer options unlocked!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlatInfoRow("Students", "${students.size}")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FlatInfoRow("Attendance Records", "${records.size}")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FlatInfoRow("Threshold", "${threshold}%")

            Spacer(modifier = Modifier.height(20.dp))

            // Export Roster Options
            Text("Export Student Roster", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            val scope = rememberCoroutineScope()
            val electiveStudents by viewModel.electiveStudents.collectAsState()

            var showRosterActionDialog by remember { mutableStateOf(false) }
            var activeExportFile by remember { mutableStateOf<java.io.File?>(null) }
            var exportTitleText by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Export All Students
                Button(
                    onClick = {
                        if (students.isEmpty()) {
                            Toast.makeText(context, "No students to export", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            try {
                                val headers = arrayOf("RRN", "Student Name")
                                val rowData = students.map { arrayOf(it.rrn, it.name) }
                                val infoList = listOf(
                                    "Export Type: Complete Class Roster",
                                    "Total Students: ${students.size}",
                                    "Generated: ${java.text.SimpleDateFormat("d MMMM yyyy, h:mm a", java.util.Locale.getDefault()).format(java.util.Date())}"
                                )
                                val file = com.example.crattendance.utils.ExportHelper.exportToPDF(
                                    context = context,
                                    fileName = "Complete_Roster_Export",
                                    title = "Complete Student Roster",
                                    infoList = infoList,
                                    headers = headers,
                                    data = rowData
                                )
                                if (file != null) {
                                    activeExportFile = file
                                    exportTitleText = "Complete Roster"
                                    showRosterActionDialog = true
                                } else {
                                    Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export All",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }

                // Export Electives Configuration
                Button(
                    onClick = {
                        if (electiveStudents.isEmpty()) {
                            Toast.makeText(context, "No elective students configured", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            try {
                                val headers = arrayOf("Elective Name", "RRN", "Student Name")
                                val rowData = electiveStudents.map { es ->
                                    val match = students.find { it.rrn == es.studentRrn }
                                    arrayOf(es.electiveName, es.studentRrn, match?.name ?: "Unknown")
                                }
                                val infoList = listOf(
                                    "Export Type: Elective Enrolled Students Roster",
                                    "Total Enrolled Records: ${electiveStudents.size}",
                                    "Generated: ${java.text.SimpleDateFormat("d MMMM yyyy, h:mm a", java.util.Locale.getDefault()).format(java.util.Date())}"
                                )
                                val file = com.example.crattendance.utils.ExportHelper.exportToPDF(
                                    context = context,
                                    fileName = "Elective_Students_Roster",
                                    title = "Elective Enrolled Students Roster",
                                    infoList = infoList,
                                    headers = headers,
                                    data = rowData
                                )
                                if (file != null) {
                                    activeExportFile = file
                                    exportTitleText = "Electives Roster"
                                    showRosterActionDialog = true
                                } else {
                                    Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export Elective",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            if (showRosterActionDialog && activeExportFile != null) {
                AlertDialog(
                    onDismissRequest = { showRosterActionDialog = false; activeExportFile = null },
                    title = { Text("PDF Generated Successfully") },
                    text = { Text("How would you like to handle the exported $exportTitleText PDF?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                activeExportFile?.let { file ->
                                    val fileUri = androidx.core.content.FileProvider.getUriForFile(
                                        context, "${context.packageName}.provider", file
                                    )
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(fileUri, "application/pdf")
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No PDF viewer found. Please install one.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showRosterActionDialog = false
                                activeExportFile = null
                            }
                        ) {
                            Text("Open / View")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                activeExportFile?.let { file ->
                                    val fileUri = androidx.core.content.FileProvider.getUriForFile(
                                        context, "${context.packageName}.provider", file
                                    )
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Share PDF"))
                                }
                                showRosterActionDialog = false
                                activeExportFile = null
                            }
                        ) {
                            Text("Share PDF")
                        }
                    }
                )
            }

            if (isDangerZoneRevealed) {
                Spacer(modifier = Modifier.height(24.dp))

                // Danger zone — flat
                Text("Danger Zone", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Clear All Data",
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Wipe all students, timetable, and attendance records.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Data & Start Fresh")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Clear All Data?") },
                text = { Text("This will delete everything and redirect you to the setup page. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetAllData()
                            showResetDialog = false
                            onClearAllData()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Yes, Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun FlatInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
