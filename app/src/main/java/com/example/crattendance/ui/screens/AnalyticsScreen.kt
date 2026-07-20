package com.example.crattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.crattendance.ui.components.SleekBarChart
import com.example.crattendance.ui.components.SleekCircularChart
import com.example.crattendance.ui.main.CRAttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: CRAttendanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val records by viewModel.attendanceRecords.collectAsState()
    val threshold by viewModel.attendanceThreshold.collectAsState()

    val overallPercentage = remember(students, records) {
        val pres = records.count { it.status == "Present" }
        val tot = records.size
        if (tot == 0) 100f else (pres.toFloat() / tot * 100f)
    }

    val subjectData = remember(records) {
        val subjects = records.map { it.subject }.distinct()
        subjects.map { subName ->
            val subRecs = records.filter { it.subject == subName }
            val pres = subRecs.count { it.status == "Present" }
            val tot = subRecs.size
            val pct = if (tot == 0) 100f else (pres.toFloat() / tot * 100f)
            Pair(subName.take(8), pct)
        }.take(6)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
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
            Text("Overall Performance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Average class attendance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (records.isEmpty()) "No data yet" else "Target: ${threshold}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${records.size} total records",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SleekCircularChart(
                        percentage = overallPercentage,
                        label = "Overall",
                        size = 80.dp,
                        color = if (overallPercentage >= threshold.toFloat()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (subjectData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Subject Comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SleekBarChart(data = subjectData)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
