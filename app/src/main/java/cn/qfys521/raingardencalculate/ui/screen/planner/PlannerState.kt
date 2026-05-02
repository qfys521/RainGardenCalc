package cn.qfys521.raingardencalculate.ui.screen.planner

import android.content.SharedPreferences
import cn.qfys521.raingardencalculate.model.CropType
import cn.qfys521.raingardencalculate.model.Song

sealed interface GoalConfig {
    data class LevelGoal(val currentLevel: Int, val targetLevel: Int) : GoalConfig
    data class SongGoal(val currentLevel: Int, val songIndex: Int) : GoalConfig
}

enum class LoginMode(val label: String) {
    INTERVAL("间隔登录"),
    CUSTOM("自定义登录"),
    CROP_READY("菜熟就上")
}

enum class ScheduleFilter(val label: String) {
    ALL("全部"),
    PLANTING("种植"),
    HARVEST("收获"),
    LEVEL_UP("升级"),
    NO_WATER("隐藏浇水")
}

data class PlannerState(
    val goal: GoalConfig = GoalConfig.LevelGoal(1, 2),
    val loginMode: LoginMode = LoginMode.INTERVAL,
    val baseTimeHours: Double = 0.0,
    val intervalHours: Double = 8.0,
    val customTimes: List<Double> = listOf(0.0),
    val repeatDays: Int = 1,
    val untilGoal: Boolean = false,
    val inventory: Map<CropType, Int> = CropType.entries.associateWith { 0 },
    val searchQuery: String = "",
    val filter: ScheduleFilter = ScheduleFilter.ALL,
    val pageSize: Int = 10,
    val currentPage: Int = 0,
    val customPageSizeText: String = "10"
) {
    val effectiveRepeatDays: Int
        get() = if (untilGoal) 36500 else repeatDays.coerceIn(1, 365)
}

internal const val PREFS_NAME = "planner_state"

private const val KEY_GOAL_MODE = "planner_goal_mode"
private const val KEY_LEVEL_CURRENT = "planner_level_current"
private const val KEY_LEVEL_TARGET = "planner_level_target"
private const val KEY_SONG_LEVEL = "planner_song_level"
private const val KEY_SONG_INDEX = "planner_song_index"
private const val KEY_LOGIN_MODE = "planner_login_mode"
private const val KEY_BASE_TIME = "planner_base_time"
private const val KEY_INTERVAL = "planner_interval"
private const val KEY_CUSTOM_TIMES = "planner_custom_times"
private const val KEY_REPEAT_DAYS = "planner_repeat_days"
private const val KEY_UNTIL_GOAL = "planner_until_goal"
private const val KEY_INV_PREFIX = "planner_inv_"

fun loadPlannerState(prefs: SharedPreferences): PlannerState {
    val goalMode = prefs.getInt(KEY_GOAL_MODE, 0)
    val goal = if (goalMode == 0) {
        GoalConfig.LevelGoal(
            currentLevel = prefs.getInt(KEY_LEVEL_CURRENT, 1).coerceIn(1, 21),
            targetLevel = prefs.getInt(KEY_LEVEL_TARGET, 2).coerceIn(2, 22)
        )
    } else {
        GoalConfig.SongGoal(
            currentLevel = prefs.getInt(KEY_SONG_LEVEL, 1).coerceIn(1, 22),
            songIndex = prefs.getInt(KEY_SONG_INDEX, 0).coerceIn(0, Song.SONGS.size - 1)
        )
    }

    val loginMode = when (prefs.getInt(KEY_LOGIN_MODE, 0)) {
        1 -> LoginMode.CUSTOM
        2 -> LoginMode.CROP_READY
        else -> LoginMode.INTERVAL
    }

    val customTimes = readDoubleListPrefs(prefs, KEY_CUSTOM_TIMES, listOf(0.0))
    val repeatDays = prefs.getInt(KEY_REPEAT_DAYS, 1).coerceIn(1, 365)
    val untilGoal = prefs.getBoolean(KEY_UNTIL_GOAL, false)

    val inventory = CropType.entries.associateWith { type ->
        prefs.getInt("$KEY_INV_PREFIX${type.name}", 0).coerceAtLeast(0)
    }

    return PlannerState(
        goal = goal,
        loginMode = loginMode,
        baseTimeHours = prefs.getFloat(KEY_BASE_TIME, 0.0f).toDouble().coerceIn(0.0, 23.5),
        intervalHours = prefs.getFloat(KEY_INTERVAL, 8.0f).toDouble().coerceIn(0.5, 48.0),
        customTimes = customTimes,
        repeatDays = repeatDays,
        untilGoal = untilGoal,
        inventory = inventory
    )
}

fun savePlannerState(prefs: SharedPreferences, state: PlannerState) {
    prefs.edit().apply {
        when (val g = state.goal) {
            is GoalConfig.LevelGoal -> {
                putInt(KEY_GOAL_MODE, 0)
                putInt(KEY_LEVEL_CURRENT, g.currentLevel)
                putInt(KEY_LEVEL_TARGET, g.targetLevel)
            }
            is GoalConfig.SongGoal -> {
                putInt(KEY_GOAL_MODE, 1)
                putInt(KEY_SONG_LEVEL, g.currentLevel)
                putInt(KEY_SONG_INDEX, g.songIndex)
            }
        }
        putInt(KEY_LOGIN_MODE, state.loginMode.ordinal)
        putFloat(KEY_BASE_TIME, state.baseTimeHours.toFloat())
        putFloat(KEY_INTERVAL, state.intervalHours.toFloat())
        putDoubleListPrefs(this, KEY_CUSTOM_TIMES, state.customTimes)
        putInt(KEY_REPEAT_DAYS, state.repeatDays)
        putBoolean(KEY_UNTIL_GOAL, state.untilGoal)
        CropType.entries.forEach { type ->
            putInt("$KEY_INV_PREFIX${type.name}", (state.inventory[type] ?: 0).coerceAtLeast(0))
        }
        apply()
    }
}

private fun readDoubleListPrefs(prefs: SharedPreferences, key: String, default: List<Double>): List<Double> {
    val raw = prefs.getString(key, null) ?: return default
    val parsed = raw.split(",").mapNotNull { it.toDoubleOrNull() }.distinct().sorted()
    return parsed.ifEmpty { default }
}

private fun putDoubleListPrefs(editor: SharedPreferences.Editor, key: String, values: List<Double>) {
    editor.putString(key, values.distinct().sorted().joinToString(","))
}
