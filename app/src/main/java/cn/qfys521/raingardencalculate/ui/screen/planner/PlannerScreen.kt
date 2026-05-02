package cn.qfys521.raingardencalculate.ui.screen.planner

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingToolbar
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cn.qfys521.raingardencalculate.calc.PlannerEngine
import cn.qfys521.raingardencalculate.calc.PotSimulator
import cn.qfys521.raingardencalculate.model.Crop
import cn.qfys521.raingardencalculate.model.CropType
import cn.qfys521.raingardencalculate.model.FlowerPot
import cn.qfys521.raingardencalculate.model.LevelUpgrade
import cn.qfys521.raingardencalculate.model.Plan
import cn.qfys521.raingardencalculate.model.PotAction
import cn.qfys521.raingardencalculate.model.Song
import cn.qfys521.raingardencalculate.ui.component.MiuixFilterChip
import cn.qfys521.raingardencalculate.ui.component.MiuixTimePickerDialog
import cn.qfys521.raingardencalculate.ui.theme.BodyMedium
import cn.qfys521.raingardencalculate.ui.theme.BodySmall
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var state by remember { mutableStateOf(loadPlannerState(prefs)) }
    var plan by remember { mutableStateOf<Plan?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showBaseTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(state) {
        savePlannerState(prefs, state)
    }

    val needs = computeNeeds(state)
    val effectivePotCount = when (val g = state.goal) {
        is GoalConfig.LevelGoal -> FlowerPot.POTS.count { it.unlockLevel <= g.targetLevel }.coerceAtLeast(1)
        is GoalConfig.SongGoal -> FlowerPot.POTS.count { it.unlockLevel <= g.currentLevel }.coerceAtLeast(1)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
        ) {
            TopAppBar(title = "花园规划器", largeTitle = "花园规划器")

            SmallTitle(text = "规划模式")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.defaultColors()
            ) {
                BasicComponent(
                    title = "选择规划类型",
                    summary = if (state.goal is GoalConfig.LevelGoal) "等级规划" else "解锁歌曲",
                    bottomAction = {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MiuixFilterChip(
                                selected = state.goal is GoalConfig.LevelGoal,
                                onClick = {
                                    state = state.copy(goal = GoalConfig.LevelGoal(1, 2), inventory = CropType.entries.associateWith { 0 })
                                    plan = null
                                }
                            ) { Text("等级规划") }
                            MiuixFilterChip(
                                selected = state.goal is GoalConfig.SongGoal,
                                onClick = {
                                    state = state.copy(goal = GoalConfig.SongGoal(1, 0), inventory = CropType.entries.associateWith { 0 })
                                    plan = null
                                }
                            ) { Text("解锁歌曲") }
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = state.goal is GoalConfig.LevelGoal,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val goal = state.goal as? GoalConfig.LevelGoal ?: return@AnimatedVisibility
                Column {
                    SmallTitle(text = "等级目标")
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.defaultColors()
                    ) {
                        LevelDropdownRow("当前等级", goal.currentLevel, 1..21) {
                            state = state.copy(goal = goal.copy(currentLevel = it, targetLevel = maxOf(goal.targetLevel, it + 1)))
                            plan = null
                        }
                        HorizontalDivider()
                        LevelDropdownRow("目标等级", goal.targetLevel, (goal.currentLevel + 1)..22) {
                            state = state.copy(goal = goal.copy(targetLevel = it))
                            plan = null
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.goal is GoalConfig.SongGoal,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val goal = state.goal as? GoalConfig.SongGoal ?: return@AnimatedVisibility
                Column {
                    SmallTitle(text = "曲目目标")
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.defaultColors()
                    ) {
                        LevelDropdownRow("当前等级", goal.currentLevel, 1..22) {
                            state = state.copy(goal = goal.copy(currentLevel = it))
                            plan = null
                        }
                        HorizontalDivider()
                        SongDropdownRow(goal.songIndex) {
                            state = state.copy(goal = goal.copy(songIndex = it))
                            plan = null
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = needs.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    SmallTitle(text = "需求预览")
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.defaultColors()
                    ) {
                        needs.entries.forEachIndexed { index, (type, amount) ->
                            if (index > 0) HorizontalDivider()
                            val existing = state.inventory[type] ?: 0
                            val net = (amount - existing).coerceAtLeast(0)
                            BasicComponent(
                                title = type.displayName,
                                summary = if (existing > 0) "需要 $amount | 已有 $existing | 还需 $net" else "需要 $amount"
                            )
                        }
                    }
                }
            }

            SmallTitle(text = "上号配置")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.defaultColors()
            ) {
                BasicComponent(
                    title = "登录模式",
                    summary = state.loginMode.label,
                    bottomAction = {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LoginMode.entries.forEach { mode ->
                                MiuixFilterChip(
                                    selected = state.loginMode == mode,
                                    onClick = { state = state.copy(loginMode = mode); plan = null }
                                ) { Text(mode.label) }
                            }
                        }
                    }
                )
                HorizontalDivider()
                val baseH = state.baseTimeHours.toLong()
                val baseM = ((state.baseTimeHours - baseH) * 60).toLong()
                BasicComponent(
                    title = "基础时间",
                    summary = String.format("%02d:%02d", baseH, baseM),
                    onClick = { showBaseTimePicker = true },
                    bottomAction = {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.0 to "0:00", 6.0 to "6:00", 8.0 to "8:00", 12.0 to "12:00").forEach { (value, label) ->
                                MiuixFilterChip(
                                    selected = state.baseTimeHours == value,
                                    onClick = { state = state.copy(baseTimeHours = value); plan = null }
                                ) { Text(label) }
                            }
                        }
                    }
                )
                HorizontalDivider()

                when (state.loginMode) {
                    LoginMode.INTERVAL -> {
                        BasicComponent(
                            title = "登录间隔",
                            summary = formatHours(state.intervalHours),
                            bottomAction = {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(2.0 to "2小时", 4.0 to "4小时", 8.0 to "8小时", 12.0 to "12小时", 24.0 to "24小时").forEach { (value, label) ->
                                        MiuixFilterChip(
                                            selected = state.intervalHours == value,
                                            onClick = { state = state.copy(intervalHours = value); plan = null }
                                        ) { Text(label) }
                                    }
                                }
                            }
                        )
                    }
                    LoginMode.CUSTOM -> {
                        BasicComponent(title = "自定义登录", summary = "点击时间可修改，首次上号为基准时间")
                        state.customTimes.forEachIndexed { index, timeHours ->
                            HorizontalDivider()
                            val h = timeHours.toLong()
                            val m = ((timeHours - h) * 60).toLong()
                            val label = if (index == 0) "首次上号" else "第${index + 1}次"
                            BasicComponent(
                                title = label,
                                summary = "${String.format("%02d:%02d", h, m)} (T+${formatHours(timeHours)})",
                                onClick = { editingTimeIndex = index; showTimePicker = true },
                                endActions = {
                                    if (state.customTimes.size > 1) {
                                        IconButton(onClick = {
                                            state = state.copy(customTimes = state.customTimes.toMutableList().apply { removeAt(index) })
                                            plan = null
                                        }) { Icon(Icons.Default.Close, "删除", modifier = Modifier.width(16.dp)) }
                                    }
                                }
                            )
                        }
                        HorizontalDivider()
                        BasicComponent(
                            title = "添加登录时间",
                            onClick = { editingTimeIndex = -1; showTimePicker = true },
                            endActions = { Icon(Icons.Default.Add, null, modifier = Modifier.width(16.dp)) }
                        )
                    }
                    LoginMode.CROP_READY -> {
                        BasicComponent(title = "菜熟就上", summary = "根据作物生长时间自动生成登录计划，作物一熟就上号收割。")
                    }
                }

                if (state.loginMode != LoginMode.INTERVAL) {
                    HorizontalDivider()
                    BasicComponent(
                        title = "重复天数",
                        summary = if (state.untilGoal) "直达目标" else "${state.repeatDays}天",
                        bottomAction = {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 3, 7).forEach { days ->
                                    MiuixFilterChip(
                                        selected = state.repeatDays == days && !state.untilGoal,
                                        onClick = { state = state.copy(repeatDays = days, untilGoal = false); plan = null }
                                    ) { Text("${days}天") }
                                }
                                MiuixFilterChip(
                                    selected = state.untilGoal,
                                    onClick = {
                                        state = if (state.untilGoal) state.copy(untilGoal = false, repeatDays = 1)
                                        else state.copy(untilGoal = true, repeatDays = 1)
                                        plan = null
                                    }
                                ) { Text("直达目标") }
                            }
                        }
                    )
                }
            }

            SmallTitle(text = "花盆状态")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.defaultColors()
            ) {
                BasicComponent(title = "花盆数量", summary = "$effectivePotCount 个 (根据等级自动计算)")
                FlowerPot.POTS.forEach { pot ->
                    HorizontalDivider()
                    val unlocked = when (val g = state.goal) {
                        is GoalConfig.LevelGoal -> pot.unlockLevel <= g.targetLevel
                        is GoalConfig.SongGoal -> pot.unlockLevel <= g.currentLevel
                    }
                    val costText = if (pot.materialCosts.isNotEmpty()) " (${pot.materialCosts.joinToString(", ") { "${it.type.displayName}*${it.amount}" }})" else ""
                    BasicComponent(
                        title = pot.name,
                        summary = if (unlocked) "已解锁$costText" else "Lv.${pot.unlockLevel}解锁$costText",
                        titleColor = BasicComponentDefaults.titleColor(
                            color = if (unlocked) MiuixTheme.colorScheme.onBackground else MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    )
                }
            }

            SmallTitle(text = "已有库存")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.defaultColors()
            ) {
                CropType.entries.forEachIndexed { index, type ->
                    if (index > 0) HorizontalDivider()
                    BasicComponent(
                        title = type.displayName,
                        summary = "${state.inventory[type] ?: 0}",
                        endActions = {
                            TextField(
                                value = (state.inventory[type] ?: 0).toString(),
                                onValueChange = { value ->
                                    val num = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                                    state = state.copy(inventory = state.inventory.toMutableMap().apply { put(type, num) })
                                    plan = null
                                },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Button(
                    onClick = {
                        val loginTimes = buildLoginTimes(state.loginMode, state.intervalHours, state.customTimes, state.effectiveRepeatDays, state.baseTimeHours)
                        plan = when (val g = state.goal) {
                            is GoalConfig.LevelGoal -> PlannerEngine.computePlanPerLevel(g.currentLevel, g.targetLevel, state.inventory, loginTimes)
                            is GoalConfig.SongGoal -> {
                                val song = Song.SONGS.getOrNull(g.songIndex)
                                val materialNeeds = song?.materialCosts?.associate { it.type to it.amount } ?: emptyMap()
                                PlannerEngine.computePlan(materialNeeds, state.inventory, effectivePotCount, loginTimes, g.currentLevel, g.currentLevel, false)
                            }
                        }
                        state = state.copy(currentPage = 0, searchQuery = "", filter = ScheduleFilter.ALL)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = needs.isNotEmpty() && (state.loginMode != LoginMode.CUSTOM || state.customTimes.size >= 2),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) { Text("生成规划方案") }
            }

            AnimatedVisibility(visible = plan == null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.defaultColors()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 12.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Text(
                            "配置参数后点击生成",
                            style = BodyMedium,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }

            plan?.let { p ->
                PlanResults(
                    plan = p,
                    isLevelMode = state.goal is GoalConfig.LevelGoal,
                    currentLevel = when (val g = state.goal) { is GoalConfig.LevelGoal -> g.currentLevel; is GoalConfig.SongGoal -> g.currentLevel },
                    targetLevel = when (val g = state.goal) { is GoalConfig.LevelGoal -> g.targetLevel; is GoalConfig.SongGoal -> g.currentLevel },
                    scheduleSearchQuery = state.searchQuery,
                    scheduleFilter = state.filter,
                    pageSize = state.pageSize,
                    currentPage = state.currentPage,
                    onSearchChange = { state = state.copy(searchQuery = it, currentPage = 0) },
                    onFilterChange = { state = state.copy(filter = it, currentPage = 0) },
                    onPageSizeChange = { state = state.copy(pageSize = it, currentPage = 0) },
                    onPageChange = { state = state.copy(currentPage = it) },
                    customPageSizeText = state.customPageSizeText,
                    onCustomPageSizeTextChange = { state = state.copy(customPageSizeText = it) }
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        plan?.let { p ->
            AnimatedVisibility(
                visible = true,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                FloatingToolbar {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { scrollState.animateScrollTo(0) } }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "回到顶部")
                        }
                        IconButton(onClick = { LAST_SHARE_PLAN = p; sharePlannerScreenshot(context) }) {
                            Icon(Icons.Default.Share, contentDescription = "分享截图")
                        }
                        IconButton(onClick = { sharePlanJson(context, p) }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "导出JSON")
                        }
                    }
                }
            }
        }
    }

    MiuixTimePickerDialog(
        show = showTimePicker,
        initialHour = if (editingTimeIndex >= 0 && editingTimeIndex < state.customTimes.size) state.customTimes[editingTimeIndex].toLong().toInt() else 8,
        initialMinute = if (editingTimeIndex >= 0 && editingTimeIndex < state.customTimes.size) ((state.customTimes[editingTimeIndex] - state.customTimes[editingTimeIndex].toLong()) * 60).toLong().toInt() else 0,
        onDismiss = { showTimePicker = false },
        onConfirm = { hour, minute ->
            val hoursFromStart = hour + minute / 60.0
            state = if (editingTimeIndex >= 0 && editingTimeIndex < state.customTimes.size) {
                state.copy(customTimes = state.customTimes.toMutableList().apply { set(editingTimeIndex, hoursFromStart) }.distinct().sorted())
            } else {
                state.copy(customTimes = (state.customTimes + hoursFromStart).distinct().sorted())
            }
            showTimePicker = false
            plan = null
        }
    )

    MiuixTimePickerDialog(
        show = showBaseTimePicker,
        initialHour = state.baseTimeHours.toLong().toInt(),
        initialMinute = ((state.baseTimeHours - state.baseTimeHours.toLong()) * 60).toLong().toInt(),
        onDismiss = { showBaseTimePicker = false },
        onConfirm = { hour, minute ->
            state = state.copy(baseTimeHours = hour + minute / 60.0)
            showBaseTimePicker = false
            plan = null
        }
    )
}


@Composable
private fun LevelDropdownRow(label: String, value: Int, range: IntRange, onSelect: (Int) -> Unit) {
    val options = range.map { "Lv.$it" }
    val selectedIndex = value - range.first
    WindowDropdownPreference(
        title = label,
        items = options,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { onSelect(range.first + it) }
    )
}

@Composable
private fun SongDropdownRow(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val songNames = Song.SONGS.map { it.name }
    WindowDropdownPreference(
        title = "选择曲目",
        items = songNames,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelect
    )
}


private fun computeNeeds(state: PlannerState): Map<CropType, Int> {
    return when (val goal = state.goal) {
        is GoalConfig.LevelGoal -> {
            val targetCosts = LevelUpgrade.UPGRADES.find { it.level == goal.targetLevel }?.cumulativeCosts ?: emptyList()
            val currentCosts = if (goal.currentLevel <= 1) emptyList()
            else LevelUpgrade.UPGRADES.find { it.level == goal.currentLevel }?.cumulativeCosts ?: emptyList()
            val currentMap = currentCosts.associate { it.type to it.amount }
            val levelNeeds = targetCosts.associate { it.type to it.amount }
                .mapValues { (type, amount) -> amount - (currentMap[type] ?: 0) }
            val potCosts = PlannerEngine.potUnlockCosts(goal.currentLevel, goal.targetLevel)
            val potMap = potCosts.groupBy { it.type }.mapValues { (_, costs) -> costs.sumOf { it.amount } }
            (levelNeeds.keys + potMap.keys).associate { type ->
                type to ((levelNeeds[type] ?: 0) + (potMap[type] ?: 0))
            }.filter { it.value > 0 }
        }
        is GoalConfig.SongGoal -> {
            Song.SONGS.getOrNull(goal.songIndex)?.materialCosts?.associate { it.type to it.amount } ?: emptyMap()
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanResults(
    plan: Plan,
    isLevelMode: Boolean,
    currentLevel: Int,
    targetLevel: Int,
    scheduleSearchQuery: String,
    scheduleFilter: ScheduleFilter,
    pageSize: Int,
    currentPage: Int,
    onSearchChange: (String) -> Unit,
    onFilterChange: (ScheduleFilter) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    onPageChange: (Int) -> Unit,
    customPageSizeText: String,
    onCustomPageSizeTextChange: (String) -> Unit
) {
    if (plan.isAlreadySatisfied) {
        SmallTitle(text = "材料已充足")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.defaultColors()) {
            Column {
                BasicComponent(
                    title = "您已有足够的材料来完成目标，无需额外种植。",
                    titleColor = BasicComponentDefaults.titleColor(color = MiuixTheme.colorScheme.primary)
                )
                plan.materialNeeds.forEach { (type, amount) ->
                    HorizontalDivider()
                    val existing = plan.existingInventory[type] ?: 0
                    BasicComponent(title = type.displayName, summary = "需要${amount}, 已有$existing")
                }
            }
        }
    }

    if (plan.loginSchedule.isEmpty() && !plan.isAlreadySatisfied) {
        SmallTitle(text = "无法完成规划")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.defaultColors()) {
            BasicComponent(
                title = "在设定的登录时间内无法生产足够的材料",
                summary = "请增加登录次数或调整间隔",
                titleColor = BasicComponentDefaults.titleColor(color = MiuixTheme.colorScheme.error)
            )
        }
    }

    SmallTitle(text = "方案概览")
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.defaultColors()) {
        Column {
            if (isLevelMode) {
                BasicComponent(title = "目标", summary = "Lv.$currentLevel → Lv.$targetLevel")
                HorizontalDivider()
            }
            BasicComponent(title = "花盆数", summary = "${plan.potCount}个")
            HorizontalDivider()
            val intervalLabel = if (plan.loginIntervalHours <= 0.0) "动态(菜熟/可浇水就上)" else formatHours(plan.loginIntervalHours)
            BasicComponent(title = "上号间隔", summary = intervalLabel)
            HorizontalDivider()
            BasicComponent(title = "总耗时", summary = formatHours(plan.totalTimeHours))
            HorizontalDivider()
            BasicComponent(title = "登录次数", summary = "${plan.totalLoginCount}次")
        }
    }

    if (plan.netNeeds.isNotEmpty()) {
        SmallTitle(text = "材料总览")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.defaultColors()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val allTypes = (plan.materialNeeds.keys + plan.productionSummary.keys + plan.levelUpSpending.keys + plan.potUnlockSpending.keys).toSet()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("类型", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("需要", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f))
                    Text("已有", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f))
                    Text("产出", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f))
                    Text("升级", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f))
                    Text("花盆", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                allTypes.forEach { type ->
                    val needed = plan.materialNeeds[type] ?: 0
                    val existing = plan.existingInventory[type] ?: 0
                    val produced = plan.productionSummary[type] ?: 0
                    val lvCost = plan.levelUpSpending[type] ?: 0
                    val potCost = plan.potUnlockSpending[type] ?: 0
                    if (needed == 0 && produced == 0 && lvCost == 0 && potCost == 0) return@forEach
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(type.displayName, modifier = Modifier.weight(1f))
                        Text("$needed", modifier = Modifier.weight(0.7f))
                        Text("$existing", modifier = Modifier.weight(0.7f))
                        Text("$produced", modifier = Modifier.weight(0.7f))
                        Text(if (lvCost > 0) "-$lvCost" else "-", modifier = Modifier.weight(0.7f), color = if (lvCost > 0) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        Text(if (potCost > 0) "-$potCost" else "-", modifier = Modifier.weight(0.7f), color = if (potCost > 0) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
            }
        }
    }

    if (plan.steps.isNotEmpty()) {
        SmallTitle(text = "种植计划")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.defaultColors()) {
            Column {
                plan.steps.forEachIndexed { index, step ->
                    if (index > 0) HorizontalDivider()
                    val summaryText = buildString {
                        append("x${step.batchCount} (${formatHours(step.cycleTimeHours)})")
                        if (step.crop.materialCosts.isNotEmpty()) {
                            append("\n耗材: ${step.crop.materialCosts.joinToString(", ") { "${it.type.displayName}*${it.amount}" }}")
                        }
                    }
                    BasicComponent(title = "${index + 1}. ${step.crop.name}", summary = summaryText)
                }
            }
        }
    }

    if (plan.cropProgression.isNotEmpty()) {
        SmallTitle(text = "各级作物使用明细")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.defaultColors()) {
            Column {
                val groupedByLevel = plan.cropProgression.groupBy { it.level }
                var isFirst = true
                groupedByLevel.toSortedMap().forEach { (level, usages) ->
                    if (!isFirst) HorizontalDivider()
                    isFirst = false
                    BasicComponent(
                        title = "Lv.$level",
                        titleColor = BasicComponentDefaults.titleColor(color = MiuixTheme.colorScheme.primary)
                    )
                    usages.forEach { usage ->
                        HorizontalDivider()
                        val typeStr = usage.cropTypes.joinToString("/") { it.displayName }
                        BasicComponent(
                            title = "${usage.cropName} ($typeStr)",
                            summary = "种植${usage.planted} 收获${usage.harvested}",
                            insideMargin = PaddingValues(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (plan.loginSchedule.isNotEmpty()) {
        SmallTitle(text = "登录时间表")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.defaultColors()) {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = scheduleSearchQuery,
                    onValueChange = onSearchChange,
                    label = "搜索作物名称",
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScheduleFilter.entries.forEach { filter ->
                        MiuixFilterChip(selected = scheduleFilter == filter, onClick = { onFilterChange(filter) }) {
                            Text(filter.label)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("每页: ", style = BodySmall)
                    listOf(5, 10, 20, 50).forEach { size ->
                        MiuixFilterChip(selected = pageSize == size, onClick = { onPageSizeChange(size) }, modifier = Modifier.padding(horizontal = 2.dp)) {
                            Text("$size")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("自定义分页: ", style = BodySmall)
                    TextField(
                        value = customPageSizeText,
                        onValueChange = { v ->
                            val digits = v.filter { it.isDigit() }.take(4)
                            onCustomPageSizeTextChange(digits)
                            val size = digits.toIntOrNull()
                            if (size != null && size > 0) onPageSizeChange(size.coerceAtMost(5000))
                        },
                        modifier = Modifier.width(120.dp),
                        singleLine = true,
                        label = "自定义",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                val filteredSchedule = plan.loginSchedule.filter { event ->
                    val hasAction = event.potActions.any { it.action != PotAction.IDLE || it.note.isNotEmpty() }
                    if (!hasAction) return@filter false
                    val matchesSearch = scheduleSearchQuery.isEmpty() || event.potActions.any { action ->
                        action.crop?.name?.contains(scheduleSearchQuery, ignoreCase = true) == true || action.note.contains(scheduleSearchQuery, ignoreCase = true)
                    }
                    val matchesFilter = when (scheduleFilter) {
                        ScheduleFilter.ALL -> true
                        ScheduleFilter.PLANTING -> event.potActions.any { it.action == PotAction.PLANT || it.action == PotAction.PLANT_AND_HARVEST }
                        ScheduleFilter.HARVEST -> event.potActions.any { it.action == PotAction.HARVEST }
                        ScheduleFilter.LEVEL_UP -> event.potActions.any { it.note.contains("升级") || it.note.contains("花盆") }
                        ScheduleFilter.NO_WATER -> event.potActions.any { it.action != PotAction.IDLE || it.note.isNotEmpty() }
                    }
                    matchesSearch && matchesFilter
                }

                val totalPages = maxOf(1, (filteredSchedule.size + pageSize - 1) / pageSize)
                val safePage = currentPage.coerceIn(0, totalPages - 1)
                val paginatedSchedule = filteredSchedule.drop(safePage * pageSize).take(pageSize)

                Text("共 ${filteredSchedule.size} 条记录", style = BodySmall, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Spacer(modifier = Modifier.height(4.dp))

                paginatedSchedule.forEach { event ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        val dayNum = (event.timeHours / 24).toInt() + 1
                        val minutesInDay = ((event.timeHours % 24) * 60).toInt().coerceAtLeast(0)
                        val hh = String.format("%02d", minutesInDay / 60)
                        val mm = String.format("%02d", minutesInDay % 60)
                        Text(
                            "第${event.loginIndex + 1}次  Day$dayNum $hh:$mm",
                            fontWeight = FontWeight.Bold,
                            style = BodyMedium
                        )

                        val systemActions = event.potActions.filter { it.action == PotAction.IDLE && it.note.isNotEmpty() }
                        systemActions.forEach { pot ->
                            Text("  ${pot.note}", style = BodySmall.copy(fontWeight = FontWeight.SemiBold), color = MiuixTheme.colorScheme.primary)
                        }

                        val potGroups = event.potActions
                            .filter { it.action != PotAction.IDLE || it.note.isEmpty() }
                            .groupBy { it.potIndex }
                            .toSortedMap()
                        potGroups.forEach { (potIndex, actions) ->
                            val parts = actions.mapNotNull { a ->
                                when (a.action) {
                                    PotAction.PLANT -> "种${a.crop?.name ?: ""}"
                                    PotAction.HARVEST -> "收${a.crop?.name ?: ""}"
                                    PotAction.WATER -> "浇水"
                                    PotAction.PLANT_AND_HARVEST -> "种收${a.crop?.name ?: ""}"
                                    PotAction.IDLE -> null
                                }
                            }
                            if (parts.isNotEmpty()) {
                                val label = if (potIndex >= 0) "花盆${potIndex + 1}" else "操作"
                                Text("  $label: ${parts.joinToString(" | ")}", style = BodySmall, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            }
                        }

                        if (event.inventory.isNotEmpty()) {
                            val invText = event.inventory.entries.filter { it.value > 0 }.joinToString("  ") { "${it.key.displayName}:${it.value}" }
                            if (invText.isNotEmpty()) {
                                Text("  库存: $invText", style = BodySmall, color = MiuixTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    HorizontalDivider()
                }

                if (totalPages > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(text = "上一页", onClick = { onPageChange(safePage - 1) }, enabled = safePage > 0, colors = ButtonDefaults.textButtonColorsPrimary())
                        Text("${safePage + 1} / $totalPages", modifier = Modifier.padding(horizontal = 16.dp), style = BodyMedium)
                        TextButton(text = "下一页", onClick = { onPageChange(safePage + 1) }, enabled = safePage < totalPages - 1, colors = ButtonDefaults.textButtonColorsPrimary())
                    }
                }
            }
        }
    }
}


private fun buildLoginTimes(loginMode: LoginMode, loginInterval: Double, customLoginTimes: List<Double>, effectiveRepeatDays: Int, baseTime: Double = 0.0): List<Double> {
    return when (loginMode) {
        LoginMode.INTERVAL -> PotSimulator.generateFixedLoginTimes(loginInterval).map { it + baseTime }
        LoginMode.CUSTOM -> {
            val baseTimes = customLoginTimes.sorted()
            val days = effectiveRepeatDays.coerceAtLeast(1000)
            (0 until days).flatMap { day -> baseTimes.map { it + day * 24.0 + baseTime } }
        }
        LoginMode.CROP_READY -> listOf(baseTime, -(effectiveRepeatDays * 24.0))
    }
}

private fun formatHours(hours: Double): String {
    if (hours < 0) return "即时"
    val h = hours.toLong()
    val days = h / 24
    val remainingHours = h % 24
    val minutes = ((hours - h) * 60).toLong()
    return buildString {
        if (days > 0) append("${days}d")
        if (remainingHours > 0) append("${remainingHours}h")
        if (minutes > 0 && days == 0L) append("${minutes}m")
        if (isEmpty()) append("0h")
    }
}

private fun formatClockTime(hours: Double): String {
    val totalMinutes = kotlin.math.floor(hours * 60.0).toInt().coerceAtLeast(0)
    val day = totalMinutes / (24 * 60)
    val minutesInDay = totalMinutes % (24 * 60)
    val h = minutesInDay / 60
    val m = minutesInDay % 60
    return "D$day ${String.format("%02d:%02d", h, m)}"
}

private fun sharePlannerScreenshot(context: Context) {
    try {
        val bitmap = renderPlanLongBitmap(context)
        val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val imageFile = File(sharedDir, "plan_long_share.png")
        FileOutputStream(imageFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        val imageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "露晓卉庭规划长截图")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享长截图"))
    } catch (_: IOException) {
        Toast.makeText(context, "截图分享失败", Toast.LENGTH_SHORT).show()
    } catch (_: IllegalArgumentException) {
        Toast.makeText(context, "截图分享失败", Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, "截图分享失败", Toast.LENGTH_SHORT).show()
    }
}

private fun renderPlanLongBitmap(context: Context): Bitmap {
    val plan = LAST_SHARE_PLAN ?: throw IOException("No plan to render")

    val colorBg = android.graphics.Color.parseColor("#F5F5F5")
    val colorCard = android.graphics.Color.WHITE
    val colorPrimary = android.graphics.Color.parseColor("#1A73E8")
    val colorOnPrimary = android.graphics.Color.WHITE
    val colorText = android.graphics.Color.parseColor("#1A1A1A")
    val colorTextSecondary = android.graphics.Color.parseColor("#666666")
    val colorAccent = android.graphics.Color.parseColor("#34A853")
    val colorWarn = android.graphics.Color.parseColor("#EA4335")
    val colorDivider = android.graphics.Color.parseColor("#E0E0E0")

    val width = 1080
    val pad = 40
    val cardRadius = 24f
    val cardPad = 32
    val cardLeft = pad.toFloat()
    val cardRight = (width - pad).toFloat()
    val contentW = cardRight - cardLeft - cardPad * 2
    val rowH = 48f
    val cardGap = 24f

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorOnPrimary; textSize = 48f; isFakeBoldText = true }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorOnPrimary; textSize = 28f; alpha = 200 }
    val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorPrimary; textSize = 32f; isFakeBoldText = true }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorText; textSize = 28f }
    val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorText; textSize = 28f; isFakeBoldText = true }
    val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorTextSecondary; textSize = 24f }
    val statValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorPrimary; textSize = 40f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
    val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorTextSecondary; textSize = 22f; textAlign = Paint.Align.CENTER }
    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorAccent; textSize = 26f; isFakeBoldText = true }
    val warnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorWarn; textSize = 26f }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorCard; isAntiAlias = true }
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorBg }
    val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorDivider; strokeWidth = 1.5f }
    val primaryBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorPrimary }

    val materialTypes = (plan.materialNeeds.keys + plan.productionSummary.keys + plan.levelUpSpending.keys + plan.potUnlockSpending.keys).toSet()
    val scheduleEvents = plan.loginSchedule.filter { event ->
        event.potActions.any { it.action != PotAction.IDLE || it.note.isNotEmpty() }
    }
    val showSchedule = scheduleEvents

    val headerH = 160f + pad
    val statsH = 120f
    var totalH = pad + headerH + cardGap + statsH + cardGap

    val matRows = materialTypes.count { type ->
        val n = plan.materialNeeds[type] ?: 0; val e = plan.existingInventory[type] ?: 0
        val p = plan.productionSummary[type] ?: 0; val l = plan.levelUpSpending[type] ?: 0
        val pt = plan.potUnlockSpending[type] ?: 0
        n != 0 || p != 0 || l != 0 || pt != 0
    }
    if (matRows > 0) totalH += (72 + cardPad + rowH * (matRows + 1) + cardPad) + cardGap

    val stepCount = plan.steps.size
    val stepExtraLines = plan.steps.count { it.crop.materialCosts.isNotEmpty() }
    if (stepCount > 0) totalH += (72 + cardPad + rowH * (stepCount + stepExtraLines) + cardPad) + cardGap

    if (showSchedule.isNotEmpty()) {
        var schedRows = 0
        showSchedule.forEach { event ->
            schedRows += 1 // header
            schedRows += event.potActions.count { it.action == PotAction.IDLE && it.note.isNotEmpty() }
            schedRows += event.potActions.filter { it.action != PotAction.IDLE || it.note.isEmpty() }.groupBy { it.potIndex }.size
        }
        totalH += (72 + cardPad + rowH * schedRows + cardPad) + cardGap
    }

    totalH += pad + 48f // footer

    val bitmap = Bitmap.createBitmap(width, totalH.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawRect(0f, 0f, width.toFloat(), totalH, bgPaint)

    var y = 0f

    canvas.drawRect(0f, 0f, width.toFloat(), 160f + pad, primaryBgPaint)
    y += pad
    canvas.drawText("露晓卉庭规划", pad.toFloat(), y + 56f, titlePaint)
    canvas.drawText("Rain Garden Calculate", pad.toFloat(), y + 92f, subtitlePaint)
    val goalText = if (plan.materialNeeds.isNotEmpty()) {
        "目标材料: ${plan.materialNeeds.keys.joinToString("·") { it.displayName }}"
    } else "材料已充足"
    canvas.drawText(goalText, pad.toFloat(), y + 128f, subtitlePaint)
    y += 160f + cardGap

    drawCard(canvas, cardLeft, y, cardRight, y + statsH, cardRadius, cardPaint)
    val stats = listOf(
        "花盆" to "${plan.potCount}个",
        "间隔" to if (plan.loginIntervalHours <= 0) "动态" else formatHours(plan.loginIntervalHours),
        "耗时" to formatHours(plan.totalTimeHours),
        "登录" to "${plan.totalLoginCount}次"
    )
    val statW = contentW / stats.size
    stats.forEachIndexed { i, (label, value) ->
        val cx = cardLeft + cardPad + statW * i + statW / 2
        canvas.drawText(value, cx, y + 52f, statValuePaint)
        canvas.drawText(label, cx, y + 88f, statLabelPaint)
        if (i < stats.size - 1) {
            val dx = cardLeft + cardPad + statW * (i + 1)
            canvas.drawLine(dx, y + 24f, dx, y + statsH - 24f, dividerPaint)
        }
    }
    y += statsH + cardGap

    if (matRows > 0) {
        val cardH = 72 + cardPad + rowH * (matRows + 1) + cardPad
        drawCard(canvas, cardLeft, y, cardRight, y + cardH, cardRadius, cardPaint)
        canvas.drawText("材料总览", cardLeft + cardPad, y + 44f, sectionPaint)
        var ry = y + 72f
        val colW = floatArrayOf(contentW * 0.22f, contentW * 0.16f, contentW * 0.16f, contentW * 0.16f, contentW * 0.15f, contentW * 0.15f)
        val colX = FloatArray(6)
        var cx = cardLeft + cardPad; for (i in colW.indices) { colX[i] = cx; cx += colW[i] }
        listOf("类型", "需要", "已有", "产出", "升级", "花盆").forEachIndexed { i, h -> canvas.drawText(h, colX[i], ry, bodyBoldPaint) }
        ry += rowH * 0.6f
        canvas.drawLine(cardLeft + cardPad, ry, cardRight - cardPad, ry, dividerPaint)
        ry += rowH * 0.4f
        materialTypes.forEach { type ->
            val needed = plan.materialNeeds[type] ?: 0
            val existing = plan.existingInventory[type] ?: 0
            val produced = plan.productionSummary[type] ?: 0
            val lvCost = plan.levelUpSpending[type] ?: 0
            val potCost = plan.potUnlockSpending[type] ?: 0
            if (needed == 0 && produced == 0 && lvCost == 0 && potCost == 0) return@forEach
            canvas.drawText(type.displayName, colX[0], ry, bodyPaint)
            canvas.drawText("$needed", colX[1], ry, bodyPaint)
            canvas.drawText("$existing", colX[2], ry, bodyPaint)
            canvas.drawText("$produced", colX[3], ry, accentPaint)
            canvas.drawText(if (lvCost > 0) "-$lvCost" else "-", colX[4], ry, if (lvCost > 0) warnPaint else smallPaint)
            canvas.drawText(if (potCost > 0) "-$potCost" else "-", colX[5], ry, if (potCost > 0) warnPaint else smallPaint)
            ry += rowH
        }
        y += cardH + cardGap
    }

    if (stepCount > 0) {
        val cardH = 72 + cardPad + rowH * (stepCount + stepExtraLines) + cardPad
        drawCard(canvas, cardLeft, y, cardRight, y + cardH, cardRadius, cardPaint)
        canvas.drawText("种植计划", cardLeft + cardPad, y + 44f, sectionPaint)
        var ry = y + 72f
        plan.steps.forEachIndexed { idx, step ->
            val stepText = "${idx + 1}. ${step.crop.name}"
            val detailText = "  x${step.batchCount}  ${formatHours(step.cycleTimeHours)}"
            canvas.drawText(stepText, cardLeft + cardPad, ry, bodyBoldPaint)
            canvas.drawText(detailText, cardLeft + cardPad + bodyBoldPaint.measureText(stepText), ry, smallPaint)
            ry += rowH
            if (step.crop.materialCosts.isNotEmpty()) {
                val costStr = "耗材: ${step.crop.materialCosts.joinToString(" ") { "${it.type.displayName}*${it.amount}" }}"
                canvas.drawText(costStr, cardLeft + cardPad, ry, smallPaint)
                ry += rowH
            }
        }
        y += cardH + cardGap
    }

    if (showSchedule.isNotEmpty()) {
        var totalRows = 0
        showSchedule.forEach { event ->
            totalRows += 1 // header
            val systemActions = event.potActions.filter { it.action == PotAction.IDLE && it.note.isNotEmpty() }
            totalRows += systemActions.size
            val potGroups = event.potActions.filter { it.action != PotAction.IDLE || it.note.isEmpty() }.groupBy { it.potIndex }
            totalRows += potGroups.size
        }
        val cardH = 72 + cardPad + rowH * totalRows + cardPad
        drawCard(canvas, cardLeft, y, cardRight, y + cardH, cardRadius, cardPaint)
        canvas.drawText("登录时间表", cardLeft + cardPad, y + 44f, sectionPaint)
        var ry = y + 72f
        showSchedule.forEach { event ->
            val dayNum = (event.timeHours / 24).toInt() + 1
            val minutesInDay = ((event.timeHours % 24) * 60).toInt().coerceAtLeast(0)
            val hh = String.format("%02d", minutesInDay / 60)
            val mm = String.format("%02d", minutesInDay % 60)
            canvas.drawText("第${event.loginIndex + 1}次  Day$dayNum $hh:$mm", cardLeft + cardPad, ry, bodyBoldPaint)
            ry += rowH

            event.potActions.filter { it.action == PotAction.IDLE && it.note.isNotEmpty() }.forEach { pot ->
                canvas.drawText(pot.note, cardLeft + cardPad + 16f, ry, accentPaint)
                ry += rowH
            }

            event.potActions
                .filter { it.action != PotAction.IDLE || it.note.isEmpty() }
                .groupBy { it.potIndex }
                .toSortedMap()
                .forEach { (potIndex, actions) ->
                    val parts = actions.mapNotNull { a ->
                        when (a.action) {
                            PotAction.PLANT -> "种${a.crop?.name ?: ""}"
                            PotAction.HARVEST -> "收${a.crop?.name ?: ""}"
                            PotAction.WATER -> "浇水"
                            PotAction.PLANT_AND_HARVEST -> "种收${a.crop?.name ?: ""}"
                            PotAction.IDLE -> null
                        }
                    }
                    if (parts.isNotEmpty()) {
                        val label = if (potIndex >= 0) "花盆${potIndex + 1}" else "操作"
                        val text = "$label: ${parts.joinToString(" | ")}"
                        canvas.drawText(text, cardLeft + cardPad + 16f, ry, smallPaint)
                        ry += rowH
                    }
                }
        }
        y += cardH + cardGap
    }

    y += 16f
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorTextSecondary; textSize = 22f; textAlign = Paint.Align.CENTER }
    canvas.drawText("露晓卉庭计算器 · Rain Garden Calculate", width / 2f, y + 22f, footerPaint)

    return bitmap
}

