package com.example.crattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.crattendance.ui.main.CRAttendanceViewModel
import kotlinx.coroutines.delay

@Composable
fun StudentListScreen(
    viewModel: CRAttendanceViewModel,
    onBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(searchQuery) {
        delay(200)
        debouncedQuery = searchQuery
    }

    val filteredStudents = remember(students, debouncedQuery) {
        if (debouncedQuery.isEmpty()) students
        else students.filter {
            it.name.contains(debouncedQuery, ignoreCase = true) ||
                it.rrn.contains(debouncedQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
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
                    text = if (isSearchActive) "Search" else "Class Roster",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSearchActive) "Search by name or RRN" else "${students.size} students",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isSearchActive) {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { showAddStudentDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Students",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (isSearchActive) {
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

        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "No students yet. Use + to add single or bulk students." else "No matching students",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    count = filteredStudents.size,
                    key = { filteredStudents[it].rrn }
                ) { index ->
                    val student = filteredStudents[index]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetails(student.rrn) }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            Text(student.rrn, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (index < filteredStudents.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        AddStudentsDialog(
            onDismiss = { showAddStudentDialog = false },
            onAddSingle = { rrn, name, phone, notes ->
                viewModel.addManualStudent(rrn = rrn, name = name, phone = phone, notes = notes)
            },
            onAddBulk = { csvData ->
                viewModel.addBulkStudents(csvData)
            }
        )
    }
}

@Composable
private fun AddStudentsDialog(
    onDismiss: () -> Unit,
    onAddSingle: (rrn: String, name: String, phone: String, notes: String) -> Unit,
    onAddBulk: (csvData: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf(0) } // 0 = single, 1 = bulk
    var rrn by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var bulkText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = com.example.crattendance.theme.Teal,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Add Students",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val activeColor = com.example.crattendance.theme.Teal
                    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    val activeTextColor = Color.White
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedMode == 0) activeColor else Color.Transparent)
                            .clickable { selectedMode = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Single Student",
                            color = if (selectedMode == 0) activeTextColor else inactiveTextColor,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedMode == 1) activeColor else Color.Transparent)
                            .clickable { selectedMode = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Bulk Import",
                            color = if (selectedMode == 1) activeTextColor else inactiveTextColor,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                if (selectedMode == 0) {
                    OutlinedTextField(
                        value = rrn,
                        onValueChange = { 
                            rrn = it
                            errorText = null
                        },
                        label = { Text("RRN") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = com.example.crattendance.theme.Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.crattendance.theme.Teal,
                            focusedLabelColor = com.example.crattendance.theme.Teal,
                            cursorColor = com.example.crattendance.theme.Teal
                        )
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            errorText = null
                        },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = com.example.crattendance.theme.Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.crattendance.theme.Teal,
                            focusedLabelColor = com.example.crattendance.theme.Teal,
                            cursorColor = com.example.crattendance.theme.Teal
                        )
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone (optional)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = com.example.crattendance.theme.Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.crattendance.theme.Teal,
                            focusedLabelColor = com.example.crattendance.theme.Teal,
                            cursorColor = com.example.crattendance.theme.Teal
                        )
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = com.example.crattendance.theme.Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.crattendance.theme.Teal,
                            focusedLabelColor = com.example.crattendance.theme.Teal,
                            cursorColor = com.example.crattendance.theme.Teal
                        )
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Paste CSV format lines below:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Format: RRN,Name,Phone,Notes (one per line)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { 
                            bulkText = it
                            errorText = null
                        },
                        placeholder = { Text("22CS001,Arya,9876543210,Class rep") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.crattendance.theme.Teal,
                            cursorColor = com.example.crattendance.theme.Teal
                        )
                    )
                }

                errorText?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMode == 0) {
                        if (rrn.trim().isEmpty() || name.trim().isEmpty()) {
                            errorText = "RRN and Name are required."
                            return@Button
                        }
                        onAddSingle(rrn, name, phone, notes)
                    } else {
                        if (bulkText.trim().isEmpty()) {
                            errorText = "Paste at least one student line."
                            return@Button
                        }
                        onAddBulk(bulkText)
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.crattendance.theme.Teal
                )
            ) {
                Text(if (selectedMode == 0) "Add Student" else "Import Bulk", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { 
                Text("Cancel") 
            }
        }
    )
}
