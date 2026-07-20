package com.example.crattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crattendance.theme.Teal
import com.example.crattendance.ui.main.CRAttendanceViewModel

@Composable
fun SetupWizardScreen(
    viewModel: CRAttendanceViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }

    val college by viewModel.setupCollege.collectAsState()
    val department by viewModel.setupDepartment.collectAsState()
    val course by viewModel.setupCourse.collectAsState()
    val semester by viewModel.setupSemester.collectAsState()
    val section by viewModel.setupSection.collectAsState()
    val studentsText by viewModel.importedStudentsText.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CR Attendance",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Teal,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Simple. Fast. Reliable.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Step indicator
            Row(
                modifier = Modifier.padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Teal)
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(if (step >= 2) Teal else MaterialTheme.colorScheme.outline)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (step >= 2) Teal else MaterialTheme.colorScheme.outline)
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (step == 1) {
                        Text("College Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Enter your class details. Everything stays on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
                        )

                        OutlinedTextField(value = college, onValueChange = { viewModel.setupCollege.value = it }, label = { Text("College Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = department, onValueChange = { viewModel.setupDepartment.value = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = course, onValueChange = { viewModel.setupCourse.value = it }, label = { Text("Course Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(value = semester, onValueChange = { viewModel.setupSemester.value = it }, label = { Text("Semester") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                            OutlinedTextField(value = section, onValueChange = { viewModel.setupSection.value = it }, label = { Text("Section") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                        }
                    } else {
                        Text("Student Import", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Paste CSV data: RRN,Name (one per line).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
                        )

                        OutlinedTextField(
                            value = studentsText,
                            onValueChange = { viewModel.importedStudentsText.value = it },
                            label = { Text("RRN,Name CSV Paste") },
                            placeholder = { Text("RRN,Name") },
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 12
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (step == 2) {
                            TextButton(onClick = { step = 1 }) { Text("Back") }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Button(
                            onClick = {
                                if (step == 1) step = 2
                                else { viewModel.saveSetup(); onComplete() }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (step == 1) "Next" else "Finish")
                        }
                    }
                }
            }
        }
    }
}
