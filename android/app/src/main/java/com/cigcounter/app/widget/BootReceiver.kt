package com.cigcounter.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for system boot completion to re-arm both the midnight reset worker
 * and the 9:00 PM evening summary worker, plus refreshes the Glance widgets.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Re-arm midnight reset worker
            MidnightResetWorker.schedule(context)

            // Re-arm 9 PM evening summary worker
            EveningSummaryWorker.schedule(context)

            // Update widgets post-boot
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    CigWidgetReceiver.updateAll(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
