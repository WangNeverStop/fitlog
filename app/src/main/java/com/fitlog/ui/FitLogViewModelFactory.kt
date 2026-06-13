package com.fitlog.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitlog.FitLogApplication
import com.fitlog.ui.screens.calendar.CalendarViewModel
import com.fitlog.ui.screens.home.HomeViewModel
import com.fitlog.ui.screens.mine.MineViewModel
import com.fitlog.ui.screens.profile.ProfileViewModel
import com.fitlog.ui.screens.records.RecordsViewModel
import com.fitlog.ui.screens.training.TrainingSessionViewModel
import com.fitlog.ui.screens.weight.WeightViewModel

/**
 * Central place to construct ViewModels with their repository dependencies pulled
 * from the application container. Add new ViewModels here as screens gain logic.
 */
object FitLogViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = fitLogApplication()
            ProfileViewModel(app.container.userRepository)
        }
        initializer {
            val app = fitLogApplication()
            HomeViewModel(app.container.userRepository, app.container.trainingRepository)
        }
        initializer {
            val app = fitLogApplication()
            TrainingSessionViewModel(app.container.userRepository, app.container.trainingRepository)
        }
        initializer {
            val app = fitLogApplication()
            CalendarViewModel(
                app.container.userRepository,
                app.container.trainingRepository,
                app.container.userPreferences,
            )
        }
        initializer {
            val app = fitLogApplication()
            MineViewModel(app.container.userRepository, app.container.userPreferences)
        }
        initializer {
            val app = fitLogApplication()
            RecordsViewModel(app.container.userRepository, app.container.trainingRepository)
        }
        initializer {
            val app = fitLogApplication()
            WeightViewModel(app.container.userRepository, app.container.trainingRepository, app.container.userPreferences)
        }
    }
}

private fun CreationExtras.fitLogApplication(): FitLogApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FitLogApplication)
