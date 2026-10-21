# CigCounter 🚬⏱️

A fully offline, privacy-first cigarette counter and habit awareness app for Android with a quick-tap home-screen widget.

**Core Philosophy:** Help people understand their smoking habits and the financial impact through transparent arithmetic. No lectures, no preaching, no guilt trips — just the math.

---

## ✨ Features

- **One-Tap Home Screen Widget:**
  - Big round `+` button on the left logs a cigarette in milliseconds without opening the app.
  - Live counts for **Today**, **Last 7 Days (Week)**, and **Last 30 Days (Month)**.
  - "Could have saved: X units today" calculated in real time.
  - Tapping the statistics area launches the app.
  - Midnight reset automatically resets the "Today" count via WorkManager.
- **Granular Room Database:**
  - Logs **every single cigarette** as an individual record with its exact timestamp.
  - Enables flexible historical trend analysis and immediate single-step undo.
- **Generic Currency ("Units"):**
  - No predetermined currency symbols ($ / € / £).
  - Flexible decimal pricing per cigarette (e.g., `7.5` units).
  - Live input validation with helpful error alerts.
- **History & Trends Bar Chart:**
  - Custom hardware-accelerated bar chart drawn purely with **Compose Canvas** (zero bloated chart libraries).
  - Toggle between 7-day and 30-day views with daily averages and peak metrics.
- **100% Offline & Private (Strict):**
  - **Zero internet permission** (`android.permission.INTERNET` is not even in the manifest).
  - Zero analytics, trackers, ads, or crash reporters.
  - Android cloud backup and device transfer excluded via custom data extraction rules.
  - All data resides strictly inside your phone's encrypted sandbox.

---

## 🛠️ Tech Stack & Compatibility

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Kotlin | `1.9.24` |
| **Android Gradle Plugin** | AGP | `8.4.2` |
| **Compile / Target SDK** | Android 14 (API 34) | `34` |
| **Minimum SDK** | Android 8.0 (Oreo) | `26` |
| **App UI** | Jetpack Compose + Material 3 | Compose BOM `2024.05.00` |
| **Home Screen Widget** | Jetpack Glance | `1.1.0` |
| **Database** | Room (with KSP compiler) | `2.6.1` (KSP `1.9.24-1.0.20`) |
| **Preferences** | Jetpack DataStore Preferences | `1.1.1` |
| **Background Tasks** | WorkManager | `2.9.0` |
| **Date/Time** | `java.time` (ThreeTen/JSR-310) | Native (API 26+) |

---

## 🏗️ How to Build

### Option A: Automatic via GitHub Actions (Zero Setup)
1. Fork or push this repository to GitHub.
2. Go to the **Actions** tab in your GitHub repository.
3. The **"Build CigCounter Android APK"** workflow runs automatically on every push.
4. Once completed (approx. 2-3 minutes), download the compiled debug APK directly under **Artifacts** (`CigCounter-Debug-APK`).

### Option B: Local Command Line
Ensure you have **JDK 17** installed:
```bash
# Verify Java version (must be 17+)
java -version

# Build the Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Generating the Gradle Wrapper (If not present)
If you don't have Android Studio and need to re-generate wrapper binaries:
```bash
# Install gradle on Linux/Mac (e.g. via brew or apt)
# brew install gradle   OR   sudo apt install gradle
gradle wrapper --gradle-version 8.7
```

---

## 📱 How to Install the APK on Your Phone

1. Download the `app-debug.apk` onto your Android phone (via USB cable, Google Drive download, or browser from GitHub releases).
2. Tap the downloaded `.apk` file in your phone's file manager or download notification.
3. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings**.
   - Toggle **Allow from this source** to ON.
   - Return and tap **Install**.
4. Open **CigCounter** from your app drawer.
5. Long-press on your phone's home screen, tap **Widgets**, scroll to **CigCounter**, and drag the widget onto your home screen.

---

## 🔒 Privacy Guarantee

```
No Internet Permission  •  No Ads  •  No Analytics  •  Local Database Only
```
Check `app/src/main/AndroidManifest.xml` to verify: there is no `<uses-permission android:name="android.permission.INTERNET" />`. Even if the app wanted to send data anywhere, the Android OS kernel would strictly reject it.

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
