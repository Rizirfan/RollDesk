package com.example.crattendance.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "college_config")
data class CollegeConfigEntity(
    @PrimaryKey val id: Int = 1,
    val collegeName: String,
    val department: String,
    val course: String,
    val semester: String,
    val section: String
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val rrn: String, // Register Number / ID
    val name: String,
    val phone: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday (typically Mon-Fri)
    val period: Int, // 1 to selected periods-per-day (6/7/8)
    val subjectName: String
)

@Entity(
    tableName = "attendance_records",
    indices = [Index(value = ["studentRrn"]), Index(value = ["date"]), Index(value = ["date", "period"])]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRrn: String,
    val date: String, // Format: "yyyy-MM-dd"
    val period: Int, // 1 to 6
    val subject: String,
    val status: String, // "Present", "Absent", "Medical Leave", "On Duty", "Late"
    val timestamp: Long // EpochMillis
)

@Entity(
    tableName = "elective_students",
    indices = [Index(value = ["studentRrn"])]
)
data class ElectiveStudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val electiveName: String,
    val studentRrn: String
)

@Entity(
    tableName = "elective_attendance_records",
    indices = [Index(value = ["studentRrn"]), Index(value = ["electiveName"])]
)
data class ElectiveAttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRrn: String,
    val electiveName: String,
    val date: String, // Format: "yyyy-MM-dd"
    val subject: String,
    val status: String, // "Present", "Absent", "Medical Leave", "On Duty", "Late"
    val timestamp: Long // EpochMillis
)
