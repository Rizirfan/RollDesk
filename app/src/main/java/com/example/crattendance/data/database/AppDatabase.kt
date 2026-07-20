package com.example.crattendance.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CollegeConfigEntity::class,
        StudentEntity::class,
        TimetableEntity::class,
        AttendanceRecordEntity::class,
        ElectiveStudentEntity::class,
        ElectiveAttendanceRecordEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collegeConfigDao(): CollegeConfigDao
    abstract fun studentDao(): StudentDao
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun electiveStudentDao(): ElectiveStudentDao
    abstract fun electiveAttendanceDao(): ElectiveAttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS elective_students (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        electiveName TEXT NOT NULL,
                        studentRrn TEXT NOT NULL
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elective_students_studentRrn ON elective_students(studentRrn)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS elective_attendance_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentRrn TEXT NOT NULL,
                        electiveName TEXT NOT NULL,
                        date TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        status TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elective_attendance_records_studentRrn ON elective_attendance_records(studentRrn)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elective_attendance_records_electiveName ON elective_attendance_records(electiveName)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_studentRrn ON attendance_records(studentRrn)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_date ON attendance_records(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_date_period ON attendance_records(date, period)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cr_attendance_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
