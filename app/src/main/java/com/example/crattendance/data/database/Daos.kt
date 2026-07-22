package com.example.crattendance.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollegeConfigDao {
    @Query("SELECT * FROM college_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<CollegeConfigEntity?>

    @Query("SELECT * FROM college_config WHERE id = 1 LIMIT 1")
    fun getConfigDirect(): CollegeConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(config: CollegeConfigEntity)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY rrn ASC")
    fun getAllActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY rrn ASC")
    fun getAllActiveStudentsDirect(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE rrn = :rrn LIMIT 1")
    fun getStudentByRrn(rrn: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(students: List<StudentEntity>)

    @Update
    fun update(student: StudentEntity)

    @Query("DELETE FROM students")
    fun deleteAll()
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable ORDER BY dayOfWeek ASC, period ASC")
    fun getFullTimetable(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable ORDER BY dayOfWeek ASC, period ASC")
    fun getFullTimetableDirect(): List<TimetableEntity>

    @Query("SELECT * FROM timetable WHERE dayOfWeek = :dayOfWeek ORDER BY period ASC")
    fun getTimetableForDay(dayOfWeek: Int): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE dayOfWeek = :dayOfWeek ORDER BY period ASC")
    fun getTimetableForDayDirect(dayOfWeek: Int): List<TimetableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPeriod(period: TimetableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(periods: List<TimetableEntity>)

    @Query("DELETE FROM timetable")
    fun deleteAll()

    @Query("DELETE FROM timetable WHERE dayOfWeek = :dayOfWeek AND period = :period")
    fun deletePeriod(dayOfWeek: Int, period: Int)

    @Query("DELETE FROM timetable WHERE dayOfWeek = :dayOfWeek")
    fun deleteTimetableForDay(dayOfWeek: Int)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecordsDirect(): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY period ASC")
    fun getRecordsForDate(date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY period ASC")
    fun getRecordsForDateDirect(date: String): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND period = :period")
    fun getRecordsForPeriod(date: String, period: Int): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND period = :period")
    fun getRecordsForPeriodDirect(date: String, period: Int): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE studentRrn = :studentRrn ORDER BY date DESC, period DESC")
    fun getRecordsForStudent(studentRrn: String): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(records: List<AttendanceRecordEntity>)

    @Query("DELETE FROM attendance_records WHERE date = :date AND period = :period")
    fun deletePeriodAttendance(date: String, period: Int)

    @Query("DELETE FROM attendance_records")
    fun deleteAll()
}

@Dao
interface ElectiveStudentDao {
    @Query("SELECT * FROM elective_students ORDER BY electiveName ASC, studentRrn ASC")
    fun getAllElectiveStudents(): Flow<List<ElectiveStudentEntity>>

    @Query("SELECT * FROM elective_students ORDER BY electiveName ASC, studentRrn ASC")
    fun getAllElectiveStudentsDirect(): List<ElectiveStudentEntity>

    @Query("SELECT DISTINCT electiveName FROM elective_students ORDER BY electiveName ASC")
    fun getAllElectiveNames(): Flow<List<String>>

    @Query("SELECT DISTINCT electiveName FROM elective_students ORDER BY electiveName ASC")
    fun getAllElectiveNamesDirect(): List<String>

    @Query("SELECT * FROM elective_students WHERE electiveName = :electiveName ORDER BY studentRrn ASC")
    fun getStudentsForElective(electiveName: String): Flow<List<ElectiveStudentEntity>>

    @Query("SELECT * FROM elective_students WHERE electiveName = :electiveName ORDER BY studentRrn ASC")
    fun getStudentsForElectiveDirect(electiveName: String): List<ElectiveStudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(electiveStudents: List<ElectiveStudentEntity>)

    @Query("UPDATE elective_students SET electiveName = :newName WHERE electiveName = :oldName")
    fun renameElective(oldName: String, newName: String)

    @Query("DELETE FROM elective_students WHERE electiveName = :electiveName")
    fun deleteElective(electiveName: String)

    @Query("DELETE FROM elective_students")
    fun deleteAll()
}

@Dao
interface ElectiveAttendanceDao {
    @Query("SELECT * FROM elective_attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ElectiveAttendanceRecordEntity>>

    @Query("SELECT * FROM elective_attendance_records ORDER BY timestamp DESC")
    fun getAllRecordsDirect(): List<ElectiveAttendanceRecordEntity>

    @Query("SELECT * FROM elective_attendance_records WHERE electiveName = :electiveName ORDER BY timestamp DESC")
    fun getRecordsForElective(electiveName: String): Flow<List<ElectiveAttendanceRecordEntity>>

    @Query("SELECT * FROM elective_attendance_records WHERE electiveName = :electiveName ORDER BY timestamp DESC")
    fun getRecordsForElectiveDirect(electiveName: String): List<ElectiveAttendanceRecordEntity>

    @Query("SELECT * FROM elective_attendance_records WHERE date = :date AND electiveName = :electiveName ORDER BY timestamp DESC")
    fun getRecordsForDateAndElective(date: String, electiveName: String): Flow<List<ElectiveAttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(records: List<ElectiveAttendanceRecordEntity>)

    @Query("UPDATE elective_attendance_records SET electiveName = :newName WHERE electiveName = :oldName")
    fun renameElective(oldName: String, newName: String)

    @Query("DELETE FROM elective_attendance_records WHERE date = :date AND electiveName = :electiveName")
    fun deleteRecordsForDateAndElective(date: String, electiveName: String)

    @Query("DELETE FROM elective_attendance_records")
    fun deleteAll()
}
