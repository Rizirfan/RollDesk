package com.example.crattendance

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.ui.NavDisplay
import com.example.crattendance.ui.components.AppBottomNavBar
import com.example.crattendance.ui.main.CRAttendanceViewModel
import com.example.crattendance.ui.screens.*

@Composable
fun MainNavigation(isSetupCompleted: Boolean) {
    val viewModel: CRAttendanceViewModel = hiltViewModel()

    // Determine initial route dynamically to prevent flashing/theme issues
    val startDestination = remember(isSetupCompleted) {
        if (isSetupCompleted) Dashboard else SetupWizard
    }
    val backStack = rememberNavBackStack(startDestination)

    // Sync backStack when preferences load to prevent flashing of SetupWizard
    LaunchedEffect(isSetupCompleted) {
        backStack.clear()
        if (isSetupCompleted) {
            backStack.add(Dashboard)
        } else {
            backStack.add(SetupWizard)
        }
    }

    var bottomBarSelectedIndex by remember { mutableIntStateOf(0) }

    val showBottomBar = backStack.lastOrNull()?.let { key ->
        key is Dashboard || key is TakeAttendance || key is StudentList
    } ?: false

    fun navigateToTop(index: Int) {
        // Prevent back stack accumulation when switching top level tabs
        backStack.clear()
        when (index) {
            0 -> backStack.add(Dashboard)
            1 -> backStack.add(TakeAttendance)
            2 -> backStack.add(StudentList)
        }
        bottomBarSelectedIndex = index
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                val current = backStack.lastOrNull()
                if (current is Dashboard) {
                    // Quit application if we press back on Home screen
                    backStack.removeLastOrNull()
                } else if (current is TakeAttendance || current is StudentList) {
                    // Redirect back to Dashboard if pressing back on other main tabs
                    backStack.clear()
                    backStack.add(Dashboard)
                    bottomBarSelectedIndex = 0
                } else {
                    // Normal popup for detail screens
                    backStack.removeLastOrNull()
                    val last = backStack.lastOrNull()
                    when (last) {
                        is Dashboard -> bottomBarSelectedIndex = 0
                        is TakeAttendance -> bottomBarSelectedIndex = 1
                        is StudentList -> bottomBarSelectedIndex = 2
                        else -> {}
                    }
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
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                AppBottomNavBar(
                                    selectedIndex = bottomBarSelectedIndex,
                                    onItemSelected = ::navigateToTop
                                )
                            }
                        }
                    ) { padding ->
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTakeAttendance = {
                                backStack.clear()
                                backStack.add(TakeAttendance)
                                bottomBarSelectedIndex = 1
                            },
                            onNavigateToElectiveAttendance = {
                                backStack.add(ElectiveSetup)
                            },
                            onNavigateToStudentList = {
                                backStack.clear()
                                backStack.add(StudentList)
                                bottomBarSelectedIndex = 2
                            },
                            onNavigateToTimetable = { backStack.add(TimetableScreen) },
                            onNavigateToSettings = { backStack.add(SettingsScreen) },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
                entry<TakeAttendance> {
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                AppBottomNavBar(
                                    selectedIndex = bottomBarSelectedIndex,
                                    onItemSelected = ::navigateToTop
                                )
                            }
                        }
                    ) { padding ->
                        TakeAttendanceScreen(
                            viewModel = viewModel,
                            onBack = {
                                backStack.clear()
                                backStack.add(Dashboard)
                                bottomBarSelectedIndex = 0
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
                entry<ElectiveSetup> {
                    ElectiveSetupScreen(
                        viewModel = viewModel,
                        onStartAttendance = { electiveName ->
                            backStack.add(ElectiveAttendance(electiveName))
                        },
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    )
                }
                entry<ElectiveAttendance> { key ->
                    TakeAttendanceScreen(
                        viewModel = viewModel,
                        isElective = true,
                        electiveName = key.electiveName,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier
                    )
                }
                entry<StudentList> {
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                AppBottomNavBar(
                                    selectedIndex = bottomBarSelectedIndex,
                                    onItemSelected = ::navigateToTop
                                )
                            }
                        }
                    ) { padding ->
                        StudentListScreen(
                            viewModel = viewModel,
                            onBack = {
                                backStack.clear()
                                backStack.add(Dashboard)
                                bottomBarSelectedIndex = 0
                            },
                            onNavigateToDetails = { rrn -> backStack.add(StudentDetails(rrn)) },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
                entry<StudentDetails> { key ->
                    StudentDetailsScreen(
                        viewModel = viewModel,
                        rrn = key.rrn,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier
                    )
                }
                entry<TimetableScreen> {
                    TimetableScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier
                    )
                }
                entry<SettingsScreen> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() },
                        onClearAllData = {
                            backStack.clear()
                            backStack.add(SetupWizard)
                        },
                        modifier = Modifier
                    )
                }
            }
        )

        // Dark scrim behind status bar — keeps battery/time icons visible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .align(androidx.compose.ui.Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
        )
    }
}
