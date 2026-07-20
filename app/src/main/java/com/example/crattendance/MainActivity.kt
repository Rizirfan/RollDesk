package com.example.crattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.crattendance.theme.CRAttendanceTheme
import com.example.crattendance.ui.main.CRAttendanceViewModel

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
    )

    setContent {
      val viewModel: CRAttendanceViewModel = hiltViewModel()
      val themeMode by viewModel.themeMode.collectAsState()
      val isSetupCompletedState by viewModel.isSetupCompleted.collectAsState()

      CRAttendanceTheme(themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          // If setup state is not yet loaded from datastore flow (first emission is loading), render spinner
          if (isSetupCompletedState == null) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
              CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
              )
            }
          } else {
            MainNavigation(isSetupCompleted = isSetupCompletedState ?: false)
          }
        }
      }
    }
  }
}
