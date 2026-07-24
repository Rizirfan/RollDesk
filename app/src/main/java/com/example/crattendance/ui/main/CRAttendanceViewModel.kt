package com.example.crattendance.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crattendance.data.AppPreferences
import com.example.crattendance.data.DataRepository
import com.example.crattendance.data.database.CollegeConfigEntity
import com.example.crattendance.data.database.ElectiveAttendanceRecordEntity
import com.example.crattendance.data.database.ElectiveStudentEntity
import com.example.crattendance.data.database.StudentEntity
import com.example.crattendance.data.database.TimetableEntity
import com.example.crattendance.utils.BackupHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CRAttendanceViewModel @Inject constructor(
    private val repository: DataRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    val isSetupCompleted: StateFlow<Boolean?> = preferences.isSetupCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val collegeConfig: StateFlow<CollegeConfigEntity?> = repository.collegeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val students: StateFlow<List<StudentEntity>> = repository.allActiveStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetable: StateFlow<List<TimetableEntity>> = repository.fullTimetable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords = repository.allAttendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceThreshold: StateFlow<Int> = preferences.attendanceThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    val timetablePdfPath: StateFlow<String?> = preferences.timetablePdfPath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode: StateFlow<Int> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val periodsPerDay: StateFlow<Int> = preferences.periodsPerDay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)

    // Elective StateFlows
    val electiveNames: StateFlow<List<String>> = repository.allElectiveNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val electiveStudents: StateFlow<List<ElectiveStudentEntity>> = repository.allElectiveStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val electiveAttendanceRecords: StateFlow<List<ElectiveAttendanceRecordEntity>> = repository.allElectiveAttendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setPeriodsPerDay(periods: Int) {
        viewModelScope.launch {
            val normalized = when (periods) {
                7 -> 7
                8 -> 8
                else -> 6
            }
            preferences.setPeriodsPerDay(normalized)
            repository.deleteTimetablePeriodsGreaterThan(normalized)
        }
    }

    // Setup Wizard States
    val setupCollege = MutableStateFlow("")
    val setupDepartment = MutableStateFlow("")
    val setupCourse = MutableStateFlow("")
    val setupSemester = MutableStateFlow("")
    val setupSection = MutableStateFlow("")
    val importedStudentsText = MutableStateFlow("")

    fun saveSetup() {
        viewModelScope.launch {
            // Write config first
            repository.saveCollegeConfig(
                CollegeConfigEntity(
                    collegeName = setupCollege.value,
                    department = setupDepartment.value,
                    course = setupCourse.value,
                    semester = setupSemester.value,
                    section = setupSection.value
                )
            )

            // Import students if text isn't empty
            if (importedStudentsText.value.trim().isNotEmpty()) {
                val parsed = BackupHelper.parseCSVStudents(importedStudentsText.value)
                if (parsed.isNotEmpty()) {
                    repository.saveAllStudents(parsed)
                }
            }

            // Save setup state last to ensure DB is written before navigation re-evaluates
            preferences.setSetupCompleted(true)
        }
    }

    fun saveCollegeConfig(config: CollegeConfigEntity) {
        viewModelScope.launch {
            repository.saveCollegeConfig(config)
        }
    }

    fun addManualStudent(rrn: String, name: String, phone: String? = null, notes: String? = null) {
        viewModelScope.launch {
            if (rrn.trim().isNotEmpty() && name.trim().isNotEmpty()) {
                repository.saveStudent(StudentEntity(rrn.trim(), name.trim(), phone?.trim(), notes?.trim()))
            }
        }
    }

    fun addBulkStudents(csvData: String) {
        viewModelScope.launch {
            val parsed = BackupHelper.parseCSVStudents(csvData)
            if (parsed.isNotEmpty()) {
                repository.saveAllStudents(parsed)
            }
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            if (student.rrn.trim().isNotEmpty() && student.name.trim().isNotEmpty()) {
                repository.updateStudent(
                    student.copy(
                        rrn = student.rrn.trim(),
                        name = student.name.trim(),
                        phone = student.phone?.trim(),
                        notes = student.notes?.trim()
                    )
                )
            }
        }
    }

    fun saveTimetable(dayOfWeek: Int, timetableList: List<TimetableEntity>) {
        viewModelScope.launch {
            repository.deleteTimetableForDay(dayOfWeek)
            repository.saveAllTimetablePeriods(timetableList)
        }
    }

    fun saveTimetablePeriod(period: TimetableEntity) {
        viewModelScope.launch {
            repository.saveTimetablePeriod(period)
        }
    }

    fun deleteTimetablePeriod(dayOfWeek: Int, period: Int) {
        viewModelScope.launch {
            repository.deleteTimetablePeriod(dayOfWeek, period)
        }
    }

    fun saveAttendance(records: List<com.example.crattendance.data.database.AttendanceRecordEntity>) {
        viewModelScope.launch {
            repository.saveAttendanceRecords(records)
        }
    }

    fun deletePeriodAttendance(date: String, period: Int) {
        viewModelScope.launch {
            repository.deletePeriodAttendance(date, period)
        }
    }

    fun saveTimetablePdfPath(path: String) {
        viewModelScope.launch {
            preferences.setTimetablePdfPath(path)
        }
    }

    fun deleteTimetablePdf() {
        viewModelScope.launch {
            val path = timetablePdfPath.value
            if (!path.isNullOrEmpty()) {
                val file = java.io.File(path)
                if (file.exists()) file.delete()
            }
            preferences.setTimetablePdfPath(null)
        }
    }

    // Elective methods
    fun saveElectiveStudents(electiveName: String, studentRrns: List<String>) {
        viewModelScope.launch {
            repository.deleteElective(electiveName)
            val entities = studentRrns.map { rrn ->
                ElectiveStudentEntity(electiveName = electiveName, studentRrn = rrn)
            }
            if (entities.isNotEmpty()) {
                repository.saveElectiveStudents(entities)
            }
        }
    }

    fun deleteElective(electiveName: String) {
        viewModelScope.launch {
            repository.deleteElective(electiveName)
        }
    }

    fun renameElective(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.renameElective(oldName, newName)
        }
    }

    fun saveElectiveAttendance(records: List<ElectiveAttendanceRecordEntity>) {
        viewModelScope.launch {
            repository.saveElectiveAttendanceRecords(records)
        }
    }

    fun deleteElectiveAttendanceForDateAndElective(date: String, electiveName: String) {
        viewModelScope.launch {
            repository.deleteElectiveAttendanceForDateAndElective(date, electiveName)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.deleteAllStudents()
            repository.deleteAllTimetable()
            repository.deleteAllAttendance()
            repository.deleteAllElectiveStudents()
            repository.deleteAllElectiveAttendance()
            preferences.setSetupCompleted(false)
        }
    }
}
