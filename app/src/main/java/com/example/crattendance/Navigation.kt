package com.example.crattendance

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.ui.NavDisplay
import com.example.crattendance.ui.components.AppBottomNavBar
import com.example.crattendance.ui.components.BottomChrome
import com.example.crattendance.ui.main.CRAttendanceViewModel
import com.example.crattendance.ui.navigation.appEnterTransition
import com.example.crattendance.ui.navigation.appPredictivePopTransition
import com.example.crattendance.ui.navigation.appPopTransition
import com.example.crattendance.ui.screens.*

@Composable
fun MainNavigation(isSetupCompleted: Boolean, initialRoute: String? = null) {
    val viewModel: CRAttendanceViewModel = hiltViewModel()
    val activity = LocalContext.current as? Activity

    val startDestination = remember(isSetupCompleted) {
        if (isSetupCompleted) Dashboard else SetupWizard
    }
    val backStack = rememberNavBackStack(startDestination)

    LaunchedEffect(isSetupCompleted, initialRoute) {
        backStack.clear()
        if (isSetupCompleted) {
            backStack.add(Dashboard)
            when (initialRoute) {
                "take_attendance" -> backStack.add(TakeAttendance)
                "timetable" -> backStack.add(TimetableScreen)
            }
        } else {
            backStack.add(SetupWizard)
        }
    }

    var currentTab by remember { mutableIntStateOf(0) }

    fun updateTabFromRoute(key: Any?) {
        when (key) {
            is Dashboard -> currentTab = 0
            is TakeAttendance -> currentTab = 1
            is StudentList -> currentTab = 2
            is Records -> currentTab = 3
            else -> {}
        }
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
            updateTabFromRoute(backStack.lastOrNull())
        } else if (backStack.size == 1) {
            activity?.finish()
        }
    }

    fun navigateToTop(index: Int) {
        backStack.clear()
        backStack.add(Dashboard)
        when (index) {
            0 -> {}
            1 -> backStack.add(TakeAttendance)
            2 -> backStack.add(StudentList)
            3 -> backStack.add(Records)
        }
        currentTab = index
    }

    val showBottomBar = backStack.lastOrNull()?.let { key ->
        key is Dashboard || key is TakeAttendance ||
                key is StudentList || key is Records
    } ?: false

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomChrome(visible = true) {
                        AppBottomNavBar(
                            selectedIndex = currentTab,
                            onItemSelected = ::navigateToTop
                        )
                    }
                }
            }
        ) { padding ->
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.lastOrNull() is Dashboard) {
                        activity?.finish()
                    } else {
                        popBackStack()
                    }
                },
                entryProvider = entryProvider {
                    entry<SetupWizard> {
                        SetupWizardScreen(
                            viewModel = viewModel,
                            onComplete = { backStack.add(Dashboard) },
                            modifier = Modifier
                        )
                    }
                    entry<Dashboard> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTakeAttendance = { navigateToTop(1) },
                            onNavigateToElectiveAttendance = { electiveName ->
                                backStack.add(ElectiveAttendance(electiveName))
                            },
                            onNavigateToElectiveSetup = {
                                backStack.add(ElectiveSetup)
                            },
                            onNavigateToStudentList = { navigateToTop(2) },
                            onNavigateToTimetable = { backStack.add(TimetableScreen) },
                            onNavigateToSettings = { backStack.add(SettingsScreen) },
                            onNavigateToRecords = { navigateToTop(3) },
                            onNavigateToReports = { backStack.add(Reports) },
                            onNavigateToAnalytics = { backStack.add(Analytics) },
                            onEditSession = { date, period -> backStack.add(EditAttendance(date, period)) },
                            onEditElectiveSession = { date, electiveName, subject ->
                                backStack.add(EditElectiveAttendance(date, electiveName, subject))
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                    entry<TakeAttendance> {
                        TakeAttendanceScreen(
                            viewModel = viewModel,
                            onBack = { popBackStack() },
                            modifier = Modifier.padding(padding)
                        )
                    }
                    entry<ElectiveSetup> {
                        ElectiveSetupScreen(
                            viewModel = viewModel,
                            onStartAttendance = { electiveName ->
                                backStack.add(ElectiveAttendance(electiveName))
                            },
                            onBack = { popBackStack() },
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                    }
                    entry<ElectiveAttendance> { key ->
                        TakeAttendanceScreen(
                            viewModel = viewModel,
                            isElective = true,
                            electiveName = key.electiveName,
                            onBack = { popBackStack() },
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                    }
                    entry<StudentList> {
                        StudentListScreen(
                            viewModel = viewModel,
                            onBack = { popBackStack() },
                            onNavigateToDetails = { rrn -> backStack.add(StudentDetails(rrn)) },
                            modifier = Modifier.padding(padding)
                        )
                    }
                    entry<StudentDetails> { key ->
                        StudentDetailsScreen(
                            viewModel = viewModel,
                            rrn = key.rrn,
                            onBack = { popBackStack() },
                            modifier = Modifier
                        )
                    }
                    entry<TimetableScreen> {
                        TimetableScreen(
                            viewModel = viewModel,
                            onBack = { popBackStack() },
                            modifier = Modifier
                        )
                    }
                    entry<SettingsScreen> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { popBackStack() },
                            onNavigateToRecords = {
                                backStack.add(Records)
                            },
                            onClearAllData = {
                                backStack.clear()
                                backStack.add(SetupWizard)
                            },
                            modifier = Modifier
                        )
                    }
                    entry<Records> {
                        RecordsScreen(
                            viewModel = viewModel,
                            onBack = { popBackStack() },
                            modifier = Modifier.padding(padding)
                        )
                    }
                    entry<Reports> {
                        ReportsScreen(
                            viewModel = viewModel,
                            onBack = { popBackStack() },
                            modifier = Modifier
                        )
                    }
                    entry<Analytics> {
                        AnalyticsScreen(
                            viewModel = viewModel,
                            onBack = { popBackStack() },
                            modifier = Modifier
                        )
                    }
                    entry<EditAttendance> { key ->
                        TakeAttendanceScreen(
                            viewModel = viewModel,
                            editDate = key.date,
                            editPeriod = key.period,
                            onBack = { popBackStack() },
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                    }
                    entry<EditElectiveAttendance> { key ->
                        TakeAttendanceScreen(
                            viewModel = viewModel,
                            isElective = true,
                            electiveName = key.electiveName,
                            editDate = key.date,
                            editSubject = key.subject,
                            onBack = { popBackStack() },
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                    }
                },
                transitionSpec = { appEnterTransition() },
                popTransitionSpec = { appPopTransition() },
                predictivePopTransitionSpec = { swipeEdge ->
                    appPredictivePopTransition(swipeEdge)
                },
                sizeTransform = null
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .align(androidx.compose.ui.Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
        )
    }
}