private fun drawCard(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, radius: Float, paint: Paint) {
    canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
}

private var LAST_SHARE_PLAN: Plan? = null

private fun sharePlanJson(context: Context, plan: Plan) {
    try {
        val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val jsonFile = File(sharedDir, "plan_export.json")
        val root = JSONObject().apply {
            put("totalTimeHours", plan.totalTimeHours)
            put("totalLoginCount", plan.totalLoginCount)
            put("potCount", plan.potCount)
            put("loginIntervalHours", plan.loginIntervalHours)
            put("materialNeeds", mapToJson(plan.materialNeeds.mapKeys { it.key.name }))
            put("existingInventory", mapToJson(plan.existingInventory.mapKeys { it.key.name }))
            put("productionSummary", mapToJson(plan.productionSummary.mapKeys { it.key.name }))
            put("steps", JSONArray().apply {
                plan.steps.forEach { step ->
                    put(JSONObject().apply {
                        put("crop", step.crop.name)
                        put("batchCount", step.batchCount)
                        put("wateringCount", step.wateringCount)
                        put("cycleTimeHours", step.cycleTimeHours)
                        put("totalYield", step.totalYield)
                    })
                }
            })
            put("loginSchedule", JSONArray().apply {
                plan.loginSchedule.forEach { event ->
                    put(JSONObject().apply {
                        put("index", event.loginIndex)
                        put("timeHours", event.timeHours)
                        put("level", event.level)
                        put("actions", JSONArray().apply {
                            event.potActions.forEach { action ->
                                put(JSONObject().apply {
                                    put("potIndex", action.potIndex)
                                    put("action", action.action.name)
                                    put("crop", action.crop?.name ?: "")
                                    put("batchCount", action.batchCount)
                                    put("note", action.note)
                                })
                            }
                        })
                    })
                }
            })
        }
        jsonFile.writeText(root.toString(2), Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", jsonFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "露晓卉庭规划JSON导出")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "导出JSON"))
    } catch (_: IOException) {
        Toast.makeText(context, "JSON导出失败", Toast.LENGTH_SHORT).show()
    } catch (_: IllegalArgumentException) {
        Toast.makeText(context, "JSON导出失败", Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, "JSON导出失败", Toast.LENGTH_SHORT).show()
    }
}

private fun mapToJson(map: Map<String, Int>): JSONObject {
    val obj = JSONObject()
    map.forEach { (k, v) -> obj.put(k, v) }
    return obj
}
