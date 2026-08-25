package com.example.crattendance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.crattendance.MainActivity
import com.example.crattendance.R
import com.example.crattendance.data.database.AppDatabase
import kotlinx.coroutines.launch
import java.util.Calendar

class QuickRollWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateQuickRollWidget(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    pendingResult?.finish()
                } catch (e: Exception) {
                    // Ignore finish exception if already finished
                }
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            triggerUpdate(context)
        }

        private fun triggerUpdate(context: Context) {
            val intent = Intent(context, QuickRollWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, QuickRollWidget::class.java))
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }

        private fun updateQuickRollWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_roll)

            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val dayOfWeek = getDayOfWeek()

                val periods = db.timetableDao().getTimetableForDayDirect(dayOfWeek)
                val allRecords = db.attendanceDao().getAllRecordsDirect()

                val todayIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val todayFormatted = java.text.SimpleDateFormat("EEE, d MMM", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val todayRecords = allRecords.filter { it.date == todayIso }

                views.setTextViewText(R.id.widget_day, todayFormatted)

                val completedPeriodNumbers = todayRecords.map { it.period }.distinct()
                val completedCount = completedPeriodNumbers.size

                val slotRows = arrayOf(
                    R.id.period1_row, R.id.period2_row, R.id.period3_row,
                    R.id.period4_row, R.id.period5_row, R.id.period6_row,
                    R.id.period7_row, R.id.period8_row
                )
                val slotTags = arrayOf(
                    R.id.period1_tag, R.id.period2_tag, R.id.period3_tag,
                    R.id.period4_tag, R.id.period5_tag, R.id.period6_tag,
                    R.id.period7_tag, R.id.period8_tag
                )
                val slotNames = arrayOf(
                    R.id.period1_name, R.id.period2_name, R.id.period3_name,
                    R.id.period4_name, R.id.period5_name, R.id.period6_name,
                    R.id.period7_name, R.id.period8_name
                )

                val sortedPeriods = periods.sortedBy { it.period }

                if (sortedPeriods.isEmpty()) {
                    views.setViewVisibility(R.id.widget_no_periods, android.view.View.VISIBLE)
                    for (rowId in slotRows) {
                        views.setViewVisibility(rowId, android.view.View.GONE)
                    }
                    views.setViewVisibility(R.id.widget_attendance_status, android.view.View.GONE)
                } else {
                    views.setViewVisibility(R.id.widget_no_periods, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_attendance_status, android.view.View.VISIBLE)
                    views.setTextViewText(
                        R.id.widget_attendance_status,
                        "$completedCount/${sortedPeriods.size} Completed"
                    )

                    for (i in slotRows.indices) {
                        if (i < sortedPeriods.size) {
                            val period = sortedPeriods[i]
                            views.setViewVisibility(slotRows[i], android.view.View.VISIBLE)
                            views.setTextViewText(slotTags[i], "P${period.period}")
                            views.setTextViewText(slotNames[i], period.subjectName)
                        } else {
                            views.setViewVisibility(slotRows[i], android.view.View.GONE)
                        }
                    }
                }

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "take_attendance")
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_take_roll, pendingIntent)
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_day, "RollDesk")
                views.setViewVisibility(R.id.widget_no_periods, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.widget_attendance_status, android.view.View.GONE)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getDayOfWeek(): Int {
            return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 7
            }
        }

        private fun getDayName(dayOfWeek: Int): String {
            return when (dayOfWeek) {
                1 -> "Monday"
                2 -> "Tuesday"
                3 -> "Wednesday"
                4 -> "Thursday"
                5 -> "Friday"
                6 -> "Saturday"
                7 -> "Sunday"
                else -> ""
            }
        }
    }
}
