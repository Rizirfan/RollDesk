package com.example.crattendance.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crattendance.data.database.AttendanceRecordEntity
import com.example.crattendance.theme.AttendanceStatus
import com.example.crattendance.theme.Teal
import com.example.crattendance.ui.main.CRAttendanceViewModel
import com.example.crattendance.utils.ExportHelper
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TakeAttendanceScreen(
    viewModel: CRAttendanceViewModel,
    isElective: Boolean = false,
    electiveName: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by viewModel.students.collectAsState()
    val electiveStudents by viewModel.electiveStudents.collectAsState()
    val timetable by viewModel.timetable.collectAsState()

    val students = remember(allStudents, electiveStudents, isElective, electiveName) {
        if (isElective && !electiveName.isNullOrEmpty()) {
            val enrolledRrns = electiveStudents
                .filter { it.electiveName == electiveName }
                .map { it.studentRrn }
                .toSet()
            allStudents.filter { it.rrn in enrolledRrns }
        } else {
            allStudents
        }
    }

    var currentStep by remember { mutableIntStateOf(1) }
    var showRosterActionDialog by remember { mutableStateOf(false) }
    var activeExportFile by remember { mutableStateOf<File?>(null) }
    var exportTitleText by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }
    var selectedDateStr by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    val dayOfWeek = remember(selectedDateStr) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val d = sdf.parse(selectedDateStr)
            if (d != null) {
                val cal = Calendar.getInstance()
                cal.time = d
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    else -> 7
                }
            } else 1
        } catch (_: Exception) { 1 }
    }

    val todayPeriods = remember(timetable, dayOfWeek) {
        timetable.filter { it.dayOfWeek == dayOfWeek }.sortedBy { it.period }
    }

    var selectedPeriodIndex by remember { mutableIntStateOf(0) }
    var selectedPeriodNum by remember { mutableIntStateOf(1) }
    var selectedSubjectName by remember { mutableStateOf(if (isElective) "Linux" else "Period") }

    val attendanceMap = remember { mutableStateMapOf<String, AttendanceStatus>() }

    LaunchedEffect(students) {
        students.forEach { s ->
            if (!attendanceMap.containsKey(s.rrn)) {
                attendanceMap[s.rrn] = AttendanceStatus.PRESENT
            }
        }
    }

    LaunchedEffect(todayPeriods, selectedPeriodIndex) {
        if (!isElective) {
            if (todayPeriods.isNotEmpty() && selectedPeriodIndex < todayPeriods.size) {
                selectedSubjectName = todayPeriods[selectedPeriodIndex].subjectName
                selectedPeriodNum = todayPeriods[selectedPeriodIndex].period
            } else {
                selectedPeriodNum = selectedPeriodIndex + 1
                selectedSubjectName = "Period $selectedPeriodNum"
            }
        }
    }

    val presentCount = attendanceMap.values.count { it == AttendanceStatus.PRESENT }
    val absentCount = attendanceMap.values.count { it == AttendanceStatus.ABSENT }
    val otherCount = students.size - presentCount - absentCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (currentStep > 1) currentStep-- else onBack()
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isElective) "Elective Attendance" else if (currentStep == 1) "Session Setup" else "Roll Call",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Step $currentStep of 2",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Step indicator dots in right corner
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (currentStep >= 1) Teal else MaterialTheme.colorScheme.outline)
                )
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(if (currentStep >= 2) Teal else MaterialTheme.colorScheme.outline)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (currentStep >= 2) Teal else MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No active students found. Import students first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        if (isElective && currentStep == 1) {
            // Elective: just date + subject, go straight to roll call
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDateStr,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            val parts = selectedDateStr.split("-")
                            android.app.DatePickerDialog(context, { _, y, m, d ->
                                val cal = java.util.Calendar.getInstance()
                                cal.set(y, m, d)
                                selectedDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
                            }, parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change", style = MaterialTheme.typography.labelSmall)
                    }
                }

                OutlinedTextField(
                    value = selectedSubjectName,
                    onValueChange = { selectedSubjectName = it },
                    label = { Text("Elective Subject Name", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { currentStep = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Start Roll Call", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        } else when (currentStep) {
            1 -> Step1SessionSetup(
                selectedDateStr = selectedDateStr,
                onDateChange = { selectedDateStr = it },
                todayPeriods = todayPeriods,
                selectedPeriodIndex = selectedPeriodIndex,
                onPeriodSelect = { idx, period, subject ->
                    selectedPeriodIndex = idx
                    selectedPeriodNum = period
                    selectedSubjectName = subject
                },
                subjectName = selectedSubjectName,
                onSubjectChange = { selectedSubjectName = it },
                onNext = { currentStep = 2 },
                context = context
            )
            2 -> Step2RollCall(
                students = students,
                attendanceMap = attendanceMap,
                presentCount = presentCount,
                absentCount = absentCount,
                otherCount = otherCount,
                totalStudents = students.size,
                selectedDateStr = selectedDateStr,
                selectedPeriodNum = selectedPeriodNum,
                selectedSubjectName = selectedSubjectName,
                onMarkAllPresent = {
                    students.forEach { s -> attendanceMap[s.rrn] = AttendanceStatus.PRESENT }
                },
                onSave = {
                    val timestamp = System.currentTimeMillis()
                    if (isElective && !electiveName.isNullOrEmpty()) {
                        val recordList = students.map { s ->
                            com.example.crattendance.data.database.ElectiveAttendanceRecordEntity(
                                studentRrn = s.rrn,
                                electiveName = electiveName,
                                date = selectedDateStr,
                                subject = selectedSubjectName,
                                status = (attendanceMap[s.rrn] ?: AttendanceStatus.PRESENT).displayName,
                                timestamp = timestamp
                            )
                        }
                        viewModel.saveElectiveAttendance(recordList)
                    } else {
                        val recordList = students.map { s ->
                            AttendanceRecordEntity(
                                studentRrn = s.rrn,
                                date = selectedDateStr,
                                period = selectedPeriodNum,
                                subject = selectedSubjectName,
                                status = (attendanceMap[s.rrn] ?: AttendanceStatus.PRESENT).displayName,
                                timestamp = timestamp
                            )
                        }
                        viewModel.saveAttendance(recordList)
                    }
                    Toast.makeText(context, "Attendance saved", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                onShareText = {
                    val sb = StringBuilder()
                    sb.append("*Attendance Report*\n")
                    sb.append("Date: $selectedDateStr\n")
                    sb.append("Period: $selectedPeriodNum ($selectedSubjectName)\n\n")
                    sb.append("*Absentees ($absentCount):*\n")
                    val absents = students.filter { attendanceMap[it.rrn] == AttendanceStatus.ABSENT }
                    if (absents.isEmpty()) sb.append("Nil")
                    else absents.forEach { sb.append("- ${it.rrn} (${it.name})\n") }

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                    }
                    context.startActivity(Intent.createChooser(intent, "Share via"))
                },
                onSharePdf = {
                    try {
                        val absents = students.filter { attendanceMap[it.rrn] == AttendanceStatus.ABSENT }
                        val headers = arrayOf("RRN", "Student Name")
                        val rowData = absents.map { s ->
                            arrayOf(s.rrn, s.name)
                        }
                        val infoList = listOf(
                            "Date: $selectedDateStr",
                            "Period: $selectedPeriodNum",
                            "Subject: $selectedSubjectName",
                            "Total Students: ${students.size}",
                            "Present: $presentCount",
                            "Absent: ${absents.size}"
                        )

                        viewModel.viewModelScope.launch {
                            val file = ExportHelper.exportToPDF(
                                context = context,
                                fileName = "Attendance_${selectedDateStr}_P$selectedPeriodNum",
                                title = "Absentees Report",
                                infoList = infoList,
                                headers = headers,
                                data = rowData
                            )
                            if (file != null) {
                                activeExportFile = file
                                exportTitleText = "Attendance P$selectedPeriodNum"
                                showRosterActionDialog = true
                            } else {
                                Toast.makeText(context, "Failed to build PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "PDF error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        if (showRosterActionDialog && activeExportFile != null) {
            AlertDialog(
                onDismissRequest = { showRosterActionDialog = false; activeExportFile = null },
                title = { Text("PDF Generated Successfully") },
                text = { Text("How would you like to handle the attendance PDF report?") },
                confirmButton = {
                    Button(
                        onClick = {
                            activeExportFile?.let { file ->
                                val fileUri = androidx.core.content.FileProvider.getUriForFile(
                                    context, "${context.packageName}.provider", file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(fileUri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, fileUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share PDF"))
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
    }
}

@Composable
private fun Step1SessionSetup(
    selectedDateStr: String,
    onDateChange: (String) -> Unit,
    todayPeriods: List<com.example.crattendance.data.database.TimetableEntity>,
    selectedPeriodIndex: Int,
    onPeriodSelect: (index: Int, period: Int, subject: String) -> Unit,
    subjectName: String,
    onSubjectChange: (String) -> Unit,
    onNext: () -> Unit,
    context: android.content.Context,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Date row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDateStr,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    val parts = selectedDateStr.split("-")
                    DatePickerDialog(context, { _, y, m, d ->
                        val cal = Calendar.getInstance()
                        cal.set(y, m, d)
                        onDateChange(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time))
                    }, parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()).show()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Change", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Period label
        Text(
            if (todayPeriods.isEmpty()) "Pick a period:" else "Period",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        // Period chips - equal weight grid
        if (todayPeriods.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..6).forEach { pNum ->
                    FilterChip(
                        selected = selectedPeriodIndex == (pNum - 1),
                        onClick = { onPeriodSelect(pNum - 1, pNum, "Period $pNum") },
                        label = { Text("P$pNum", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                todayPeriods.forEachIndexed { idx, p ->
                    FilterChip(
                        selected = idx == selectedPeriodIndex,
                        onClick = { onPeriodSelect(idx, p.period, p.subjectName) },
                        label = { Text("P${p.period}", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Subject name
        OutlinedTextField(
            value = subjectName,
            onValueChange = onSubjectChange,
            label = { Text("Subject Name", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Text("Start Roll Call", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Step2RollCall(
    students: List<com.example.crattendance.data.database.StudentEntity>,
    attendanceMap: MutableMap<String, AttendanceStatus>,
    presentCount: Int,
    absentCount: Int,
    otherCount: Int,
    totalStudents: Int,
    selectedDateStr: String,
    selectedPeriodNum: Int,
    selectedSubjectName: String,
    onMarkAllPresent: () -> Unit,
    onSave: () -> Unit,
    onShareText: () -> Unit,
    onSharePdf: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Compact inline count bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InlineCount(presentCount, "P", com.example.crattendance.theme.StatusPresent)
            Spacer(modifier = Modifier.width(10.dp))
            InlineCount(absentCount, "A", com.example.crattendance.theme.StatusAbsent)
            Spacer(modifier = Modifier.width(10.dp))
            InlineCount(otherCount, "O", MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onMarkAllPresent, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                Text("All Present", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Student list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(students) { student ->
                val status = attendanceMap[student.rrn] ?: AttendanceStatus.PRESENT
                val isPresent = status == AttendanceStatus.PRESENT

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!isPresent) Modifier.background(status.color.copy(alpha = 0.05f))
                            else Modifier
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPresent,
                        onCheckedChange = { checked ->
                            attendanceMap[student.rrn] = if (checked) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
                        },
                        modifier = Modifier.size(28.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = com.example.crattendance.theme.StatusPresent,
                            uncheckedColor = com.example.crattendance.theme.StatusAbsent
                        )
                    )

                    Column(modifier = Modifier.weight(1f).padding(start = 2.dp)) {
                        Text(
                            student.name,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    if (!isPresent) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = status.color.copy(alpha = 0.1f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    attendanceMap[student.rrn] = when (status) {
                                        AttendanceStatus.ABSENT -> AttendanceStatus.MEDICAL_LEAVE
                                        AttendanceStatus.MEDICAL_LEAVE -> AttendanceStatus.ON_DUTY
                                        AttendanceStatus.ON_DUTY -> AttendanceStatus.LATE
                                        AttendanceStatus.LATE -> AttendanceStatus.ABSENT
                                        else -> AttendanceStatus.PRESENT
                                    }
                                }
                        ) {
                            Text(
                                status.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = status.color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 36.dp, end = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }
        }

        // Compact bottom actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onShareText,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Text", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = onSharePdf,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PDF", style = MaterialTheme.typography.labelSmall)
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InlineCount(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$count",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
