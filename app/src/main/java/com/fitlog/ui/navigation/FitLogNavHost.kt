package com.fitlog.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.FitLogApplication
import com.fitlog.ui.muscle.BodyModel
import com.fitlog.ui.muscle.LocalBodyModel
import com.fitlog.ui.muscle.genderToBodyModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.screens.PlaceholderScreen
import com.fitlog.ui.screens.calendar.CalendarScreen
import com.fitlog.ui.screens.home.HomeScreen
import com.fitlog.ui.screens.library.ExerciseDetailScreen
import com.fitlog.ui.screens.library.ExerciseLibraryScreen
import com.fitlog.ui.screens.mine.MineScreen
import com.fitlog.ui.screens.profile.ProfileSelectScreen
import com.fitlog.ui.screens.records.RecordsScreen
import com.fitlog.ui.screens.records.HistoricalWorkoutScreen
import com.fitlog.ui.screens.records.WorkoutDetailScreen
import com.fitlog.ui.screens.weight.WeightScreen
import com.fitlog.ui.screens.training.action.SampleExercises
import com.fitlog.ui.screens.training.TrainingSessionViewModel
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.screens.training.TrainingTargetScreen
import com.fitlog.ui.screens.training.action.ActionSelectScreen
import com.fitlog.ui.screens.training.reuse.ReuseConfirmScreen
import com.fitlog.ui.screens.training.session.SessionScreen
import com.fitlog.ui.screens.training.summary.SampleSummary
import com.fitlog.ui.screens.training.summary.SummaryScreen

private data class BottomTab(val route: String, val label: String, val icon: String)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "首页", "🏠"),
    BottomTab(Routes.CALENDAR, "日历", "📅"),
    BottomTab(Routes.RECORDS, "记录", "📒"),
    BottomTab(Routes.MINE, "我的", "👤"),
)
private val bottomTabRoutes = bottomTabs.map { it.route }.toSet()

