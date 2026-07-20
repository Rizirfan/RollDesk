<div align="center">

# RollDesk

### Simple. Fast. Reliable.

A modern Android attendance management app built for Class Representatives to take, track, and manage classroom attendance with ease.

[![Release](https://img.shields.io/github/v/release/Rizirfan/RollDesk?style=for-the-badge&color=0D7377)](https://github.com/Rizirfan/RollDesk/releases/latest)
[![License](https://img.shields.io/github/license/Rizirfan/RollDesk?style=for-the-badge&color=0D7377)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

[Download APK](https://github.com/Rizirfan/RollDesk/releases/latest) | [Documentation](DOCUMENTATION.md) | [Report Issue](https://github.com/Rizirfan/RollDesk/issues)

</div>

---

## Features

| Feature | Description |
|---------|-------------|
| **Attendance Tracking** | 2-step wizard for taking roll call with 5 status options (Present, Absent, Medical Leave, On Duty, Late) |
| **Elective Management** | Create electives, enroll students, and take separate elective attendance |
| **Dashboard** | At-a-glance overview with today's timetable, quick stats, and recent sessions |
| **Timetable Editor** | Weekly class schedule editor (Mon-Fri, 6 periods) with PDF upload support |
| **Student Roster** | Searchable class roster with per-student attendance metrics |
| **Reports & Export** | Generate attendance reports in PDF, Excel (.xlsx), or CSV format |
| **Analytics** | Visual attendance analytics with circular and bar charts |
| **Dark Mode** | Full light/dark theme with system default, light, and dark options |
| **Backup & Restore** | Export/import all data as JSON for safekeeping |
| **Quick Share** | Share attendance via text, PDF, or system share sheet |

## Screenshots

> *Screenshots coming soon*

## Download

Get the latest release directly from GitHub:

**[Download RollDesk APK (v1.0.0)](https://github.com/Rizirfan/RollDesk/releases/tag/v1.0.0)**

> Requires Android 8.0 (API 26) or higher. Enable **Install from unknown sources** in your device settings to install the APK.

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin 2.3.20 |
| **UI** | Jetpack Compose + Material Design 3 |
| **Architecture** | MVVM + Repository Pattern |
| **Database** | Room (SQLite) |
| **DI** | Dagger Hilt |
| **Navigation** | Jetpack Navigation3 |
| **Preferences** | DataStore |
| **Export** | Apache POI (Excel), OpenCSV, Android PdfDocument API |

## Architecture

```
com.example.crattendance/
├── CRAttendanceApp.kt              # Application entry (Hilt)
├── MainActivity.kt                  # Single activity
├── Navigation.kt                    # Navigation3 routing
├── NavigationKeys.kt               # Typed route definitions
│
├── data/
│   ├── AppPreferences.kt           # DataStore preferences
│   ├── DataRepository.kt           # Central data repository
│   └── database/
│       ├── AppDatabase.kt          # Room database (6 tables)
│       ├── Daos.kt                 # Data Access Objects
│       └── Entities.kt             # Data entities
│
├── di/
│   └── DatabaseModule.kt           # Hilt DI module
│
├── theme/
│   ├── Color.kt                    # Color palette
│   ├── Theme.kt                    # Light/Dark themes
│   ├── Type.kt                     # Typography
│   └── AttendanceStatus.kt         # Status enum + colors
│
├── ui/
│   ├── components/
│   │   ├── BottomNavBar.kt         # Bottom navigation
│   │   └── DashboardWidgets.kt     # Reusable UI widgets
│   ├── main/
│   │   └── CRAttendanceViewModel.kt # Shared ViewModel
│   └── screens/
│       ├── DashboardScreen.kt       # Home overview
│       ├── TakeAttendanceScreen.kt  # Attendance wizard
│       ├── ElectiveSetupScreen.kt   # Elective management
│       ├── StudentListScreen.kt     # Class roster
│       ├── StudentDetailsScreen.kt  # Student metrics
│       ├── TimetableScreen.kt       # Schedule editor
│       ├── ReportsScreen.kt         # Export center
│       ├── AnalyticsScreen.kt       # Visual analytics
│       ├── SettingsScreen.kt        # App settings
│       └── SetupWizardScreen.kt     # First-run setup
│
└── utils/
    ├── BackupHelper.kt             # JSON backup/restore
    └── ExportHelper.kt             # PDF/Excel/CSV generation
```

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK 36

### Build from Source

```bash
# Clone the repository
git clone https://github.com/Rizirfan/RollDesk.git

# Navigate to project directory
cd RollDesk

# Build debug APK
./gradlew assembleDebug
```

The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.

### Install via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## First-Time Setup

1. Install and open RollDesk
2. Enter your **College Profile** (college name, department, course, semester, section)
3. Import students by pasting CSV data (`RRN,Name` per line)
4. Set up your weekly timetable
5. Start taking attendance!

## Documentation

For complete documentation including:

- Detailed feature descriptions
- Database schema reference
- All screens and workflows
- Data models and API reference

See **[DOCUMENTATION.md](DOCUMENTATION.md)**

## Permissions

RollDesk does **not** require any special permissions. All data is stored locally on your device.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Rizirfan** - [GitHub](https://github.com/Rizirfan)

---

<div align="center">

**Made with Kotlin + Jetpack Compose**

</div>
