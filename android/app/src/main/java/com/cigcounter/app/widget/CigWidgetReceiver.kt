package com.cigcounter.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CigWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CigWidget()

    companion object {
        /**
         * Refreshes every instance of CigWidget placed on the user's home screen.
         */
        suspend fun updateAll(context: Context) {
            val glanceManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceManager.getGlanceIds(CigWidget::class.java)
            val widget = CigWidget()
            glanceIds.forEach { glanceId ->
                widget.update(context, glanceId)
            }
        }
    }
}
