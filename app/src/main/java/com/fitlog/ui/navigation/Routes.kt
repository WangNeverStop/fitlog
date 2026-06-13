package com.fitlog.ui.navigation

/** Navigation route keys. */
object Routes {
    const val PROFILE_SELECT = "profile_select"

    // Bottom-navigation tabs (hidden during the training flow).
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val RECORDS = "records"   // 训练记录（取代动作库 Tab）
    const val MINE = "mine"

    const val WEIGHT = "weight"

    // Exercise library: now a drill-in reached from 我的 (bottom bar hidden).
    const val EXERCISE_LIBRARY = "exercise_library"
    const val EXERCISE_DETAIL = "exercise_detail/{exerciseId}"
    const val EXERCISE_DETAIL_ARG = "exerciseId"
    fun exerciseDetailRoute(id: Long): String = "exercise_detail/$id"

    // Workout record detail.
    const val WORKOUT_DETAIL = "workout_detail/{workoutId}"
    const val WORKOUT_DETAIL_ARG = "workoutId"
    fun workoutDetailRoute(id: Long): String = "workout_detail/$id"
    const val HISTORICAL_WORKOUT = "historical_workout"

    // Training flow. The shared TrainingSessionViewModel carries the chosen target and
    // actions across these destinations, so no nav arguments are needed.
    const val TRAINING = "training"             // 训练目标选择
    const val REUSE_CONFIRM = "reuse_confirm"   // 复用上次训练确认
    const val ACTION_SELECT = "action_select"   // 动作选择
    const val SESSION = "session"              // 正式训练记录
    const val SUMMARY = "summary"              // 训练完成总结
}
