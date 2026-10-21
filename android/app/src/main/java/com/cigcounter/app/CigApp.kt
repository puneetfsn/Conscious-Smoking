package com.cigcounter.app

import android.app.Application
import com.cigcounter.app.data.AppDatabase
import com.cigcounter.app.data.CigaretteRepository
import com.cigcounter.app.data.UserPreferencesRepository
import com.cigcounter.app.widget.EveningSummaryWorker
import com.cigcounter.app.widget.MidnightResetWorker

class CigApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var repository: CigaretteRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        preferencesRepository = UserPreferencesRepository(this)
        repository = CigaretteRepository(database.cigaretteDao(), preferencesRepository)

        // Schedule the midnight worker to reset Today counter to 0
        MidnightResetWorker.schedule(this)

        // Schedule the 9:00 PM local evening summary notification
        EveningSummaryWorker.schedule(this)
    }
}
