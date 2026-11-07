<div align="center">

# RollDesk Documentation

### Complete App Reference Guide

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [App Screens](#app-screens)
   - [Setup Wizard](#1-setup-wizard)
   - [Dashboard](#2-dashboard)
   - [Take Attendance](#3-take-attendance)
   - [Elective Setup](#4-elective-setup)
   - [Student List](#5-student-list)
   - [Student Details](#6-student-details)
   - [Timetable](#7-timetable)
   - [Reports](#8-reports)
   - [Analytics](#9-analytics)
   - [Settings](#10-settings)
   - [Records](#11-records)
   - [Quick Notes (Google Keep)](#12-quick-notes-google-keep)
4. [Attendance System](#attendance-system)
5. [Data Models](#data-models)
6. [Export Formats](#export-formats)
7. [Backup & Restore](#backup--restore)
8. [Tech Stack](#tech-stack)
9. [Project Structure](#project-structure)

---

## Overview

**RollDesk** is a Class Representative attendance management app designed for college students. It provides a streamlined workflow for taking classroom attendance, managing student rosters, tracking elective subjects, generating reports, and visualizing attendance analytics.

- **Current Version:** 1.6.0
- **Package:** `com.example.crattendance`
- **Min Android:** 8.0 (API 26)
- **Target Android:** 16 (API 36)
- **Architecture:** MVVM with Repository Pattern
- **All data is stored locally on-device. No internet required.**

---

## Features

| Feature | Description |
|---------|-------------|
| Attendance Tracking | Take attendance for regular classes with 5 status options |
| Elective Management | Create electives, enroll students, track elective attendance separately |
| Dashboard | Overview with today's timetable, stats, overview card, and recent sessions |
| Quick Notes | Optional note composer that hands text to Google Keep (toggle in Settings) |
| Timetable Editor | Weekly schedule editor with PDF upload |
| Student Roster | Searchable roster with per-student attendance metrics |
| Reports | Export as PDF, Excel (.xlsx), or CSV |
| Analytics | Visual charts for attendance performance |
| Dark Mode | System default / Light / Dark theme support |
| Records History | Browse, search, filter, and delete attendance records (regular and elective) |
| Backup/Restore | JSON export/import of all data |
| Quick Share | Share attendance via text, PDF, or share sheet |

---

## App Screens

### 1. Setup Wizard

The first-run wizard guides new users through initial configuration.

**Step 1 - College Profile:**
| Field | Required | Description |
|-------|----------|-------------|
| College Name | Yes | Your college/university name |
| Department | No | Department or branch |
| Course Name | No | Degree program (e.g., B.Tech CSE) |
| Semester | No | Current semester |
| Section | No | Class section (e.g., A, B) |

**Step 2 - Student Import:**
- Paste CSV text with one student per line
- Format: `RRN,Name` (minimum)
- Optional columns: `Phone,Notes`
- Example:
  ```
  23BCE001,Alice Johnson,9876543210,Hosteler
  23BCE002,Bob Smith
  23BCE003,Charlie Brown,9123456780
  ```

> The wizard only appears on first launch. College profile can be edited later in Settings.

---

### 2. Dashboard

The home screen provides a quick overview of everything.

**Sections:**

| Section | Description |
|---------|-------------|
| **Greeting** | Time-of-day adaptive greeting with today's date |
| **Today's Timetable** | Teal banner showing periods and subjects for the current day, with a "Take Attendance" button |
| **Elective Banner** | Amber shortcut with an arrow that opens elective attendance |
| **Quick Stats** | Two cards: Students count and Periods Today count (clickable) |
| **Overview Card** | Total students and records, plus a "View attendance records →" shortcut |
| **Recent Sessions** | Last 5 regular attendance sessions with present/absent counts |
| **Recent Electives** | Last 5 elective attendance sessions |
| **Quick Note Icon** | Header shortcut that opens the note composer; visible only when Quick Notes is enabled |

**Session Detail Popup:**
- Tap any session to view full details
- Shows present, absent, and other students
- Actions: Copy to clipboard, Delete, Export as PDF
- PDF export offers Open or Share options

---

### 3. Take Attendance

A 2-step wizard for taking roll call.

**Step 1 - Session Setup:**

| Field | Description |
|-------|-------------|
| Date | Defaults to today, changeable via date picker |
| Period | Auto-populated from timetable, or P1-P6 if no timetable |
| Subject | Auto-filled from timetable, editable |

> For elective mode: only Date and Subject are required.

**Step 2 - Roll Call:**

| Element | Description |
|---------|-------------|
| Count Bar | Live Present/Absent/Other counts |
| All Present | One-tap to mark everyone present |
| Student List | Scrollable checkboxes with status indicators |
| Status Cycle | Tap to cycle: Absent > Medical Leave > On Duty > Late > Absent |

**Color coding:**
- Green checkbox = Present
- Red checkbox = Absent
- Blue badge = Medical Leave
- Purple badge = On Duty
- Orange badge = Late

**Bottom Actions:**
| Button | Action |
|--------|--------|
| Text | Share attendance summary via text intent |
| PDF | Generate and share/view PDF |
| Save | Commit attendance to database |

---

### 4. Elective Setup

Manage elective subjects and their student enrollments.

**Features:**
- Create new electives (e.g., "Data Science", "AI & ML")
- View existing electives as cards with student counts
- Enroll/unenroll students via checkbox list with search
- Select All / None buttons for quick enrollment
- View recent attendance history per elective
- Delete electives (removes enrollment and attendance data)
- "Take Attendance" button navigates to attendance wizard in elective mode

**Enrollment Counter:**
- Shows "X/Y enrolled" where X = enrolled students, Y = total students

---

### 5. Student List

Full class roster with search functionality.

| Feature | Description |
|---------|-------------|
| Header | Shows "Class Roster" with total student count |
| Search | Toggle search by name or RRN (case-insensitive) |
| Student Cards | Display name and RRN for each student |
| Tap | Navigate to Student Details screen |

> Empty state message when no students are enrolled.

---

### 6. Student Details

Detailed attendance metrics for an individual student.

**Displayed Information:**
| Metric | Description |
|--------|-------------|
| Student Name | Full name (headline) |
| RRN | Registration/Roll Number |
| Phone | Phone number (if available) |
| Total Periods | Total attendance periods recorded |
| Present | Count (green) |
| Absent | Count (red) |
| Medical Leave | Count (blue) |
| On Duty | Count (purple) |
| Late | Count (orange) |

---

### 7. Timetable

Weekly class schedule management.

**View Mode:**
- Tab bar for Monday through Friday
- Lists 6 periods with subject names (or "Free")

**Edit Mode:**
- Toggle via edit icon in the top bar
- Text fields for each period (P1-P6)
- Leave blank for free periods
- Save / Cancel buttons

**PDF Timetable:**
- Upload a PDF of the official timetable
- View, share/download, or delete the saved PDF
- Uses FileProvider for secure sharing

---

### 8. Reports

Export attendance data in multiple formats.

**Export Formats:**
| Format | Extension | Library |
|--------|-----------|---------|
| PDF | `.pdf` | Android PdfDocument API |
| Excel | `.xlsx` | Apache POI |
| CSV | `.csv` | OpenCSV |

**Report Data Shown:**
- College, Department, Course, Semester, Section
- Total students, Total records, Generated timestamp

**Report Contents (per student):**
| Column | Description |
|--------|-------------|
| S.No. | Auto-numbered row |
| Student ID | RRN |
| Name | Student name |
| Present | Total present count |
| Absent | Total absent count |
| Attendance % | Calculated percentage |

> After generation, the file is auto-shared via the system share sheet.

---

### 9. Analytics

Visual attendance performance metrics.

**Overall Performance:**
- Animated circular progress chart
- Shows average class attendance percentage
- Color-coded: green above threshold, red below
- Displays target threshold and total records

**Subject Comparison:**
- Vertical bar chart showing attendance % per subject
- Top 6 subjects displayed
- Animated bars with percentage labels

> Charts are custom-drawn using Compose Canvas with smooth animations.

---

### 10 Settings

App configuration and profile management.

**Sections:**

| Section | Options |
|---------|---------|
| **Appearance** | System Default / Light / Dark theme |
| **College Profile** | Edit college name, department, course, semester, section |
| **Features** | **Quick Notes** toggle — enable/disable the Home note shortcut and Google Keep handoff |
| **App Widgets** | Refresh the Quick Roll widget and pin it to the home screen |
| **Database Stats** | Students count, attendance records, threshold % |
| **Export Roster** | Export All (full roster PDF), Export Elective (elective roster PDF) |

**Danger Zone (Hidden):**
- Tap "Database Stats" label 7 times to reveal
- Shows countdown toast on taps 4-6
- Contains "Clear All Data & Start Fresh" button
- Confirmation required before deletion
- Clears all students, timetable, attendance, electives, and resets setup state

---

### 11. Records

The Records screen provides a complete browseable history of all attendance sessions (regular and elective).

**Features:**
- Browse the entire attendance history, grouped by date (newest first) and session.
- Filter chips: All / Regular / Elective with **scope-aware counts**:
  - All → `total records` + `sessions`
  - Regular → `regular records` + `sessions`
  - Elective → `elective records` + `sessions`
- Header search icon expands a compact search field (auto-focused) that matches student name, RRN, subject, elective name, or date.
- Expand a session card to view every student and their status.
- **Copy icon** beside the session delete button copies a text report (subject, date, period/elective, and Present/Absent/Other lists with names and RRNs).
- **Present/Absent checkbox** on each expanded student row, mirroring the Roll Call screen:
  - Checked (green) = Present
  - Unchecked (red) = Absent
  - Updates the saved record immediately and refreshes the widget.
- Delete an individual student's attendance record (confirmation required).
- Delete an entire attendance session (confirmation required).
- Delete all records with scope: Regular only, Elective only, or Both. Students, timetable, college profile, and elective enrollments are kept.
- The home-screen widget refreshes after any deletion.

**UX notes:**
- Dates stored as `yyyy-MM-dd` are displayed as `EEE, d MMM yyyy`.
- Records are grouped by date, newest first, then sessions sorted by period (regular) or elective name (elective).
- A 1 dp divider separates each date group beneath its date header.
- Session keys use `date + period + subject` (regular) or `date + electiveName + subject` (elective).
- All destructive actions show a confirmation dialog with Cancel and a red Delete confirm button.

---

### 12. Quick Notes (Google Keep)

Quick Notes is an optional fast note composer that hands text to the Google Keep app.

**Enable / disable:**
- Go to **Settings → Features → Quick Notes**.
- The toggle is **off by default**; its state is saved in DataStore.
- When off, the Home note icon is hidden. Nothing else in the app changes.

**Composer (opened from the Home header note icon):**

| Element | Description |
|---------|-------------|
| Title | Optional single-line note title |
| Note | Multi-line body, auto-focused, keyboard-aware |
| Save to Keep | Opens Google Keep with the note pre-filled (`ACTION_SEND`, `text/plain`) |
| Open Keep | Launches the Google Keep app; falls back to its Play Store listing |

**Handoff order for Save to Keep:**
1. Google Keep (`com.google.android.keep`) if installed.
2. System share sheet if another app can handle `text/plain`.
3. Clipboard, with a toast, if no handler exists.

**Limitations:**
- RollDesk does **not** store notes locally — Keep is the storage.
- Google Keep has no public notes API, so RollDesk cannot browse, read, or sync Keep notes.
- The manifest declares package visibility (`<queries>`) for Google Keep and `text/plain` SEND intents so detection works on Android 11+.

---

## Attendance System

### Status Types

| Status | Label | Color | Hex |
|--------|-------|-------|-----|
| Present | P | Green | `#2E7D32` |
| Absent | A | Red | `#D32F2F` |
| Medical Leave | ML | Blue | `#1565C0` |
| On Duty | OD | Purple | `#6A1B9A` |
| Late | L | Orange | `#EF6C00` |

### Regular vs Elective Attendance

| Aspect | Regular | Elective |
|--------|---------|----------|
| Student Pool | All active students | Enrolled students only |
| Setup | Date + Period + Subject | Date + Subject |
| Period | Required (from timetable) | N/A |
| Data Table | `attendance_records` | `elective_attendance_records` |
| Enrollment | All students auto-included | Manual enrollment required |

---

## Data Models

### Database Tables (6)

#### `college_config`
| Column | Type | Description |
|--------|------|-------------|
| id | Int (PK) | Always 1 (singleton) |
| collegeName | String | College name |
| department | String | Department |
| course | String | Course name |
| semester | String | Semester |
| section | String | Section |

#### `students`
| Column | Type | Description |
|--------|------|-------------|
| rrn | String (PK) | Registration/Roll Number |
| name | String | Student full name |
| phone | String? | Phone number (optional) |
| notes | String? | Additional notes (optional) |
| isActive | Boolean | Active status (default: true) |

#### `timetable`
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK, auto) | Auto-generated ID |
| dayOfWeek | Int | 1=Monday through 7=Sunday |
| period | Int | 1-6 |
| subjectName | String | Subject name |

#### `attendance_records`
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK, auto) | Auto-generated ID |
| studentRrn | String | Student reference |
| date | String | Format: yyyy-MM-dd |
| period | Int | 1-6 |
| subject | String | Subject name |
| status | String | Present/Absent/Medical Leave/On Duty/Late |
| timestamp | Long | Epoch milliseconds |

#### `elective_students`
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK, auto) | Auto-generated ID |
| electiveName | String | Elective name |
| studentRrn | String | Student reference (indexed) |

#### `elective_attendance_records`
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK, auto) | Auto-generated ID |
| studentRrn | String | Student reference (indexed) |
| electiveName | String | Elective name (indexed) |
| date | String | Format: yyyy-MM-dd |
| subject | String | Subject name |
| status | String | Present/Absent/Medical Leave/On Duty/Late |
| timestamp | Long | Epoch milliseconds |

### DataStore Preferences

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `setup_completed` | Boolean | false | First-run wizard state |
| `attendance_threshold` | Int | 75 | Target attendance % |
| `periods_per_day` | Int | 6 | Periods per day (6, 7, or 8) |
| `timetable_pdf_path` | String? | null | Uploaded timetable PDF path |
| `theme_mode` | Int | 0 | 0=System, 1=Light, 2=Dark |
| `notes_enabled` | Boolean | false | Quick Notes / Google Keep feature toggle |

---

## Export Formats

### PDF

- Multi-page auto-paginated document
- Teal accent header band
- Title, metadata, and timestamp
- Auto-repeated table headers on each page
- Auto-numbered rows (S.No. column)
- Column auto-distribution
- Row divider lines

### Excel (.xlsx)

- Apache POI workbook creation
- Header row + data rows
- Auto-generated file name with timestamp

### CSV

- Standard comma-separated values
- Header row + data rows
- OpenCSV library

---

## Backup & Restore

### Export Backup
- Exports all data as JSON:
  - College configuration
  - Student roster
  - Timetable entries
  - Attendance records

### Import Backup
- Imports from JSON string
- **Clears all existing data before import**
- Restores college config, students, timetable, and attendance

> Note: Elective data is not included in backup/restore.

---

## Integrations & Permissions

| Integration | Direction | Notes |
|-------------|-----------|-------|
| Google Keep | Outbound share | Optional Quick Notes handoff via `ACTION_SEND` (`text/plain`). No data is read from Keep. |
| Home Screen Widget | System | Quick Roll widget receives `APPWIDGET_UPDATE`, `DATE_CHANGED`, `TIME_SET`, and `TIMEZONE_CHANGED`. |
| FileProvider | Internal | Securely shares generated PDFs with viewer/share apps. |
| Runtime permissions | None | The app requires no camera, storage, location, or internet permission. |

---

## Tech Stack

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.3.20 | Programming language |
| Jetpack Compose BOM | 2026.03.01 | UI framework |
| Material Design 3 | via BOM | Design system |
| Room | 2.7.0-alpha08 | SQLite database |
| Dagger Hilt | 2.59.2 | Dependency injection |
| Navigation3 | 1.0.1 | Screen routing |
| DataStore | 1.1.1 | Preferences storage |
| OpenCSV | 5.7.1 | CSV file handling |
| Apache POI | 5.2.3 | Excel file generation |
| Desugar JDK Libs | 2.1.4 | Java API compatibility |

---

## Project Structure

```
app/src/main/java/com/example/crattendance/
│
├── CRAttendanceApp.kt              # @HiltAndroidApp Application class
├── MainActivity.kt                  # @AndroidEntryPoint single activity
├── Navigation.kt                    # NavDisplay + composable routing
├── NavigationKeys.kt               # 9 sealed NavKey route definitions
│
├── data/
│   ├── AppPreferences.kt           # DataStore wrapper (6 preferences)
│   ├── DataRepository.kt           # @Singleton repository (all DB access)
│   └── database/
│       ├── AppDatabase.kt          # Room DB v2 (6 tables, 1 migration)
│       ├── Daos.kt                 # 6 DAO interfaces
│       └── Entities.kt             # 6 entity data classes
│
├── di/
│   └── DatabaseModule.kt           # Hilt @Module providing DB + DAOs
│
├── theme/
│   ├── AttendanceStatus.kt         # Enum with 5 statuses + colors
│   ├── Color.kt                    # Primary (teal), accent (amber), semantic
│   ├── Motion.kt                   # Shared motion/animation tokens
│   ├── Theme.kt                    # Light/Dark Material3 theme
│   └── Type.kt                     # Custom typography (12 styles)
│
├── ui/
│   ├── components/
│   │   ├── BottomNavBar.kt         # 4-tab navigation (Home/Attendance/Students/Records)
│   │   ├── DashboardWidgets.kt     # MetricCard, Charts, ActionChip
│   │   └── Motion.kt               # Press feedback + motion helpers
│   ├── navigation/
│   │   └── NavTransitions.kt       # Enter/pop/predictive transitions
│   ├── main/
│   │   └── CRAttendanceViewModel.kt # Shared @HiltViewModel
│   └── screens/
│       ├── DashboardScreen.kt       # Home overview
│       ├── TakeAttendanceScreen.kt  # Attendance wizard
│       ├── ElectiveSetupScreen.kt   # Elective management
│       ├── StudentListScreen.kt     # Roster with search
│       ├── StudentDetailsScreen.kt  # Per-student metrics
│       ├── TimetableScreen.kt       # Schedule editor + PDF
│       ├── RecordsScreen.kt         # Attendance records history
│       ├── NotesSheet.kt            # Quick note composer → Google Keep
│       ├── ReportsScreen.kt         # Export center (PDF/Excel/CSV)
│       ├── AnalyticsScreen.kt       # Charts and analytics
│       ├── SettingsScreen.kt        # Configuration + danger zone
│       └── SetupWizardScreen.kt     # First-run wizard
│
└── utils/
    ├── BackupHelper.kt             # JSON backup/export + CSV parsing
    └── ExportHelper.kt             # PDF/Excel/CSV file generation
```

---

## Navigation

### Routes

| Route | Parameters | Screen |
|-------|------------|--------|
| `SetupWizard` | None | First-run setup |
| `Dashboard` | None | Home screen |
| `TakeAttendance` | None | Regular attendance |
| `ElectiveSetup` | None | Elective management |
| `ElectiveAttendance` | `electiveName: String` | Elective roll call |
| `StudentList` | None | Class roster |
| `StudentDetails` | `rrn: String` | Student detail view |
| `TimetableScreen` | None | Schedule editor |
| `SettingsScreen` | None | App settings |
| `Records` | None | Attendance Records history |
| `Reports` | None | Export center |
| `Analytics` | None | Attendance analytics |
| `EditAttendance` | `date: String`, `period: Int` | Edit a regular session |
| `EditElectiveAttendance` | `date: String`, `electiveName: String`, `subject: String` | Edit an elective session |

### Bottom Navigation

| Tab | Icon | Routes |
|-----|------|--------|
| Home | House | Dashboard |
| Attendance | Check Circle | TakeAttendance |
| Students | People | StudentList |
| Records | History | Records |

---

<div align="center">

**RollDesk** - Built with Kotlin + Jetpack Compose

</div>
