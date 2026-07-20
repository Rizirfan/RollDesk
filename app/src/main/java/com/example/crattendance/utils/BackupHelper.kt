package com.example.crattendance.utils

import android.content.Context
import com.example.crattendance.data.DataRepository
import com.example.crattendance.data.database.CollegeConfigEntity
import com.example.crattendance.data.database.StudentEntity
import com.example.crattendance.data.database.TimetableEntity
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.StringReader

object BackupHelper {

    suspend fun exportBackup(context: Context, repository: DataRepository): String = withContext(Dispatchers.IO) {
        val backupJson = JSONObject()

        // 1. College Config
        val config = repository.getCollegeConfigDirect()
        if (config != null) {
            val configJson = JSONObject().apply {
                put("collegeName", config.collegeName)
                put("department", config.department)
                put("course", config.course)
                put("semester", config.semester)
                put("section", config.section)
            }
            backupJson.put("college_config", configJson)
        }

        // 2. Students
        val students = repository.getAllActiveStudentsDirect()
        val studentsArray = JSONArray()
        students.forEach { s ->
            studentsArray.put(JSONObject().apply {
                put("rrn", s.rrn)
                put("name", s.name)
                put("phone", s.phone ?: JSONObject.NULL)
                put("notes", s.notes ?: JSONObject.NULL)
                put("isActive", s.isActive)
            })
        }
        backupJson.put("students", studentsArray)

        // 3. Timetable
        val timetable = repository.getFullTimetableDirect()
        val timetableArray = JSONArray()
        timetable.forEach { t ->
            timetableArray.put(JSONObject().apply {
                put("dayOfWeek", t.dayOfWeek)
                put("period", t.period)
                put("subjectName", t.subjectName)
            })
        }
        backupJson.put("timetable", timetableArray)

        // 4. Attendance Records
        val records = repository.getAllAttendanceRecordsDirect()
        val recordsArray = JSONArray()
        records.forEach { r ->
            recordsArray.put(JSONObject().apply {
                put("studentRrn", r.studentRrn)
                put("date", r.date)
                put("period", r.period)
                put("subject", r.subject)
                put("status", r.status)
                put("timestamp", r.timestamp)
            })
        }
        backupJson.put("attendance_records", recordsArray)

        return@withContext backupJson.toString()
    }

    suspend fun importBackup(backupJsonStr: String, repository: DataRepository) = withContext(Dispatchers.IO) {
        val root = JSONObject(backupJsonStr)

        if (root.has("college_config")) {
            val c = root.getJSONObject("college_config")
            repository.saveCollegeConfig(
                CollegeConfigEntity(
                    collegeName = c.getString("collegeName"),
                    department = c.getString("department"),
                    course = c.getString("course"),
                    semester = c.getString("semester"),
                    section = c.getString("section")
                )
            )
        }

        if (root.has("students")) {
            val sArr = root.getJSONArray("students")
            val studentsList = mutableListOf<StudentEntity>()
            for (i in 0 until sArr.length()) {
                val s = sArr.getJSONObject(i)
                studentsList.add(
                    StudentEntity(
                        rrn = s.getString("rrn"),
                        name = s.getString("name"),
                        phone = if (s.isNull("phone")) null else s.getString("phone").ifEmpty { null },
                        notes = if (s.isNull("notes")) null else s.getString("notes").ifEmpty { null },
                        isActive = s.optBoolean("isActive", true)
                    )
                )
            }
            if (studentsList.isNotEmpty()) {
                repository.deleteAllStudents()
                repository.saveAllStudents(studentsList)
            }
        }

        if (root.has("timetable")) {
            val tArr = root.getJSONArray("timetable")
            val timetableList = mutableListOf<TimetableEntity>()
            for (i in 0 until tArr.length()) {
                val t = tArr.getJSONObject(i)
                timetableList.add(
                    TimetableEntity(
                        dayOfWeek = t.getInt("dayOfWeek"),
                        period = t.getInt("period"),
                        subjectName = t.getString("subjectName")
                    )
                )
            }
            if (timetableList.isNotEmpty()) {
                repository.deleteAllTimetable()
                repository.saveAllTimetablePeriods(timetableList)
            }
        }

        // Import attendance records if present
        if (root.has("attendance_records")) {
            val rArr = root.getJSONArray("attendance_records")
            val recordsList = mutableListOf<com.example.crattendance.data.database.AttendanceRecordEntity>()
            for (i in 0 until rArr.length()) {
                val r = rArr.getJSONObject(i)
                recordsList.add(
                    com.example.crattendance.data.database.AttendanceRecordEntity(
                        studentRrn = r.getString("studentRrn"),
                        date = r.getString("date"),
                        period = r.getInt("period"),
                        subject = r.getString("subject"),
                        status = r.getString("status"),
                        timestamp = r.getLong("timestamp")
                    )
                )
            }
            if (recordsList.isNotEmpty()) {
                repository.deleteAllAttendance()
                repository.saveAttendanceRecords(recordsList)
            }
        }
    }

    fun parseCSVStudents(csvData: String): List<StudentEntity> {
        val students = mutableListOf<StudentEntity>()
        val lines = csvData.split('\n', '\r')
        lines.forEach { line ->
            val parts = line.split(',')
            if (parts.size >= 2) {
                val rrn = parts[0].trim()
                val name = parts[1].trim()
                val phone = if (parts.size > 2) parts[2].trim() else null
                val notes = if (parts.size > 3) parts[3].trim() else null
                if (rrn.isNotEmpty() && name.isNotEmpty()) {
                    students.add(StudentEntity(rrn, name, phone, notes))
                }
            }
        }
        return students
    }
}
