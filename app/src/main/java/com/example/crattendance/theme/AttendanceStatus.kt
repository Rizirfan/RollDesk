package com.example.crattendance.theme

import androidx.compose.ui.graphics.Color

val StatusPresent = Color(0xFF2E7D32)
val StatusAbsent = Color(0xFFD32F2F)
val StatusMedicalLeave = Color(0xFF1565C0)
val StatusOnDuty = Color(0xFF6A1B9A)
val StatusLate = Color(0xFFEF6C00)

enum class AttendanceStatus(val displayName: String, val color: Color, val labelShort: String) {
    PRESENT("Present", StatusPresent, "P"),
    ABSENT("Absent", StatusAbsent, "A"),
    MEDICAL_LEAVE("Medical Leave", StatusMedicalLeave, "ML"),
    ON_DUTY("On Duty", StatusOnDuty, "OD"),
    LATE("Late", StatusLate, "L")
}
