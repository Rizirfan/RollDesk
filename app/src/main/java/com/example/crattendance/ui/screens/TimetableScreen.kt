package com.example.crattendance.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.example.crattendance.data.database.TimetableEntity
import com.example.crattendance.ui.main.CRAttendanceViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: CRAttendanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timetable by viewModel.timetable.collectAsState()
    val periodsPerDay by viewModel.periodsPerDay.collectAsState()
    var selectedDay by remember { mutableIntStateOf(1) }
    var isEditing by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val periods = remember(periodsPerDay) { (1..periodsPerDay).toList() }
    val context = LocalContext.current

    val timetablePdfPath by viewModel.timetablePdfPath.collectAsState()
    var showDeletePdfDialog by remember { mutableStateOf(false) }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
                val destFile = File(dir, "timetable.pdf")
                inputStream?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.saveTimetablePdfPath(destFile.absolutePath)
                Toast.makeText(context, "Timetable PDF saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val activeTimetable = remember(timetable, selectedDay, periodsPerDay) {
        timetable.filter { it.dayOfWeek == selectedDay && it.period <= periodsPerDay }
    }

    val editFields = remember(activeTimetable, isEditing) {
        mutableStateMapOf<Int, String>().apply {
            periods.forEach { p ->
                val existing = activeTimetable.find { it.period == p }
                this[p] = existing?.subjectName ?: ""
            }
        }
    }

    LaunchedEffect(selectedDay) {
        isEditing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = {
                            isEditing = true
                            editFields.clear()
                            periods.forEach { p ->
                                val existing = activeTimetable.find { it.period == p }
                                editFields[p] = existing?.subjectName ?: ""
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Timetable", tint = MaterialTheme.colorScheme.primary)
                        }
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
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedDay - 1,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                daysOfWeek.forEachIndexed { idx, name ->
                    Tab(
                        selected = selectedDay == idx + 1,
                        onClick = { selectedDay = idx + 1 },
                        text = { Text(name, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Periods per day",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(6, 7, 8).forEach { option ->
                    FilterChip(
                        selected = periodsPerDay == option,
                        onClick = { viewModel.setPeriodsPerDay(option) },
                        label = { Text("$option Hours") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditing) {
                Text(
                    "${daysOfWeek[selectedDay - 1]} — enter subjects (leave blank for free)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    periods.forEachIndexed { idx, periodNum ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "P$periodNum",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(36.dp)
                            )
                            OutlinedTextField(
                                value = editFields[periodNum] ?: "",
                                onValueChange = { editFields[periodNum] = it },
                                placeholder = { Text("Free Period") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                        if (idx < periods.lastIndex) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val entries = periods.mapNotNull { p ->
                                val name = (editFields[p] ?: "").trim()
                                if (name.isNotEmpty()) {
                                    TimetableEntity(dayOfWeek = selectedDay, period = p, subjectName = name)
                                } else null
                            }
                            viewModel.saveTimetable(selectedDay, entries)
                            isEditing = false
                            Toast.makeText(context, "Timetable saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    periods.forEachIndexed { idx, periodNum ->
                        val matchingPeriod = activeTimetable.find { it.period == periodNum }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Period $periodNum",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = matchingPeriod?.subjectName ?: "Free",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (matchingPeriod != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        if (idx < periods.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    // Timetable PDF section
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Timetable PDF",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (timetablePdfPath != null && File(timetablePdfPath!!).exists()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("timetable.pdf", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                Text("Saved locally", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                try {
                                    val file = File(timetablePdfPath!!)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "View PDF", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                try {
                                    val file = File(timetablePdfPath!!)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Download / Share PDF"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot share PDF", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Upload, contentDescription = "Download PDF", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showDeletePdfDialog = true }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete PDF", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { pdfLauncher.launch(arrayOf("application/pdf")) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Timetable PDF")
                        }
                    }
                }
            }
        }
    }

    if (showDeletePdfDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePdfDialog = false },
            title = { Text("Delete PDF?") },
            text = { Text("Remove the saved timetable PDF?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTimetablePdf()
                        showDeletePdfDialog = false
                        Toast.makeText(context, "PDF deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePdfDialog = false }) { Text("Cancel") }
            }
        )
    }
}
