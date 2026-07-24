package com.example.crattendance.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import com.example.crattendance.data.database.AttendanceRecordEntity
import com.example.crattendance.ui.main.CRAttendanceViewModel
import com.example.crattendance.utils.ExportHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: CRAttendanceViewModel,
    onNavigateToTakeAttendance: () -> Unit,
    onNavigateToElectiveAttendance: (electiveName: String) -> Unit,
    onNavigateToElectiveSetup: () -> Unit,
    onNavigateToStudentList: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onEditSession: (date: String, period: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val records by viewModel.attendanceRecords.collectAsState()
    val timetable by viewModel.timetable.collectAsState()
    val electiveRecords by viewModel.electiveAttendanceRecords.collectAsState()
    val electiveNames by viewModel.electiveNames.collectAsState()
    val context = LocalContext.current

    val studentMap = remember(students) {
        students.associateBy { it.rrn }
    }

    var selectedSession by remember { mutableStateOf<List<AttendanceRecordEntity>?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<List<AttendanceRecordEntity>?>(null) }

    var showRosterActionDialog by remember { mutableStateOf(false) }
    var activeExportFile by remember { mutableStateOf<java.io.File?>(null) }
    var exportTitleText by remember { mutableStateOf("") }

    var calendarSelectedDate by remember { mutableStateOf<String?>(null) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    var showElectivePicker by remember { mutableStateOf(false) }
    var editingElectiveName by remember { mutableStateOf<String?>(null) }
    var renameElectiveText by remember { mutableStateOf("") }
    var showDeleteElectiveDialog by remember { mutableStateOf(false) }
    var deletingElectiveName by remember { mutableStateOf<String?>(null) }

    val todayDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    val calendar = remember { Calendar.getInstance() }
    val dayOfWeek = remember {
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }

    val todayPeriods = remember(timetable, dayOfWeek) {
        timetable.filter { it.dayOfWeek == dayOfWeek }.sortedBy { it.period }
    }

    val totalStudents = students.size
    val totalRecords = records.size

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val recentSessions = remember(records) {
        records
            .groupBy { "${it.date}_${it.period}" }
            .values
            .sortedByDescending { it.first().timestamp }
            .take(5)
    }

    val recentElectiveSessions = remember(electiveRecords) {
        electiveRecords
            .groupBy { "${it.date}_${it.electiveName}_${it.subject}" }
            .values
            .sortedByDescending { it.first().timestamp }
            .take(5)
    }

    var selectedElectiveSession by remember { mutableStateOf<List<com.example.crattendance.data.database.ElectiveAttendanceRecordEntity>?>(null) }
    var showElectiveDeleteDialog by remember { mutableStateOf(false) }
    var electiveSessionToDelete by remember { mutableStateOf<List<com.example.crattendance.data.database.ElectiveAttendanceRecordEntity>?>(null) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = todayDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(onClick = {
                        val parts = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).split("-")
                        android.app.DatePickerDialog(context, { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            val picked = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            calendarSelectedDate = picked
                            showCalendarDialog = true
                        }, parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()).show()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item(key = "timetable_banner") {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Timetable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = if (todayPeriods.isEmpty()) "No periods scheduled" else "${todayPeriods.size} periods",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }

                        if (todayPeriods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                todayPeriods.forEach { period ->
                                    Text(
                                        text = "P${period.period}: ${period.subjectName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToTakeAttendance,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Attendance", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item(key = "elective_banner") {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showElectivePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(com.example.crattendance.theme.Elective.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
            
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = com.example.crattendance.theme.Elective,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Elective Attendance",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Track elective subject roll calls",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item(key = "quick_stats") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToStudentList() }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "$totalStudents",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Students",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTimetable() }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "${todayPeriods.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Periods Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item(key = "overview") {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Overview",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$totalStudents", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Students", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$totalRecords", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Records", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (recentSessions.isNotEmpty()) {
                item(key = "recent_header") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Recent",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            recentSessions.forEachIndexed { index, sessionRecords ->
                                val firstRecord = sessionRecords.first()
                                val presentCount = sessionRecords.count { it.status == "Present" }
                                val absentCount = sessionRecords.count { it.status == "Absent" }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { selectedSession = sessionRecords }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = firstRecord.subject,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${firstRecord.date}  \u00B7  P${firstRecord.period}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "$presentCount P",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = com.example.crattendance.theme.StatusPresent
                                        )
                                        Text(
                                            text = "$absentCount A",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = com.example.crattendance.theme.StatusAbsent
                                        )
                                    }
                                }

                                if (index < recentSessions.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }

            if (recentElectiveSessions.isNotEmpty()) {
                item(key = "recent_elective_header") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Recent Electives",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            recentElectiveSessions.forEachIndexed { index, sessionRecords ->
                                val firstRecord = sessionRecords.first()
                                val presentCount = sessionRecords.count { it.status == "Present" }
                                val absentCount = sessionRecords.count { it.status == "Absent" }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { selectedElectiveSession = sessionRecords }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${firstRecord.electiveName} (${firstRecord.subject})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = firstRecord.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "$presentCount P",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = com.example.crattendance.theme.StatusPresent
                                        )
                                        Text(
                                            text = "$absentCount A",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = com.example.crattendance.theme.StatusAbsent
                                        )
                                    }
                                }

                                if (index < recentElectiveSessions.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Elective Session Detail Popup
    selectedElectiveSession?.let { session ->
        val first = session.first()
        val absents = session.filter { it.status == "Absent" }
        val presents = session.filter { it.status == "Present" }

        Dialog(
            onDismissRequest = { selectedElectiveSession = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { selectedElectiveSession = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {}
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = first.electiveName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                electiveSessionToDelete = session
                                showElectiveDeleteDialog = true
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Subject: ${first.subject}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(text = first.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "${presents.size} Present", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = com.example.crattendance.theme.StatusPresent)
                            Text(text = "${absents.size} Absent", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = com.example.crattendance.theme.StatusAbsent)
                        }

                        if (absents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Absentees:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            absents.forEach { record ->
                                val studentName = studentMap[record.studentRrn]?.name ?: "Unknown"
                                Text(
                                    text = "- ${record.studentRrn} ($studentName)",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sb = StringBuilder()
                                    sb.append("*Elective Attendance Report*\n")
                                    sb.append("Elective: ${first.electiveName}\n")
                                    sb.append("Subject: ${first.subject}\n")
                                    sb.append("Date: ${first.date}\n\n")
                                    sb.append("*Absentees (${absents.size}):*\n")
                                    if (absents.isEmpty()) sb.append("Nil")
                                    else absents.forEach {
                                        val studentName = studentMap[it.studentRrn]?.name ?: "Unknown"
                                        sb.append("- ${it.studentRrn} ($studentName)\n")
                                    }

                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Elective Attendance", sb.toString())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    val sb = StringBuilder()
                                    sb.append("*Elective Attendance Report*\n")
                                    sb.append("Elective: ${first.electiveName}\n")
                                    sb.append("Subject: ${first.subject}\n")
                                    sb.append("Date: ${first.date}\n\n")
                                    sb.append("*Absentees (${absents.size}):*\n")
                                    if (absents.isEmpty()) sb.append("Nil")
                                    else absents.forEach {
                                        val studentName = studentMap[it.studentRrn]?.name ?: "Unknown"
                                        sb.append("- ${it.studentRrn} ($studentName)\n")
                                    }

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Report"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val headers = arrayOf("RRN", "Student Name")
                                        val rowData = absents.map { s ->
                                            val studentName = studentMap[s.studentRrn]?.name ?: "Unknown"
                                            arrayOf(s.studentRrn, studentName)
                                        }
                                        val infoList = listOf(
                                            "Elective: ${first.electiveName}",
                                            "Subject: ${first.subject}",
                                            "Date: ${first.date}",
                                            "Present: ${presents.size}",
                                            "Absent: ${absents.size}"
                                        )
                                        viewModel.viewModelScope.launch {
                                            val file = ExportHelper.exportToPDF(
                                                context = context,
                                                fileName = "Elective_${first.electiveName}_${first.date}",
                                                title = "Elective Absentees Report",
                                                infoList = infoList,
                                                headers = headers,
                                                data = rowData
                                            )
                                            if (file != null) {
                                                activeExportFile = file
                                                exportTitleText = "Elective ${first.electiveName}"
                                                showRosterActionDialog = true
                                            } else {
                                                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "PDF Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { selectedElectiveSession = null },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) { Text("Close", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            }
        }
    }

    // Elective Delete Dialog
    if (showElectiveDeleteDialog && electiveSessionToDelete != null) {
        val first = electiveSessionToDelete!!.first()
        AlertDialog(
            onDismissRequest = { showElectiveDeleteDialog = false; electiveSessionToDelete = null },
            title = { Text("Delete Attendance?") },
            text = { Text("Delete elective records for ${first.electiveName} (${first.subject}) on ${first.date}? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteElectiveAttendanceForDateAndElective(first.date, first.electiveName)
                        showElectiveDeleteDialog = false
                        selectedElectiveSession = null
                        electiveSessionToDelete = null
                        Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showElectiveDeleteDialog = false; electiveSessionToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Session Detail Popup
    selectedSession?.let { session ->
        val first = session.first()
        val presentStudents = session.filter { it.status == "Present" }
        val absentStudents = session.filter { it.status == "Absent" }
        val otherStudents = session.filter { it.status != "Present" && it.status != "Absent" }

        Dialog(
            onDismissRequest = { selectedSession = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { selectedSession = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { /* absorb clicks */ }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(first.subject, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${first.date}  \u00B7  P${first.period}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { selectedSession = null }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                            if (presentStudents.isNotEmpty()) {
                                Text("Present (${presentStudents.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = com.example.crattendance.theme.StatusPresent)
                                Spacer(modifier = Modifier.height(2.dp))
                                presentStudents.forEach { r ->
                                    val studName = studentMap[r.studentRrn]?.name ?: r.studentRrn
                                    Text(
                                        "$studName (${r.studentRrn})",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 1.dp, bottom = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (absentStudents.isNotEmpty()) {
                                Text("Absent (${absentStudents.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = com.example.crattendance.theme.StatusAbsent)
                                Spacer(modifier = Modifier.height(2.dp))
                                absentStudents.forEach { r ->
                                    val studName = studentMap[r.studentRrn]?.name ?: r.studentRrn
                                    Text(
                                        "$studName (${r.studentRrn})",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 1.dp, bottom = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (otherStudents.isNotEmpty()) {
                                Text("Other (${otherStudents.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                otherStudents.forEach { r ->
                                    val studName = studentMap[r.studentRrn]?.name ?: r.studentRrn
                                    Text(
                                        "$studName (${r.studentRrn}) - ${r.status}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 1.dp, bottom = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sb = StringBuilder()
                                    sb.appendLine(first.subject)
                                    sb.appendLine("${first.date}  \u00B7  P${first.period}")
                                    sb.appendLine()
                                    if (presentStudents.isNotEmpty()) {
                                        sb.appendLine("Present (${presentStudents.size}):")
                                        presentStudents.forEach { r ->
                                            val name = studentMap[r.studentRrn]?.name ?: ""
                                            sb.appendLine("  $name (${r.studentRrn})")
                                        }
                                        sb.appendLine()
                                    }
                                    if (absentStudents.isNotEmpty()) {
                                        sb.appendLine("Absent (${absentStudents.size}):")
                                        absentStudents.forEach { r ->
                                            val name = studentMap[r.studentRrn]?.name ?: ""
                                            sb.appendLine("  $name (${r.studentRrn})")
                                        }
                                        sb.appendLine()
                                    }
                                    if (otherStudents.isNotEmpty()) {
                                        sb.appendLine("Other (${otherStudents.size}):")
                                        otherStudents.forEach { r ->
                                            val name = studentMap[r.studentRrn]?.name ?: ""
                                            sb.appendLine("  $name (${r.studentRrn}) - ${r.status}")
                                        }
                                    }
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Attendance Report", sb.toString())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    selectedSession = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    sessionToDelete = session
                                    showDeleteDialog = true
                                    selectedSession = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    onEditSession(first.date, first.period)
                                    selectedSession = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    val absents = session.filter { it.status == "Absent" }
                                    val headers = arrayOf("RRN", "Student Name")
                                    val rowData = absents.map { r ->
                                        val name = studentMap[r.studentRrn]?.name ?: ""
                                        arrayOf(r.studentRrn, name)
                                    }
                                    val infoList = listOf(
                                        "Date: ${first.date}",
                                        "Period: P${first.period}",
                                        "Subject: ${first.subject}",
                                        "Present: ${presentStudents.size}",
                                        "Absent: ${absentStudents.size}"
                                    )
                                    viewModel.viewModelScope.launch {
                                        val file = ExportHelper.exportToPDF(
                                            context = context,
                                            fileName = "Attendance_${first.date}_P${first.period}",
                                            title = "Absentees Report",
                                            infoList = infoList,
                                            headers = headers,
                                            data = rowData
                                        )
                                        if (file != null) {
                                            activeExportFile = file
                                            exportTitleText = "Attendance P${first.period}"
                                            showRosterActionDialog = true
                                        } else {
                                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    selectedSession = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && sessionToDelete != null) {
        val first = sessionToDelete!!.first()
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; sessionToDelete = null },
            title = { Text("Delete Record?") },
            text = { Text("Delete attendance for ${first.subject} on ${first.date}, P${first.period}? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePeriodAttendance(first.date, first.period)
                        showDeleteDialog = false
                        sessionToDelete = null
                        Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; sessionToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Elective picker dialog
    if (showElectivePicker) {
        AlertDialog(
            onDismissRequest = { showElectivePicker = false },
            title = { Text("Select Elective") },
            text = {
                if (electiveNames.isEmpty()) {
                    Text("No electives configured yet.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        electiveNames.forEach { name ->
                            val recordCount = electiveRecords.count { it.electiveName == name }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showElectivePicker = false
                                        onNavigateToElectiveAttendance(name)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (recordCount > 0) {
                                            Text(
                                                text = "$recordCount records",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        editingElectiveName = name
                                        renameElectiveText = name
                                        showElectivePicker = false
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = {
                                        deletingElectiveName = name
                                        showDeleteElectiveDialog = true
                                        showElectivePicker = false
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { showElectivePicker = false; onNavigateToElectiveSetup() }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                    TextButton(onClick = { showElectivePicker = false }) { Text("Cancel") }
                }
            }
        )
    }

    // Rename elective dialog
    if (editingElectiveName != null) {
        AlertDialog(
            onDismissRequest = { editingElectiveName = null },
            title = { Text("Rename Elective") },
            text = {
                OutlinedTextField(
                    value = renameElectiveText,
                    onValueChange = { renameElectiveText = it },
                    label = { Text("Elective Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameElectiveText.trim()
                        if (newName.isNotEmpty() && newName != editingElectiveName) {
                            viewModel.renameElective(editingElectiveName!!, newName)
                            Toast.makeText(context, "Renamed to $newName", Toast.LENGTH_SHORT).show()
                        }
                        editingElectiveName = null
                    }
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { editingElectiveName = null }) { Text("Cancel") }
            }
        )
    }

    // Delete elective confirmation dialog
    if (showDeleteElectiveDialog && deletingElectiveName != null) {
        AlertDialog(
            onDismissRequest = { showDeleteElectiveDialog = false; deletingElectiveName = null },
            title = { Text("Delete Elective?") },
            text = { Text("Delete \"$deletingElectiveName\" and all its attendance records? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteElective(deletingElectiveName!!)
                        showDeleteElectiveDialog = false
                        deletingElectiveName = null
                        Toast.makeText(context, "Elective deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteElectiveDialog = false; deletingElectiveName = null }) { Text("Cancel") }
            }
        )
    }

    // Calendar date attendance dialog
    if (showCalendarDialog && calendarSelectedDate != null) {
        val dateRecords = records.filter { it.date == calendarSelectedDate }
        val groupedByPeriod = dateRecords.groupBy { it.period }.toSortedMap()

        Dialog(
            onDismissRequest = { showCalendarDialog = false; calendarSelectedDate = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { showCalendarDialog = false; calendarSelectedDate = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {}
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = calendarSelectedDate!!,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showCalendarDialog = false; calendarSelectedDate = null }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${dateRecords.size} records across ${groupedByPeriod.size} periods",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (dateRecords.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No attendance records for this date",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                                groupedByPeriod.forEach { (period, periodRecords) ->
                                    val first = periodRecords.first()
                                    val presentCount = periodRecords.count { it.status == "Present" }
                                    val absentCount = periodRecords.count { it.status == "Absent" }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                selectedSession = periodRecords
                                                showCalendarDialog = false
                                                calendarSelectedDate = null
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = first.subject,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "P$period",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "$presentCount P",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = com.example.crattendance.theme.StatusPresent
                                            )
                                            Text(
                                                text = "$absentCount A",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = com.example.crattendance.theme.StatusAbsent
                                            )
                                        }
                                    }

                                    if (period != groupedByPeriod.keys.last()) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
