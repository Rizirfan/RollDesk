package com.example.crattendance.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.crattendance.data.database.AttendanceRecordEntity
import com.example.crattendance.data.database.ElectiveAttendanceRecordEntity
import com.example.crattendance.data.database.StudentEntity
import com.example.crattendance.theme.Elective
import com.example.crattendance.theme.OnSurfaceSecondary
import com.example.crattendance.theme.StatusAbsent
import com.example.crattendance.theme.StatusLate
import com.example.crattendance.theme.StatusMedicalLeave
import com.example.crattendance.theme.StatusOnDuty
import com.example.crattendance.theme.StatusPresent
import com.example.crattendance.theme.Teal
import com.example.crattendance.ui.main.CRAttendanceViewModel
import com.example.crattendance.widget.QuickRollWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

enum class RecordsScope { All, Regular, Elective }

enum class DeleteAllScope { RegularOnly, ElectiveOnly, Both }

data class AttendanceSession(
    val key: String,
    val isElective: Boolean,
    val date: String,
    val period: Int?,
    val subject: String,
    val electiveName: String?,
    val regularRecords: List<AttendanceRecordEntity>,
    val electiveRecords: List<ElectiveAttendanceRecordEntity>,
    val count: Int
)

private fun displayDate(iso: String): String {
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    return try {
        displayFormat.format(isoFormat.parse(iso))
    } catch (e: ParseException) {
        iso
    }
}

sealed class ListItem {
    data class DateHeader(val date: String) : ListItem()
    data class SessionItem(val session: AttendanceSession) : ListItem()
}

// Both record types share status/studentRrn/date/id, so these helpers let us
// render either kind from a single List<Any>.
private fun recordStudentRrn(r: Any): String = when (r) {
    is AttendanceRecordEntity -> r.studentRrn
    is ElectiveAttendanceRecordEntity -> r.studentRrn
    else -> ""
}
private fun recordStatus(r: Any): String = when (r) {
    is AttendanceRecordEntity -> r.status
    is ElectiveAttendanceRecordEntity -> r.status
    else -> ""
}

private fun statusColorFor(status: String): Color = when (status) {
    "Present" -> StatusPresent
    "Absent" -> StatusAbsent
    "Medical Leave" -> StatusMedicalLeave
    "On Duty" -> StatusOnDuty
    "Late" -> StatusLate
    else -> OnSurfaceSecondary
}

