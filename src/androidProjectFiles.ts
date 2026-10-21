export interface ProjectFile {
  path: string;
  category: 'gradle' | 'manifest' | 'kotlin' | 'res' | 'ci' | 'docs';
  description: string;
  content: string;
}

export const ANDROID_PROJECT_FILES: ProjectFile[] = [
  {
    path: "settings.gradle.kts",
    category: "gradle",
    description: "Configures Gradle plugins repository and SmokeLedger root module",
    content: `pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmokeLedger"
include(":app")`
  },
  {
    path: "build.gradle.kts",
    category: "gradle",
    description: "Root Gradle build script registering Android, Kotlin, and KSP plugins",
    content: `// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}`
  },
  {
    path: "gradle.properties",
    category: "gradle",
    description: "JVM arguments and AndroidX flags",
    content: `# Project-wide Gradle settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official`
  },
  {
    path: "gradle/libs.versions.toml",
    category: "gradle",
    description: "Gradle Version Catalog with verified compatible versions",
    content: `[versions]
agp = "8.4.2"
kotlin = "1.9.24"
ksp = "1.9.24-1.0.20"
composeCompiler = "1.5.14"
composeBom = "2024.05.00"
coreKtx = "1.13.1"
lifecycleRuntimeKtx = "2.8.0"
activityCompose = "1.9.0"
navigationCompose = "2.7.7"
room = "2.6.1"
datastore = "1.1.1"
glance = "1.1.0"
workManager = "2.9.0"
coroutines = "1.8.1"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Room (KSP)
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# DataStore Preferences
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Jetpack Glance
androidx-glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
androidx-glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Tests
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`
  },
  {
    path: "app/build.gradle.kts",
    category: "gradle",
    description: "App module build script: SDK 34, Compose, Room KSP, Glance, WorkManager",
    content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cigcounter.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cigcounter.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}`
  },
  {
    path: "app/src/main/AndroidManifest.xml",
    category: "manifest",
    description: "App manifest: Zero internet permission, Evening summary notifications, and Boot receivers",
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Strictly offline: Zero INTERNET permission -->
    <uses-permission android:name="android.intent.action.BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />

    <application
        android:name=".CigApp"
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@drawable/ic_cig_logo"
        android:label="@string/app_name"
        android:roundIcon="@drawable/ic_cig_logo"
        android:supportsRtl="true"
        android:theme="@style/Theme.CigCounter">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.CigCounter">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver
            android:name=".widget.CigWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_info" />
        </receiver>

        <receiver
            android:name=".widget.BootReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

    </application>

</manifest>`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/data/CigaretteLog.kt",
    category: "kotlin",
    description: "Room Entity storing timestamp and isSkipped flag for Ghost Counter (resisted cravings)",
    content: `package com.cigcounter.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cigarette_logs")
data class CigaretteLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isSkipped: Boolean = false
)`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/util/DamageReportGenerator.kt",
    category: "kotlin",
    description: "Generates offline vector PDF clinical report for doctor/therapist consultation",
    content: `package com.cigcounter.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.cigcounter.app.data.CigaretteLog
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DamageReportGenerator {

    fun generatePdfReport(context: Context, logs: List<CigaretteLog>, pricePerUnit: Double, zoneId: ZoneId = ZoneId.systemDefault()): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply { color = Color.BLACK; textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val sectionPaint = Paint().apply { color = Color.rgb(220, 50, 20); textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val bodyPaint = Paint().apply { color = Color.BLACK; textSize = 10f; isAntiAlias = true }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

        var y = 50f
        canvas.drawText("SmokeLedger — Clinical Damage Report", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Generated on: \${LocalDate.now(zoneId)}  •  Confidential Patient Profile", 40f, y, bodyPaint)
        y += 30f

        val thirtyDaysAgo = DateRangeUtils.getMonthStartMillis(zoneId)
        val smokedLogs = logs.filter { !it.isSkipped && it.timestampMillis >= thirtyDaysAgo }
        val totalSmoked = smokedLogs.size
        val totalSpent = totalSmoked * pricePerUnit
        val dailyAvg = totalSmoked / 30.0

        canvas.drawRect(40f, y, 555f, y + 90f, boxPaint)
        canvas.drawText("30-DAY BEHAVIOUR SUMMARY", 55f, y + 22f, sectionPaint)
        canvas.drawText("Total Smoked (30 Days): $totalSmoked cigarettes (\${String.format(\"%.1f\", dailyAvg)}/day)", 55f, y + 42f, bodyPaint)
        canvas.drawText("Total Units Spent: \${CalculationUtils.formatUnits(totalSpent)} units", 55f, y + 60f, bodyPaint)
        canvas.drawText("Ghost Counter: \${logs.count { it.isSkipped && it.timestampMillis >= thirtyDaysAgo }} cravings successfully resisted", 55f, y + 78f, bodyPaint)

        y += 115f
        canvas.drawText("THE MIRROR — 1-YEAR PACE PROJECTION", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("• Projected 1-year consumption: \${String.format(\"%.0f\", dailyAvg * 365)} cigarettes", 45f, y, bodyPaint)
        y += 16f
        canvas.drawText("• Projected financial expenditure: \${CalculationUtils.formatUnits(dailyAvg * 365 * pricePerUnit)} units", 45f, y, bodyPaint)

        pdfDocument.finishPage(page)
        val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val outputFile = File(reportsDir, "SmokeLedger_Damage_Report_\${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out -> pdfDocument.writeTo(out) }
        pdfDocument.close()
        return outputFile
    }
}`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/widget/EveningSummaryWorker.kt",
    category: "kotlin",
    description: "Daily 9 PM offline notification worker comparing today vs yesterday's habit count",
    content: `package com.cigcounter.app.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.cigcounter.app.CigApp
import com.cigcounter.app.R
import com.cigcounter.app.util.CalculationUtils
import com.cigcounter.app.util.DateRangeUtils
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class EveningSummaryWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CigApp
        val settings = app.preferencesRepository.getSettings()

        val startOfToday = DateRangeUtils.getStartOfTodayMillis()
        val startOfYesterday = DateRangeUtils.getDaysAgoStartOfDayMillis(1)

        val todayCount = app.repository.getSmokedCountFrom(startOfToday)
        val yesterdayCount = app.repository.getSmokedCountBetween(startOfYesterday, startOfToday)
        val todaySpent = CalculationUtils.calculateSpent(todayCount, settings.pricePerCigarette)

        val message = when {
            todayCount == 0 -> "Smoke-free day! 0 cigarettes logged. Incredible milestone! 🎉"
            todayCount < yesterdayCount -> "Today: $todayCount cigs (\${CalculationUtils.formatUnits(todaySpent)} u). That's \${yesterdayCount - todayCount} less than yesterday! 👏"
            else -> "Today: $todayCount cigs (\${CalculationUtils.formatUnits(todaySpent)} u). Rest well; tomorrow is a fresh start."
        }

        showNotification(applicationContext, "Evening Habit Summary", message)
        schedule(applicationContext)
        return Result.success()
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "evening_summary_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily 9 PM Summary", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_cig_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(9001, notification)
    }

    companion object {
        fun schedule(context: Context) {
            val now = ZonedDateTime.now()
            var target = now.withHour(21).withMinute(0).withSecond(0).withNano(0)
            if (now.isAfter(target)) target = target.plusDays(1)
            val delay = Duration.between(now, target).toMillis()

            val workRequest = OneTimeWorkRequestBuilder<EveningSummaryWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("smokeledger_evening_summary", ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}`
  },
  {
    path: "app/src/main/java/com/cigcounter/app/widget/CigWidget.kt",
    category: "kotlin",
    description: "Jetpack Glance Nothing OS widget: NDot Matrix Dark & Light Monochrome themes with Ghost button",
    content: `package com.cigcounter.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cigcounter.app.CigApp
import com.cigcounter.app.MainActivity
import com.cigcounter.app.data.HabitSummary
import com.cigcounter.app.util.CalculationUtils

class CigWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as CigApp
        val summary = runCatching { app.repository.getHabitSummarySync() }.getOrDefault(HabitSummary())
        val settings = runCatching { app.preferencesRepository.getSettings() }.getOrDefault(com.cigcounter.app.data.UserSettings())

        provideContent {
            val isWhite = settings.widgetTheme == "NOTHING_WHITE"
            val bg = if (isWhite) ColorProvider(Color(0xFFF4F4F5)) else ColorProvider(Color(0xFF0F0F11))
            val textCol = if (isWhite) ColorProvider(Color(0xFF09090B)) else ColorProvider(Color(0xFFEDEDED))
            val subText = if (isWhite) ColorProvider(Color(0xFF52525B)) else ColorProvider(Color(0xFFA1A1AA))
            val accentRed = ColorProvider(Color(0xFFFF3B30))

            Box(modifier = GlanceModifier.fillMaxSize().background(bg).cornerRadius(22.dp).padding(12.dp)) {
                Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = GlanceModifier.size(50.dp).background(accentRed).cornerRadius(25.dp).clickable(actionRunCallback<LogSmokedActionCallback>()), contentAlignment = Alignment.Center) {
                                Text("+", style = TextStyle(color = ColorProvider(Color.White), fontSize = 32.sp, fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = GlanceModifier.height(5.dp))
                            Box(modifier = GlanceModifier.size(28.dp).background(if (isWhite) ColorProvider(Color(0xFFE4E4E7)) else ColorProvider(Color(0xFF1E1E24))).cornerRadius(14.dp).clickable(actionRunCallback<LogResistedActionCallback>()), contentAlignment = Alignment.Center) {
                                Text("🛡️", style = TextStyle(fontSize = 12.sp))
                            }
                        }
                        Spacer(modifier = GlanceModifier.width(14.dp))
                        Column(modifier = GlanceModifier.defaultWeight().fillMaxSize().clickable(actionStartActivity<MainActivity>())) {
                            Text("Today: \${summary.todayCount} cigs (\${summary.todaySkippedCount} resisted)", style = TextStyle(color = textCol, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text("Week: \${summary.weekCount} cigs · \${CalculationUtils.formatUnits(summary.weekSpentUnits)} u", style = TextStyle(color = subText, fontSize = 11.sp))
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text("Pace: ≈ \${String.format(\"%.0f\", summary.paceYearlyCigs)}/yr · \${CalculationUtils.formatUnits(summary.paceYearlyUnits)} u", style = TextStyle(color = accentRed, fontSize = 11.sp, fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }
        }
    }
}

class LogSmokedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as CigApp
        app.repository.logCigarette()
        CigWidgetReceiver.updateAll(context)
    }
}

class LogResistedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as CigApp
        app.repository.logSkippedCigarette()
        CigWidgetReceiver.updateAll(context)
    }
}`
  },
  {
    path: ".github/workflows/build.yml",
    category: "ci",
    description: "GitHub Actions CI building debug APK on push/pull request and uploading artifact",
    content: `name: Build SmokeLedger Android APK

on:
  push:
    branches: [ "main", "master" ]
  workflow_dispatch:

concurrency:
  group: \${{ github.workflow }}-\${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: 8.7
      - name: Ensure Wrapper Exists
        run: |
          if [ ! -f "gradlew" ]; then
            gradle wrapper --gradle-version 8.7
          fi
          chmod +x gradlew
      - run: ./gradlew testDebugUnitTest --continue
      - run: ./gradlew assembleDebug --stacktrace
      - uses: actions/upload-artifact@v4
        with:
          name: SmokeLedger-Debug-APK
          path: app/build/outputs/apk/debug/*.apk`
  },
  {
    path: "README.md",
    category: "docs",
    description: "Complete README with 9 new features, Nothing OS themes, and privacy documentation",
    content: `# SmokeLedger 🚬⏱️

A fully offline, privacy-first habit awareness app and Nothing OS home-screen widget.

## 🌟 9 Core Features
1. **Ghost Counter (Positive Tracking Mode):** Log every time you resist a craving; tracks total cigarettes avoided and units saved.
2. **Damage Report (Doctor/Therapist PDF):** One-tap generates a clean, printable vector PDF with 30-day totals, pace, and clinical notes.
3. **The Mirror (Pace Projection):** Multiplies 7-day average across 1 year ("At this pace: 14,400 units/year").
4. **Time Since Last Cigarette:** Live ticking timer with physiological recovery milestones.
5. **Edit Past Days:** Easily add forgotten logs to yesterday or 2 days ago without breaking data fidelity.
6. **Wishlist Goal:** "Smoke 2 less per day = Wireless Headphones in 34 days."
7. **Nothing-Style Widget Themes:** Native Matrix Dark (Obsidian) and Nothing Light (Monochrome White) with dot-matrix styling.
8. **Local Evening Summary (No Internet):** Daily 9 PM notification via WorkManager comparing today vs yesterday.
9. **PIN Lock + Local JSON Backup:** 4-digit PIN lock to keep roommates out + zero-cloud JSON export/restore.`
  }
];
