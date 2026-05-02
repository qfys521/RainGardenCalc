package cn.qfys521.milthmgardencalc.ui.screen.info

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.qfys521.milthmgardencalc.calc.GardenCalculator
import cn.qfys521.milthmgardencalc.calc.HarvestProbability
import cn.qfys521.milthmgardencalc.model.Crop
import cn.qfys521.milthmgardencalc.model.CropType
import cn.qfys521.milthmgardencalc.model.LevelUpgrade
import cn.qfys521.milthmgardencalc.model.Song
import cn.qfys521.milthmgardencalc.ui.component.CropCard
import cn.qfys521.milthmgardencalc.ui.component.MaterialCostChips
import cn.qfys521.milthmgardencalc.ui.component.MiuixFilterChip
import cn.qfys521.milthmgardencalc.ui.theme.BodyLarge
import cn.qfys521.milthmgardencalc.ui.theme.LabelMedium
import cn.qfys521.milthmgardencalc.ui.theme.TitleMedium
import cn.qfys521.milthmgardencalc.ui.theme.TitleSmall
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class InfoTab(val label: String) {
    CROPS("作物"),
    LEVELS("升级"),
    SONGS("歌曲"),
    HARVEST("丰收")
}

@Composable
fun InfoScreen(onCropClick: (Int) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = "信息")

        TabRow(
            tabs = InfoTab.entries.map { it.label },
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        when (InfoTab.entries[selectedTab]) {
            InfoTab.CROPS -> CropsTab(onCropClick)
            InfoTab.LEVELS -> LevelsTab()
            InfoTab.SONGS -> SongsTab()
            InfoTab.HARVEST -> HarvestTab()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CropsTab(onCropClick: (Int) -> Unit) {
    var wateringCount by remember { mutableIntStateOf(0) }
    var sortMode by remember { mutableStateOf(CropSortMode.INCOME) }

    val sortedCrops = remember(sortMode, wateringCount) {
        when (sortMode) {
            CropSortMode.INCOME -> Crop.CROPS.sortedByDescending {
                GardenCalculator.netIncomePerHour(it, wateringCount)
            }
            CropSortMode.GROWTH_TIME -> Crop.CROPS.sortedBy { it.growthTimeHours }
            CropSortMode.YIELD -> Crop.CROPS.sortedByDescending { it.baseYield }
            CropSortMode.UNLOCK_LEVEL -> Crop.CROPS.sortedBy { it.unlockLevel ?: 0 }
        }
    }

    Column {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("浇水次数: $wateringCount", style = BodyLarge)
            }
            Slider(
                value = wateringCount.toFloat(),
                onValueChange = { wateringCount = it.toInt() },
                valueRange = 0f..3f,
                steps = 2
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CropSortMode.entries.forEach { mode ->
                    MiuixFilterChip(
                        selected = sortMode == mode,
                        onClick = { sortMode = mode }
                    ) {
                        Text(mode.label)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            itemsIndexed(sortedCrops) { _, crop ->
                val originalIndex = Crop.CROPS.indexOf(crop)
                CropCard(crop = crop, wateringCount = wateringCount, onClick = { onCropClick(originalIndex) })
            }
        }
    }
}

@Composable
private fun LevelsTab() {
    var targetLevel by remember { mutableFloatStateOf(22f) }
    val targetLevelInt = targetLevel.toInt()

    val cumulativeCosts = remember(targetLevelInt) {
        val costs = mutableMapOf<CropType, Int>()
        LevelUpgrade.UPGRADES.filter { it.level <= targetLevelInt }.forEach { upgrade ->
            upgrade.materialCosts.forEach { cost ->
                costs[cost.type] = (costs[cost.type] ?: 0) + cost.amount
            }
        }
        costs
    }

    Column {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("目标等级: $targetLevelInt")
            }
            Slider(
                value = targetLevel,
                onValueChange = { targetLevel = it },
                valueRange = 2f..22f,
                steps = 19
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("升级至 ${targetLevelInt} 级总计", style = TitleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    cumulativeCosts.forEach { (type, amount) ->
                        Text("${type.displayName}: $amount")
                    }
                    if (cumulativeCosts.isEmpty()) Text("无需耗材")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            items(LevelUpgrade.UPGRADES) { upgrade ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Lv.${upgrade.level}", style = TitleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("本级消耗:", style = LabelMedium)
                        MaterialCostChips(upgrade.materialCosts)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("累计消耗:", style = LabelMedium)
                        MaterialCostChips(upgrade.cumulativeCosts)
                    }
                }
            }
        }
    }
}

@Composable
private fun SongsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
        items(Song.SONGS) { song ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = song.name, style = TitleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("解锁耗材:", style = LabelMedium)
                    MaterialCostChips(song.materialCosts)
                }
            }
        }
    }
}

@Composable
private fun HarvestTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("说明", style = TitleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("每次收割时有小概率使产量增加:")
                Text("  丰收: 产量 ×2 (增加100%)")
                Text("  大丰收: 产量 ×4 (增加300%)")
                Spacer(modifier = Modifier.height(4.dp))
                Text("浇水可以提高丰收概率，最多浇水3次。")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("概率表", style = TitleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("浇水", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("普通", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("丰收", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("大丰收", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                for (w in 0..3) {
                    val outcomes = HarvestProbability.outcomes(w)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${w}次", modifier = Modifier.weight(1f))
                        outcomes.forEach { outcome ->
                            Text(String.format("%.1f%%", outcome.probability * 100), modifier = Modifier.weight(1f))
                        }
                    }
                    if (w < 3) HorizontalDivider()
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("期望收益乘数", style = TitleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                for (w in 0..3) {
                    val multiplier = HarvestProbability.expectedYieldMultiplier(w)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("浇水${w}次")
                        Text(String.format("×%.3f", multiplier), fontWeight = FontWeight.SemiBold, color = MiuixTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

private enum class CropSortMode(val label: String) {
    INCOME("收益/时间"),
    GROWTH_TIME("生长时间"),
    YIELD("基础收成"),
    UNLOCK_LEVEL("解锁等级")
}