@Composable
fun RecordsScreen(
    viewModel: CRAttendanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val records by viewModel.attendanceRecords.collectAsState()
    val electiveRecords by viewModel.electiveAttendanceRecords.collectAsState()
    val context = LocalContext.current

    val studentMap = remember(students) { students.associateBy { it.rrn } }

    var scope by remember { mutableStateOf(RecordsScope.All) }
    var query by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) searchFocusRequester.requestFocus()
    }
    // Immutable Set inside mutableStateOf: every toggle creates a new instance,
    // which is what actually triggers recomposition.
    var expandedSessions by remember { mutableStateOf<Set<String>>(emptySet()) }

    var showRecordDeleteDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<Any?>(null) }
    var recordIsElective by remember { mutableStateOf(false) }

    var showSessionDeleteDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<AttendanceSession?>(null) }

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var deleteAllScope by remember { mutableStateOf(DeleteAllScope.Both) }

    fun studentNameFor(rrn: String): String = studentMap[rrn]?.name ?: rrn

    fun regularMatches(r: AttendanceRecordEntity, q: String): Boolean =
        r.studentRrn.lowercase(Locale.getDefault()).contains(q) ||
        r.subject.lowercase(Locale.getDefault()).contains(q) ||
        r.date.lowercase(Locale.getDefault()).contains(q) ||
        studentNameFor(r.studentRrn).lowercase(Locale.getDefault()).contains(q)

    fun electiveMatches(r: ElectiveAttendanceRecordEntity, q: String): Boolean =
        r.studentRrn.lowercase(Locale.getDefault()).contains(q) ||
        r.subject.lowercase(Locale.getDefault()).contains(q) ||
        r.electiveName.lowercase(Locale.getDefault()).contains(q) ||
        r.date.lowercase(Locale.getDefault()).contains(q) ||
        studentNameFor(r.studentRrn).lowercase(Locale.getDefault()).contains(q)

    val filteredRegular = remember(records, scope, query, studentMap) {
        records.filter { r ->
            val typeOk = scope != RecordsScope.Elective
            val q = query.lowercase(Locale.getDefault())
            typeOk && (q.isEmpty() || regularMatches(r, q))
        }
    }

    val filteredElective = remember(electiveRecords, scope, query, studentMap) {
        electiveRecords.filter { r ->
            val typeOk = scope != RecordsScope.Regular
            val q = query.lowercase(Locale.getDefault())
            typeOk && (q.isEmpty() || electiveMatches(r, q))
        }
    }

    val regularByDate = remember(filteredRegular) {
        filteredRegular.groupBy { it.date }
    }

    val electiveByDate = remember(filteredElective) {
        filteredElective.groupBy { it.date }
    }

    val allDates = remember(regularByDate, electiveByDate) {
        (regularByDate.keys + electiveByDate.keys).distinct().sortedDescending()
    }

    fun buildSessions(date: String): List<AttendanceSession> {
        val sessions = mutableListOf<AttendanceSession>()

        val regRecords = regularByDate[date] ?: emptyList()
        regRecords.groupBy { "${it.period}_${it.subject}" }.forEach { (_, recs) ->
            val first = recs.first()
            sessions.add(
                AttendanceSession(
                    key = "reg|${date}|${first.period}|${first.subject}",
                    isElective = false,
                    date = date,
                    period = first.period,
                    subject = first.subject,
                    electiveName = null,
                    regularRecords = recs.sortedBy { it.studentRrn },
                    electiveRecords = emptyList(),
                    count = recs.size
                )
            )
        }

        val elRecords = electiveByDate[date] ?: emptyList()
        elRecords.groupBy { "${it.electiveName}_${it.subject}" }.forEach { (_, recs) ->
            val first = recs.first()
            sessions.add(
                AttendanceSession(
                    key = "elec|${date}|${first.electiveName}|${first.subject}",
                    isElective = true,
                    date = date,
                    period = null,
                    subject = first.subject,
                    electiveName = first.electiveName,
                    regularRecords = emptyList(),
                    electiveRecords = recs.sortedBy { it.studentRrn },
                    count = recs.size
                )
            )
        }

        return sessions.sortedWith(
            compareBy<AttendanceSession> { it.isElective }
                .thenByDescending { it.period ?: 0 }
                .thenBy { it.electiveName.orEmpty() }
        )
    }

    val totalRegular = records.size
    val totalElective = electiveRecords.size

    val visibleRegular = filteredRegular.size
    val visibleElective = filteredElective.size

    val totalSessions = remember(allDates, regularByDate, electiveByDate) {
        allDates.sumOf { date ->
            val reg = regularByDate[date]?.let { it.groupBy { "${it.period}_${it.subject}" }.size } ?: 0
            val el = electiveByDate[date]?.let { it.groupBy { "${it.electiveName}_${it.subject}" }.size } ?: 0
            reg + el
        }
    }

    val isSearching = query.isNotEmpty()
    val isFiltered = scope != RecordsScope.All
    val emptyMessage = when {
        totalRegular == 0 && totalElective == 0 -> "No attendance records found"
        isSearching || isFiltered -> "No matching records found"
        else -> "No attendance records found"
    }

    val listItems = remember(allDates, regularByDate, electiveByDate) {
        val items = mutableListOf<ListItem>()
        allDates.forEach { date ->
            items.add(ListItem.DateHeader(date))
            buildSessions(date).forEach { items.add(ListItem.SessionItem(it)) }
        }
        items.toList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header — matches Students / Roll Call screens
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Attendance Records",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            IconButton(onClick = {
                if (isSearchActive) {
                    isSearchActive = false
                    query = ""
                    focusManager.clearFocus()
                } else {
                    isSearchActive = true
                }
            }) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "Close search" else "Search records"
                )
            }
            IconButton(onClick = { showDeleteAllDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete all records",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Search, scope filters and compact counts
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (isSearchActive) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search student, RRN or subject") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScopeChip(
                        label = "All",
                        selected = scope == RecordsScope.All,
                        onClick = { scope = RecordsScope.All }
                    )
                    ScopeChip(
                        label = "Regular",
                        selected = scope == RecordsScope.Regular,
                        onClick = { scope = RecordsScope.Regular }
                    )
                    ScopeChip(
                        label = "Elective",
                        selected = scope == RecordsScope.Elective,
                        onClick = { scope = RecordsScope.Elective }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                when (scope) {
                    RecordsScope.All -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InlineCount(
                                visibleRegular + visibleElective,
                                "total",
                                MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            InlineCount(totalSessions, "sessions", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    RecordsScope.Regular -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InlineCount(visibleRegular, "regular", Teal)
                            Spacer(modifier = Modifier.width(8.dp))
                            InlineCount(totalSessions, "sessions", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    RecordsScope.Elective -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InlineCount(visibleElective, "elective", Elective)
                            Spacer(modifier = Modifier.width(8.dp))
                            InlineCount(totalSessions, "sessions", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (listItems.isEmpty()) {
                item(key = "empty") {
                    EmptyState(message = emptyMessage)
                }
            } else {
                items(
                    items = listItems,
                    key = { item ->
                        when (item) {
                            is ListItem.DateHeader -> "date|${item.date}"
                            is ListItem.SessionItem -> item.session.key
                        }
                    }
                ) { item ->
                    when (item) {
                        is ListItem.DateHeader -> {
                            DateHeader(
                                date = item.date,
                                regularCount = regularByDate[item.date]?.size ?: 0,
                                electiveCount = electiveByDate[item.date]?.size ?: 0
                            )
                        }
                        is ListItem.SessionItem -> {
                            SessionRow(
                                session = item.session,
                                studentMap = studentMap,
                                isExpanded = item.session.key in expandedSessions,
                                onToggle = {
                                    val key = item.session.key
                                    expandedSessions = if (key in expandedSessions) {
                                        expandedSessions - key
                                    } else {
                                        expandedSessions + key
                                    }
                                },
                                onDeleteRecord = { record, isElective ->
                                    recordToDelete = record
                                    recordIsElective = isElective
                                    showRecordDeleteDialog = true
                                },
                                onToggleStatus = { record, isElective, isPresent ->
                                    val newStatus = if (isPresent) "Present" else "Absent"
                                    if (isElective) {
                                        viewModel.updateElectiveAttendanceRecord(
                                            (record as ElectiveAttendanceRecordEntity).copy(status = newStatus)
                                        )
                                    } else {
                                        viewModel.updateAttendanceRecord(
                                            (record as AttendanceRecordEntity).copy(status = newStatus)
                                        )
                                    }
                                    viewModel.viewModelScope.launch {
                                        delay(300)
                                        QuickRollWidget.updateAllWidgets(context)
                                    }
                                },
                                onDeleteSession = {
                                    sessionToDelete = item.session
                                    showSessionDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Individual record deletion dialog
    if (showRecordDeleteDialog && recordToDelete != null) {
        val record = recordToDelete
        val isElective = recordIsElective
        val contextForDialog = context

        val name = if (isElective) {
            studentNameFor((record as ElectiveAttendanceRecordEntity).studentRrn)
        } else {
            studentNameFor((record as AttendanceRecordEntity).studentRrn)
        }

        val confirm = {
            if (isElective) {
                viewModel.deleteElectiveAttendanceRecord((record as ElectiveAttendanceRecordEntity).id)
            } else {
                viewModel.deleteAttendanceRecord((record as AttendanceRecordEntity).id)
            }
            viewModel.viewModelScope.launch {
                delay(300)
                QuickRollWidget.updateAllWidgets(contextForDialog)
            }
            showRecordDeleteDialog = false
            recordToDelete = null
            Toast.makeText(contextForDialog, "Record deleted", Toast.LENGTH_SHORT).show()
        }

        val messageText = if (isElective) {
            val electiveRecord = record as ElectiveAttendanceRecordEntity
            "Delete attendance for $name on ${displayDate(electiveRecord.date)}, ${electiveRecord.electiveName} (${electiveRecord.subject})? This cannot be undone."
        } else {
            val regularRecord = record as AttendanceRecordEntity
            "Delete attendance for $name on ${displayDate(regularRecord.date)}, P${regularRecord.period} (${regularRecord.subject})? This cannot be undone."
        }

        AlertDialog(
            onDismissRequest = { showRecordDeleteDialog = false; recordToDelete = null },
            title = { Text("Delete Attendance Record?", fontWeight = FontWeight.Bold) },
            text = { Text(messageText) },
            confirmButton = {
                Button(
                    onClick = confirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showRecordDeleteDialog = false; recordToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Session deletion dialog
    if (showSessionDeleteDialog && sessionToDelete != null) {
        val session = sessionToDelete!!
        val contextForDialog = context
        val confirm = {
            if (session.isElective) {
                viewModel.deleteElectiveSession(session.date, session.electiveName!!, session.subject)
            } else {
                viewModel.deletePeriodAttendance(session.date, session.period!!)
            }
            viewModel.viewModelScope.launch {
                delay(300)
                QuickRollWidget.updateAllWidgets(contextForDialog)
            }
            showSessionDeleteDialog = false
            sessionToDelete = null
            Toast.makeText(contextForDialog, "Session deleted", Toast.LENGTH_SHORT).show()
        }

        val messageText = if (session.isElective) {
            "Delete attendance for ${session.electiveName} (${session.subject}) on ${displayDate(session.date)}? This cannot be undone."
        } else {
            "Delete attendance for ${session.subject} on ${displayDate(session.date)}, P${session.period}? This cannot be undone."
        }

        AlertDialog(
            onDismissRequest = { showSessionDeleteDialog = false; sessionToDelete = null },
            title = { Text("Delete Session?", fontWeight = FontWeight.Bold) },
            text = { Text(messageText) },
            confirmButton = {
                Button(
                    onClick = confirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showSessionDeleteDialog = false; sessionToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Delete all dialog
    if (showDeleteAllDialog) {
        val contextForDialog = context
        val regularCount = totalRegular
        val electiveCount = totalElective
        val selectedScopeCount = when (deleteAllScope) {
            DeleteAllScope.RegularOnly -> regularCount
            DeleteAllScope.ElectiveOnly -> electiveCount
            DeleteAllScope.Both -> regularCount + electiveCount
        }
        val canConfirm = selectedScopeCount > 0

        val confirm = {
            viewModel.viewModelScope.launch {
                when (deleteAllScope) {
                    DeleteAllScope.RegularOnly -> viewModel.deleteAllRegularAttendance()
                    DeleteAllScope.ElectiveOnly -> viewModel.deleteAllElectiveAttendance()
                    DeleteAllScope.Both -> viewModel.deleteAllAttendanceAndElective()
                }
                delay(300)
                QuickRollWidget.updateAllWidgets(contextForDialog)
            }
            showDeleteAllDialog = false
            Toast.makeText(contextForDialog, "Attendance records deleted", Toast.LENGTH_SHORT).show()
        }

        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete All Records?", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.width(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Regular records: $regularCount\nElective records: $electiveCount",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Scope:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        DeleteAllScopeRow(
                            label = "Regular only ($regularCount)",
                            selected = deleteAllScope == DeleteAllScope.RegularOnly,
                            enabled = regularCount > 0,
                            onClick = { deleteAllScope = DeleteAllScope.RegularOnly }
                        )
                        DeleteAllScopeRow(
                            label = "Elective only ($electiveCount)",
                            selected = deleteAllScope == DeleteAllScope.ElectiveOnly,
                            enabled = electiveCount > 0,
                            onClick = { deleteAllScope = DeleteAllScope.ElectiveOnly }
                        )
                        DeleteAllScopeRow(
                            label = "Both (${regularCount + electiveCount})",
                            selected = deleteAllScope == DeleteAllScope.Both,
                            enabled = regularCount + electiveCount > 0,
                            onClick = { deleteAllScope = DeleteAllScope.Both }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This deletes attendance history only. Students, timetable, college profile, and elective enrollments will be kept.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = confirm,
                    enabled = canConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete All") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DeleteAllScopeRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
private fun ScopeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(6.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Teal,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun InlineCount(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun DateHeader(date: String, regularCount: Int, electiveCount: Int) {
    val total = regularCount + electiveCount
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = displayDate(date),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (total == 1) "1 record" else "$total records",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
            thickness = 1.dp
        )
    }
}

@Composable
private fun SessionRow(
    session: AttendanceSession,
    studentMap: Map<String, StudentEntity>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDeleteRecord: (Any, Boolean) -> Unit,
    onToggleStatus: (Any, Boolean, Boolean) -> Unit,
    onDeleteSession: () -> Unit
) {
    val sessionRecords: List<Any> = if (session.isElective) {
        session.electiveRecords.map { it }
    } else {
        session.regularRecords.map { it }
    }

    val presentCount = sessionRecords.count { recordStatus(it) == "Present" }
    val absentCount = sessionRecords.count { recordStatus(it) == "Absent" }
    val otherCount = sessionRecords.count {
        recordStatus(it) != "Present" && recordStatus(it) != "Absent"
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "sessionChevronRotation"
    )

    val context = LocalContext.current

    val copySessionAsText: () -> Unit = {
        val sb = StringBuilder()
        sb.appendLine(session.subject)
        if (session.isElective) {
            sb.appendLine("${session.electiveName} \u00B7 ${displayDate(session.date)}")
        } else {
            sb.appendLine("${displayDate(session.date)} \u00B7 Period ${session.period}")
        }
        sb.appendLine()

        val present = sessionRecords.filter { recordStatus(it) == "Present" }
        val absent = sessionRecords.filter { recordStatus(it) == "Absent" }
        val others = sessionRecords.filter {
            recordStatus(it) != "Present" && recordStatus(it) != "Absent"
        }

        fun studentLine(record: Any, withStatus: Boolean) {
            val rrn = recordStudentRrn(record)
            val name = studentMap[rrn]?.name ?: rrn
            sb.append("  $name ($rrn)")
            if (withStatus) sb.append(" - ${recordStatus(record)}")
            sb.appendLine()
        }

        if (present.isNotEmpty()) {
            sb.appendLine("Present (${present.size}):")
            present.forEach { studentLine(it, false) }
            sb.appendLine()
        }
        if (absent.isNotEmpty()) {
            sb.appendLine("Absent (${absent.size}):")
            absent.forEach { studentLine(it, false) }
            sb.appendLine()
        }
        if (others.isNotEmpty()) {
            sb.appendLine("Other (${others.size}):")
            others.forEach { studentLine(it, true) }
        }

        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("Attendance Report", sb.toString().trimEnd())
        )
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (session.isElective) {
                            session.electiveName.orEmpty()
                        } else {
                            "Period ${session.period}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InlineCount(presentCount, "P", StatusPresent)
                        Spacer(modifier = Modifier.width(10.dp))
                        InlineCount(absentCount, "A", StatusAbsent)
                        if (otherCount > 0) {
                            Spacer(modifier = Modifier.width(10.dp))
                            InlineCount(otherCount, "O", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isExpanded) "Hide" else "Show",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse session" else "Expand session",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(chevronRotation)
                        )
                    }
                }
                IconButton(
                    onClick = copySessionAsText,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy as text",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDeleteSession,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete session",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    sessionRecords.forEachIndexed { index, record ->
                        val rrn = recordStudentRrn(record)
                        val name = studentMap[rrn]?.name ?: rrn
                        val status = recordStatus(record)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = status == "Present",
                                onCheckedChange = { checked ->
                                    onToggleStatus(record, session.isElective, checked)
                                },
                                modifier = Modifier.size(28.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = StatusPresent,
                                    uncheckedColor = StatusAbsent
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = rrn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusTag(text = status, color = statusColorFor(status))
                            IconButton(
                                onClick = { onDeleteRecord(record, session.isElective) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete record",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (index < sessionRecords.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 14.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