/**
 * App navigation. A Scaffold hosts a bottom navigation bar that appears only on the main
 * tabs (首页/日历/动作库/我的) and is hidden on the login and training screens. The training
 * flow shares one activity-scoped [TrainingSessionViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun FitLogNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val trainingVm: TrainingSessionViewModel = viewModel(factory = FitLogViewModelFactory.Factory)

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomTabRoutes

    // Pick male/female muscle map from the current user's saved gender.
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = currentRoute == Routes.HOME) {
        showExitDialog = true
    }
    val container = remember(context) { (context.applicationContext as FitLogApplication).container }
    val bodyModel by produceState(BodyModel.MALE, container) {
        container.userRepository.currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(BodyModel.MALE)
            else container.userPreferences.bodyData(uid).map { genderToBodyModel(it.gender) }
        }.collect { value = it }
    }

    CompositionLocalProvider(LocalBodyModel provides bodyModel) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) FitLogBottomBar(currentRoute, navController)
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PROFILE_SELECT,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.PROFILE_SELECT) {
                ProfileSelectScreen(
                    onUserReady = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PROFILE_SELECT) { inclusive = true }
                        }
                    },
                )
            }

            // --- bottom tabs ---
            composable(Routes.HOME) {
                HomeScreen(
                    onStartTraining = { navController.navigate(Routes.TRAINING) },
                    onViewRecentDetail = { id -> navController.navigate(Routes.workoutDetailRoute(id)) },
                    onOpenWeight = { navController.navigate(Routes.WEIGHT) },
                )
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    onOpenWorkout = { id -> navController.navigate(Routes.workoutDetailRoute(id)) },
                    onOpenWeight = { navController.navigate(Routes.WEIGHT) },
                )
            }
            composable(Routes.RECORDS) {
                RecordsScreen(
                    onOpen = { id -> navController.navigate(Routes.workoutDetailRoute(id)) },
                    onAddHistory = { navController.navigate(Routes.HISTORICAL_WORKOUT) },
                )
            }
            composable(Routes.MINE) {
                val toPicker: () -> Unit = {
                    navController.navigate(Routes.PROFILE_SELECT) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
                MineScreen(
                    onSwitchUser = toPicker,
                    onUserDeleted = toPicker,
                    onOpenLibrary = { navController.navigate(Routes.EXERCISE_LIBRARY) },
                    onOpenWeight = { navController.navigate(Routes.WEIGHT) },
                )
            }
            composable(Routes.WEIGHT) {
                WeightScreen(onBack = { navController.popBackStack() })
            }

            // --- exercise library (now reached from 我的; bottom bar hidden) ---
            composable(Routes.EXERCISE_LIBRARY) {
                ExerciseLibraryScreen(
                    onOpenDetail = { ex -> navController.navigate(Routes.exerciseDetailRoute(ex.id)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.EXERCISE_DETAIL,
                arguments = listOf(navArgument(Routes.EXERCISE_DETAIL_ARG) { type = NavType.LongType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong(Routes.EXERCISE_DETAIL_ARG) ?: -1L
                val exercise = SampleExercises.all.firstOrNull { it.id == id }
                    ?: SampleExercises.all.first()
                ExerciseDetailScreen(
                    exercise = exercise,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.WORKOUT_DETAIL,
                arguments = listOf(navArgument(Routes.WORKOUT_DETAIL_ARG) { type = NavType.LongType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong(Routes.WORKOUT_DETAIL_ARG) ?: -1L
                WorkoutDetailScreen(workoutId = id, onBack = { navController.popBackStack() })
            }
            composable(Routes.HISTORICAL_WORKOUT) {
                HistoricalWorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { workoutId ->
                        navController.navigate(Routes.workoutDetailRoute(workoutId)) {
                            popUpTo(Routes.HISTORICAL_WORKOUT) { inclusive = true }
                        }
                    },
                )
            }

            // --- training flow (bottom bar hidden) ---
            composable(Routes.TRAINING) {
                TrainingTargetScreen(
                    onSelect = { target ->
                        trainingVm.setTarget(target)
                        if (target.isFree) {
                            navController.navigate(Routes.ACTION_SELECT)
                        } else {
                            navController.navigate(Routes.REUSE_CONFIRM)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.REUSE_CONFIRM) {
                ReuseConfirmScreen(
                    vm = trainingVm,
                    onReuse = {
                        navController.navigate(Routes.ACTION_SELECT) {
                            popUpTo(Routes.REUSE_CONFIRM) { inclusive = true }
                        }
                    },
                    onCreateNew = {
                        navController.navigate(Routes.ACTION_SELECT) {
                            popUpTo(Routes.REUSE_CONFIRM) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.ACTION_SELECT) {
                ActionSelectScreen(
                    target = trainingVm.target ?: TrainingTarget.FREE,
                    initial = trainingVm.reuseDraft,
                    onConfirm = { slots ->
                        trainingVm.setSelectedActions(slots)
                        navController.navigate(Routes.SESSION)
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.SESSION) {
                SessionScreen(
                    plan = trainingVm.plan,
                    onFinish = { finished ->
                        trainingVm.finish(finished) { navController.navigate(Routes.SUMMARY) }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.SUMMARY) {
                SummaryScreen(
                    summary = trainingVm.lastSummary ?: SampleSummary.build(),
                    onFinishToHome = {
                        trainingVm.clear()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onBackToTraining = { navController.popBackStack() },
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("退出 App？") },
            text = { Text("确定要退出当前 App 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                ) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
    }
}

@Composable
private fun FitLogBottomBar(currentRoute: String?, navController: NavHostController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        if (tab.route == Routes.HOME) {
                            // Home is the root of the signed-in navigation stack. Returning to it
                            // should pop existing destinations instead of restoring another saved
                            // Home entry, which can conflict after a drill-down page is popped.
                            val returnedHome = navController.popBackStack(
                                route = Routes.HOME,
                                inclusive = false,
                            )
                            if (!returnedHome) {
                                navController.navigate(Routes.HOME) {
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            navController.navigate(tab.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                icon = { Text(tab.icon) },
                label = { Text(tab.label) },
            )
        }
    }
}
