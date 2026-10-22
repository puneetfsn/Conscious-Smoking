# 🚬 SmokeLedger

[![Build Debug APK](https://github.com/puneetfsn/SmokeLedger/actions/workflows/build.yml/badge.svg)](https://github.com/puneetfsn/SmokeLedger/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

An **offline-first, privacy-focused cigarette counter** and habit awareness tracker for Android. 

Unlike other tracking apps, SmokeLedger requires **zero internet permissions**, keeping your personal data completely private and on-device. It visualizes the financial and physical "burn rate" of smoking, turning abstract guilt into tangible motivation.

---

## 📱 Features

- **Home-Screen Widget (Jetpack Glance):** Redesigned, space-efficient Nothing OS styled widget with frictionless one-tap logging and active stats display.
- **Privacy by Design:** `NO_INTERNET` permission in manifest. Absolutely zero trackers, ads, or third-party analytics.
- **Optional Wishlist Goals:** Input custom financial goals (e.g., headphones, trip). The motivation card dynamically hides if no active goal is set.
- **Positive Shift Tasks:** Triggers mindfulness alerts and alternative healthy actions (deep breathing, stretching) every 10 cigarettes to gamify habit replacement.
- **On-Device PDF Export:** Generates an exhaustive "Doctor / Therapist Damage Report" summarizing consumption patterns and financial metrics.
- **Local JSON Backup:** Fully export and import data to local storage for manual backups.

---

## 🛠️ Architecture & Tech Stack

This project implements modern Android development standards:

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Declarative UI)
- **Widget Engine:** Jetpack Glance (App Widgets)
- **Local Database:** Room SQLite with Flow-based reactive queries
- **Data Persistence:** Jetpack DataStore / SharedPreferences
- **Asynchronous Flow:** Kotlin Coroutines & Flows
- **CI/CD:** Automated builds via GitHub Actions producing signed debug APKs

---

## 📥 Installation

You can download the compiled installer directly:

1. Go to the [Releases](https://github.com/puneetfsn/SmokeLedger/releases) page.
2. Download the latest `SmokeLedger-v0.x.x.apk`.
3. Open the APK file on your Android device and allow installation from unknown sources.

---

## 👥 Portfolio Value / Recruiter Highlights

- **Offline-First Data Pipelines:** Built robust Room SQLite flows that combine multiple queries natively in Kotlin backends.
- **Glance Widget API:** Overcame Jetpack Glance constraints regarding intent actions and on-screen redraw latency.
- **CI/CD Automation:** Configured a custom GitHub Actions workflow to build and test the Android package entirely in the cloud.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
