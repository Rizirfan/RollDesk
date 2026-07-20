package com.example.crattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.crattendance.ui.main.CRAttendanceViewModel

@Composable
fun StudentListScreen(
    viewModel: CRAttendanceViewModel,
    onBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val filteredStudents = remember(students, searchQuery) {
        students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.rrn.contains(searchQuery, ignoreCase = true)
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
        // Header — same compact style as attendance page
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
            }
        }

        // Search field — inline, appears when search is active
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

        // Student list
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "No students yet. Import students in Setup." else "No matching students",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredStudents.size) { index ->
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
}
