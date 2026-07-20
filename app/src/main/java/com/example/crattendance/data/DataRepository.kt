package com.example.crattendance.data

import com.example.crattendance.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val timetableDao: TimetableDao,
    private val attendanceDao: AttendanceDao,
    private val collegeConfigDao: CollegeConfigDao,
    private val electiveStudentDao: ElectiveStudentDao,
    private val electiveAttendanceDao: ElectiveAttendanceDao
) {
    // College Config
    val collegeConfig: Flow<CollegeConfigEntity?> = collegeConfigDao.getConfig()
    suspend fun getCollegeConfigDirect(): CollegeConfigEntity? = withContext(Dispatchers.IO) {
        collegeConfigDao.getConfigDirect()
    }
    suspend fun saveCollegeConfig(config: CollegeConfigEntity) = withContext(Dispatchers.IO) {
        collegeConfigDao.insertOrUpdate(config)
    }

    // Students
    val allActiveStudents: Flow<List<StudentEntity>> = studentDao.getAllActiveStudents()
    suspend fun getAllActiveStudentsDirect(): List<StudentEntity> = withContext(Dispatchers.IO) {
        studentDao.getAllActiveStudentsDirect()
    }
    suspend fun getStudentByRrn(rrn: String): StudentEntity? = withContext(Dispatchers.IO) {
        studentDao.getStudentByRrn(rrn)
    }
    suspend fun saveStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.insert(student)
    }
    suspend fun saveAllStudents(students: List<StudentEntity>) = withContext(Dispatchers.IO) {
        studentDao.insertAll(students)
    }
    suspend fun deleteAllStudents() = withContext(Dispatchers.IO) {
        studentDao.deleteAll()
    }

    // Timetable
    val fullTimetable: Flow<List<TimetableEntity>> = timetableDao.getFullTimetable()
    suspend fun getFullTimetableDirect(): List<TimetableEntity> = withContext(Dispatchers.IO) {
        timetableDao.getFullTimetableDirect()
    }
    fun getTimetableForDay(dayOfWeek: Int) = timetableDao.getTimetableForDay(dayOfWeek)
    suspend fun getTimetableForDayDirect(dayOfWeek: Int): List<TimetableEntity> = withContext(Dispatchers.IO) {
        timetableDao.getTimetableForDayDirect(dayOfWeek)
    }
    suspend fun saveTimetablePeriod(period: TimetableEntity) = withContext(Dispatchers.IO) {
        timetableDao.insertPeriod(period)
    }
    suspend fun saveAllTimetablePeriods(periods: List<TimetableEntity>) = withContext(Dispatchers.IO) {
        timetableDao.insertAll(periods)
    }
    suspend fun deleteTimetablePeriod(dayOfWeek: Int, period: Int) = withContext(Dispatchers.IO) {
        timetableDao.deletePeriod(dayOfWeek, period)
    }
    suspend fun deleteTimetableForDay(dayOfWeek: Int) = withContext(Dispatchers.IO) {
        timetableDao.deleteTimetableForDay(dayOfWeek)
    }
    suspend fun deleteAllTimetable() = withContext(Dispatchers.IO) {
        timetableDao.deleteAll()
    }

    // Attendance
    val allAttendanceRecords: Flow<List<AttendanceRecordEntity>> = attendanceDao.getAllRecords()
    suspend fun getAllAttendanceRecordsDirect(): List<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getAllRecordsDirect()
    }
    fun getAttendanceForDate(date: String) = attendanceDao.getRecordsForDate(date)
    suspend fun getAttendanceForDateDirect(date: String): List<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getRecordsForDateDirect(date)
    }
    fun getAttendanceForPeriod(date: String, period: Int) = attendanceDao.getRecordsForPeriod(date, period)
    suspend fun getAttendanceForPeriodDirect(date: String, period: Int): List<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getRecordsForPeriodDirect(date, period)
    }
    fun getAttendanceForStudent(rrn: String) = attendanceDao.getRecordsForStudent(rrn)
    suspend fun saveAttendanceRecords(records: List<AttendanceRecordEntity>) = withContext(Dispatchers.IO) {
        attendanceDao.insertAll(records)
    }
    suspend fun deletePeriodAttendance(date: String, period: Int) = withContext(Dispatchers.IO) {
        attendanceDao.deletePeriodAttendance(date, period)
    }
    suspend fun deleteAllAttendance() = withContext(Dispatchers.IO) {
        attendanceDao.deleteAll()
    }

    // Elective Students
    val allElectiveStudents: Flow<List<ElectiveStudentEntity>> = electiveStudentDao.getAllElectiveStudents()
    suspend fun getAllElectiveStudentsDirect(): List<ElectiveStudentEntity> = withContext(Dispatchers.IO) {
        electiveStudentDao.getAllElectiveStudentsDirect()
    }
    val allElectiveNames: Flow<List<String>> = electiveStudentDao.getAllElectiveNames()
    suspend fun getAllElectiveNamesDirect(): List<String> = withContext(Dispatchers.IO) {
        electiveStudentDao.getAllElectiveNamesDirect()
    }
    fun getStudentsForElective(electiveName: String): Flow<List<ElectiveStudentEntity>> =
        electiveStudentDao.getStudentsForElective(electiveName)
    suspend fun getStudentsForElectiveDirect(electiveName: String): List<ElectiveStudentEntity> = withContext(Dispatchers.IO) {
        electiveStudentDao.getStudentsForElectiveDirect(electiveName)
    }
    suspend fun saveElectiveStudents(electiveStudents: List<ElectiveStudentEntity>) = withContext(Dispatchers.IO) {
        electiveStudentDao.insertAll(electiveStudents)
    }
    suspend fun deleteElective(electiveName: String) = withContext(Dispatchers.IO) {
        electiveStudentDao.deleteElective(electiveName)
    }
    suspend fun deleteAllElectiveStudents() = withContext(Dispatchers.IO) {
        electiveStudentDao.deleteAll()
    }

    // Elective Attendance
    val allElectiveAttendanceRecords: Flow<List<ElectiveAttendanceRecordEntity>> = electiveAttendanceDao.getAllRecords()
    suspend fun getAllElectiveAttendanceRecordsDirect(): List<ElectiveAttendanceRecordEntity> = withContext(Dispatchers.IO) {
        electiveAttendanceDao.getAllRecordsDirect()
    }
    fun getElectiveAttendanceForElective(electiveName: String): Flow<List<ElectiveAttendanceRecordEntity>> =
        electiveAttendanceDao.getRecordsForElective(electiveName)
    fun getElectiveAttendanceForDateAndElective(date: String, electiveName: String): Flow<List<ElectiveAttendanceRecordEntity>> =
        electiveAttendanceDao.getRecordsForDateAndElective(date, electiveName)
    suspend fun saveElectiveAttendanceRecords(records: List<ElectiveAttendanceRecordEntity>) = withContext(Dispatchers.IO) {
        electiveAttendanceDao.insertAll(records)
    }
    suspend fun deleteElectiveAttendanceForDateAndElective(date: String, electiveName: String) = withContext(Dispatchers.IO) {
        electiveAttendanceDao.deleteRecordsForDateAndElective(date, electiveName)
    }
    suspend fun deleteAllElectiveAttendance() = withContext(Dispatchers.IO) {
        electiveAttendanceDao.deleteAll()
    }
}
