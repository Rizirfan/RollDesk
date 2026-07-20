package com.example.crattendance

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SetupWizard : NavKey
@Serializable data object Dashboard : NavKey
@Serializable data object TakeAttendance : NavKey
@Serializable data class ElectiveAttendance(val electiveName: String) : NavKey
@Serializable data object ElectiveSetup : NavKey
@Serializable data object StudentList : NavKey
@Serializable data class StudentDetails(val rrn: String) : NavKey
@Serializable data object TimetableScreen : NavKey
@Serializable data object SettingsScreen : NavKey
