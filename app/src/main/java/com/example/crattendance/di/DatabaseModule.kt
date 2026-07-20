package com.example.crattendance.di

import android.content.Context
import com.example.crattendance.data.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideCollegeConfigDao(database: AppDatabase): CollegeConfigDao {
        return database.collegeConfigDao()
    }

    @Provides
    fun provideStudentDao(database: AppDatabase): StudentDao {
        return database.studentDao()
    }

    @Provides
    fun provideTimetableDao(database: AppDatabase): TimetableDao {
        return database.timetableDao()
    }

    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }

    @Provides
    fun provideElectiveStudentDao(database: AppDatabase): ElectiveStudentDao {
        return database.electiveStudentDao()
    }

    @Provides
    fun provideElectiveAttendanceDao(database: AppDatabase): ElectiveAttendanceDao {
        return database.electiveAttendanceDao()
    }
}
