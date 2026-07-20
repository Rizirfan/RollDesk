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
import com.example.crattendance.ui.main.CRAttendanceViewModel
import com.example.crattendance.ui.screens.*

@Composable
fun MainNavigation(isSetupCompleted: Boolean) {
    val viewModel: CRAttendanceViewModel = hiltViewModel()
    val activity = LocalContext.current as? Activity

    val startDestination = remember(isSetupCompleted) {
        if (isSetupCompleted) Dashboard else SetupWizard
    }
    val backStack = rememberNavBackStack(startDestination)

    LaunchedEffect(isSetupCompleted) {
        backStack.clear()
        if (isSetupCompleted) {
            backStack.add(Dashboard)
        } else {
            backStack.add(SetupWizard)
        }
    }

    var bottomBarSelectedIndex by remember { mutableIntStateOf(0) }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
            when (backStack.lastOrNull()) {
                is Dashboard -> bottomBarSelectedIndex = 0
                is TakeAttendance -> bottomBarSelectedIndex = 1
                is StudentList -> bottomBarSelectedIndex = 2
                else -> {}
            }
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
        }
        bottomBarSelectedIndex = index
    }

    val showBottomBar = backStack.lastOrNull()?.let { key ->
        key is Dashboard || key is TakeAttendance || key is StudentList
    } ?: false

    Box(modifier = Modifier.fillMaxSize()) {
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
                            onNavigateToTakeAttendance = { navigateToTop(1) },
                            onNavigateToElectiveAttendance = {
                                backStack.add(ElectiveSetup)
                            },
                            onNavigateToStudentList = { navigateToTop(2) },
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
                            onBack = { popBackStack() },
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
                            onBack = { popBackStack() },
                            onNavigateToDetails = { rrn -> backStack.add(StudentDetails(rrn)) },
                            modifier = Modifier.padding(padding)
                        )
                    }
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
                        onClearAllData = {
                            backStack.clear()
                            backStack.add(SetupWizard)
                        },
                        modifier = Modifier
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .align(androidx.compose.ui.Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
        )
    }
}
