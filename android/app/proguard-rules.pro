# ProGuard and R8 rules for CigCounter

# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Glance rules
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class * implements androidx.glance.appwidget.action.ActionCallback { *; }

# WorkManager rules
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# DataStore & Protobuf
-dontwarn androidx.datastore.**

# Compose rules
-keepattributes *Annotation*
-dontwarn androidx.compose.**
