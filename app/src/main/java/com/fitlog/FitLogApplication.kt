package com.fitlog

import android.app.Application
import com.fitlog.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point. Holds the manual dependency container so screens and
 * ViewModels can reach repositories without a DI framework (kept simple on purpose
 * for a small personal app).
 */
class FitLogApplication : Application() {

    // Application-scoped coroutine scope for one-off startup work (e.g. seeding).
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Seed the built-in muscle groups / exercises on first launch. Idempotent.
        applicationScope.launch {
            container.trainingRepository.ensureSeeded()
        }
    }
}
