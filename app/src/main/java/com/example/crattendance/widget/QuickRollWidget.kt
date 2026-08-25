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
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        triggerUpdate(context)
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
                val dayName = getDayName(dayOfWeek)

                val periods = db.timetableDao().getTimetableForDayDirect(dayOfWeek)
                val allRecords = db.attendanceDao().getAllRecordsDirect()

                val todayIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val todayFormatted = java.text.SimpleDateFormat("EEE, d MMM", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val todayRecords = allRecords.filter { it.date == todayIso }

                views.setTextViewText(R.id.widget_day, todayFormatted)
                views.setTextViewText(
                    R.id.widget_period_count,
                    if (periods.isEmpty()) "No periods scheduled"
                    else "${periods.size} periods scheduled today"
                )

                if (periods.isNotEmpty() && todayRecords.isNotEmpty()) {
                    val completedPeriods = todayRecords.map { it.period }.distinct().size
                    views.setTextViewText(
                        R.id.widget_attendance_status,
                        "$completedPeriods / ${periods.size} periods completed"
                    )
                    views.setViewVisibility(R.id.widget_attendance_status, android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_attendance_status, android.view.View.GONE)
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
                views.setTextViewText(R.id.widget_period_count, "Tap to open")
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
