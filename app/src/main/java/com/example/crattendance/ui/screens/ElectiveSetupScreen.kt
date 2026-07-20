package com.example.crattendance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.crattendance.theme.Elective
import com.example.crattendance.theme.ElectiveContainer
import com.example.crattendance.theme.ElectiveOnContainer
import com.example.crattendance.ui.main.CRAttendanceViewModel

@Composable
fun ElectiveSetupScreen(
    viewModel: CRAttendanceViewModel,
    onStartAttendance: (electiveName: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allStudents by viewModel.students.collectAsState()
    val electiveStudents by viewModel.electiveStudents.collectAsState()

    var selectedElectiveName by remember { mutableStateOf<String?>(null) }
    var newElectiveName by remember { mutableStateOf("") }
    var isCreatingNew by remember { mutableStateOf(false) }
    val selectedStudentRrns = remember { mutableStateSetOf<String>() }

    // Search properties for student checklist matching StudentListScreen.kt
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val activeElectiveNames = remember(electiveStudents) {
        electiveStudents.map { it.electiveName }.distinct().sorted()
    }

    LaunchedEffect(electiveStudents, selectedElectiveName) {
        selectedElectiveName?.let { name ->
            val rrns = electiveStudents.filter { it.electiveName == name }.map { it.studentRrn }
            selectedStudentRrns.clear()
            selectedStudentRrns.addAll(rrns)
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    val effectiveElectiveName = if (isCreatingNew) newElectiveName.trim() else selectedElectiveName
    val canStart = !effectiveElectiveName.isNullOrEmpty() && selectedStudentRrns.isNotEmpty()
    val showStudentList = effectiveElectiveName != null

    // Search filtered students checklist matching roster screen logic
    val filteredStudents = remember(allStudents, searchQuery) {
        allStudents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.rrn.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Header matching Class Roster & StudentListScreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isSearchActive) {
                    isSearchActive = false
                    searchQuery = ""
                    focusManager.clearFocus()
                } else {
                    onBack()
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSearchActive) "Search Students" else "Electives Setup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSearchActive) "Search by name or RRN to enroll" 
                           else if (activeElectiveNames.isNotEmpty()) "${activeElectiveNames.size} electives configured"
                           else "Create an elective to start roll call",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showStudentList && !isSearchActive) {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Inline Search Field matching Roster page
        if (isSearchActive && showStudentList) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name or RRN") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Scrollable content area for Elective profiles so they do not push student details out of viewport
        if (!isSearchActive) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (activeElectiveNames.isNotEmpty() && !isCreatingNew) {
                    activeElectiveNames.forEach { name ->
                        val isSelected = selectedElectiveName == name
                        val studentCount = electiveStudents.count { it.electiveName == name }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectiveContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    selectedElectiveName = if (selectedElectiveName == name) null else name
                                    isCreatingNew = false
                                    newElectiveName = ""
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Initials badge
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (isSelected) Elective.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(2).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Elective else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        name,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) ElectiveOnContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "$studentCount student${if (studentCount != 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ElectiveOnContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Elective,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deleteElective(name)
                                        if (selectedElectiveName == name) selectedElectiveName = null
                                        Toast.makeText(context, "\"$name\" deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // New elective input card
                if (isCreatingNew) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ElectiveContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Elective.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Elective,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "New Elective",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ElectiveOnContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        isCreatingNew = false
                                        newElectiveName = ""
                                        selectedStudentRrns.clear()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = ElectiveOnContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = newElectiveName,
                                onValueChange = { newElectiveName = it },
                                placeholder = { Text("e.g. Data Science, AI & ML") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Elective,
                                    unfocusedBorderColor = Elective.copy(alpha = 0.3f),
                                    cursorColor = Elective
                                )
                            )
                        }
                    }
                }

                // Add new elective button
                if (!isCreatingNew) {
                    OutlinedButton(
                        onClick = {
                            isCreatingNew = true
                            selectedElectiveName = null
                            selectedStudentRrns.clear()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Elective),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Elective", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        // Saved elective attendance records section
        val electiveAttendanceRecords by viewModel.electiveAttendanceRecords.collectAsState()
        var selectedSessionToView by remember { mutableStateOf<List<com.example.crattendance.data.database.ElectiveAttendanceRecordEntity>?>(null) }
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }
        var sessionToDelete by remember { mutableStateOf<List<com.example.crattendance.data.database.ElectiveAttendanceRecordEntity>?>(null) }

        val electiveSessions = remember(electiveAttendanceRecords, selectedElectiveName) {
            if (selectedElectiveName == null) emptyList()
            else electiveAttendanceRecords
                .filter { it.electiveName == selectedElectiveName }
                .groupBy { "${it.date}_${it.subject}" }
                .values
                .sortedByDescending { it.first().timestamp }
        }

        // Show recent saved elective history when elective is selected and search is not active
        if (selectedElectiveName != null && !isSearchActive && electiveSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Recent Attendance History",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    electiveSessions.take(3).forEachIndexed { index, sessionList ->
                        val first = sessionList.first()
                        val present = sessionList.count { it.status == "Present" }
                        val absent = sessionList.count { it.status == "Absent" }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSessionToView = sessionList }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(first.subject, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text(first.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("$present P", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = com.example.crattendance.theme.StatusPresent)
                                Text("$absent A", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = com.example.crattendance.theme.StatusAbsent)
                            }
                        }
                        if (index < minOf(electiveSessions.size, 3) - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // Render dialog sheets for viewing/deleting saved elective sessions
        selectedSessionToView?.let { session ->
            val first = session.first()
            val absents = session.filter { it.status == "Absent" }
            val presents = session.filter { it.status == "Present" }
            val others = session.filter { it.status != "Present" && it.status != "Absent" }

            androidx.compose.ui.window.Dialog(
                onDismissRequest = { selectedSessionToView = null }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Elective Class Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                sessionToDelete = session
                                showDeleteConfirmDialog = true
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Subject: ${first.subject}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Date: ${first.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("${presents.size} Present", color = com.example.crattendance.theme.StatusPresent, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("${absents.size} Absent", color = com.example.crattendance.theme.StatusAbsent, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            if (others.isNotEmpty()) {
                                Text("${others.size} Other", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (absents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Absentees:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            LazyColumn(modifier = Modifier.heightIn(max = 120.dp).padding(top = 4.dp)) {
                                items(absents.size) { idx ->
                                    val student = allStudents.find { it.rrn == absents[idx].studentRrn }
                                    Text(
                                        "- ${absents[idx].studentRrn} (${student?.name ?: "Unknown"})",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
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
                                    sb.append("Elective: $selectedElectiveName\n")
                                    sb.append("Subject: ${first.subject}\n")
                                    sb.append("Date: ${first.date}\n\n")
                                    sb.append("*Absentees (${absents.size}):*\n")
                                    if (absents.isEmpty()) sb.append("Nil")
                                    else absents.forEach {
                                        val student = allStudents.find { s -> s.rrn == it.studentRrn }
                                        sb.append("- ${it.studentRrn} (${student?.name ?: "Unknown"})\n")
                                    }
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Elective Attendance", sb.toString())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) { Text("Copy", style = MaterialTheme.typography.labelSmall) }

                            OutlinedButton(
                                onClick = {
                                    val sb = StringBuilder()
                                    sb.append("*Elective Attendance Report*\n")
                                    sb.append("Elective: $selectedElectiveName\n")
                                    sb.append("Subject: ${first.subject}\n")
                                    sb.append("Date: ${first.date}\n\n")
                                    sb.append("*Absentees (${absents.size}):*\n")
                                    if (absents.isEmpty()) sb.append("Nil")
                                    else absents.forEach {
                                        val student = allStudents.find { s -> s.rrn == it.studentRrn }
                                        sb.append("- ${it.studentRrn} (${student?.name ?: "Unknown"})\n")
                                    }
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) { Text("Text", style = MaterialTheme.typography.labelSmall) }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val headers = arrayOf("RRN", "Student Name")
                                        val rowData = absents.map { s ->
                                            val student = allStudents.find { st -> st.rrn == s.studentRrn }
                                            arrayOf(s.studentRrn, student?.name ?: "Unknown")
                                        }
                                        val infoList = listOf(
                                            "Elective: $selectedElectiveName",
                                            "Subject: ${first.subject}",
                                            "Date: ${first.date}",
                                            "Total Students: ${session.size}",
                                            "Present: ${presents.size}",
                                            "Absent: ${absents.size}"
                                        )
                                        scope.launch {
                                            val file = com.example.crattendance.utils.ExportHelper.exportToPDF(
                                                context = context,
                                                fileName = "Elective_${selectedElectiveName}_${first.date}",
                                                title = "Elective Absentees Report",
                                                infoList = infoList,
                                                headers = headers,
                                                data = rowData
                                            )
                                            if (file != null) {
                                                val fileUri = androidx.core.content.FileProvider.getUriForFile(
                                                    context, "${context.packageName}.provider", file
                                                )
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
                                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Share PDF"))
                                            } else {
                                                Toast.makeText(context, "Failed to build PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "PDF Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) { Text("PDF", style = MaterialTheme.typography.labelSmall) }

                            Button(
                                onClick = { selectedSessionToView = null },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) { Text("Close", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            }
        }

        if (showDeleteConfirmDialog && sessionToDelete != null) {
            val first = sessionToDelete!!.first()
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Attendance?") },
                text = { Text("Are you sure you want to delete elective attendance records for ${first.subject} on ${first.date}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteElectiveAttendanceForDateAndElective(first.date, first.electiveName)
                            showDeleteConfirmDialog = false
                            selectedSessionToView = null
                            sessionToDelete = null
                            Toast.makeText(context, "Deleted saved attendance", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Student selection — divider + header + list (matches StudentListScreen style exactly)
        if (showStudentList) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Student header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Enroll Students",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${selectedStudentRrns.size}/${allStudents.size} enrolled",
                    style = MaterialTheme.typography.labelSmall,
                    color = Elective,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                TextButton(
                    onClick = { allStudents.forEach { selectedStudentRrns.add(it.rrn) } },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("All", style = MaterialTheme.typography.labelSmall) }
                TextButton(
                    onClick = { selectedStudentRrns.clear() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("None", style = MaterialTheme.typography.labelSmall) }
            }

            // Student list matching StudentListScreen layout styling
            if (filteredStudents.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isEmpty()) "No students in roster database yet." else "No matching students found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(filteredStudents.size) { index ->
                        val student = filteredStudents[index]
                        val isChecked = student.rrn in selectedStudentRrns

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedStudentRrns.remove(student.rrn)
                                    else selectedStudentRrns.add(student.rrn)
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedStudentRrns.add(student.rrn)
                                    else selectedStudentRrns.remove(student.rrn)
                                },
                                modifier = Modifier.size(28.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Elective,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    student.name,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    student.rrn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (index < filteredStudents.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 36.dp, end = 4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        } else if (activeElectiveNames.isEmpty() && !isCreatingNew) {
            // Empty state centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No electives yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            if (!isSearchActive) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Bottom action
        Button(
            onClick = {
                effectiveElectiveName?.let { name ->
                    viewModel.saveElectiveStudents(name, selectedStudentRrns.toList())
                    onStartAttendance(name)
                }
            },
            enabled = canStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Elective,
                disabledContainerColor = Elective.copy(alpha = 0.38f)
            ),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Take Attendance", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
