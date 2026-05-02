package cn.qfys521.raingardencalculate.ui.screen.crop

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
import androidx.compose.foundation.lazy.itemsIndexed
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.qfys521.raingardencalculate.calc.GardenCalculator
import cn.qfys521.raingardencalculate.calc.WateringSimulator
import cn.qfys521.raingardencalculate.model.Crop
import cn.qfys521.raingardencalculate.ui.component.CropCard
import cn.qfys521.raingardencalculate.ui.component.MiuixFilterChip
import cn.qfys521.raingardencalculate.ui.theme.BodyLarge
import cn.qfys521.raingardencalculate.ui.theme.BodySmall
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class SortMode(val label: String) {
    INCOME("收益/时间"),
    GROWTH_TIME("生长时间"),
    YIELD("基础收成")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropListScreen(onCropClick: (Int) -> Unit) {
    var wateringCount by remember { mutableIntStateOf(0) }
    var sortMode by remember { mutableStateOf(SortMode.INCOME) }

    val sortedCrops = remember(sortMode, wateringCount) {
        when (sortMode) {
            SortMode.INCOME -> Crop.CROPS.sortedByDescending {
                GardenCalculator.netIncomePerHour(it, wateringCount)
            }
            SortMode.GROWTH_TIME -> Crop.CROPS.sortedBy { it.growthTimeHours }
            SortMode.YIELD -> Crop.CROPS.sortedByDescending { it.baseYield }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = "露晓卉庭计算器")

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("浇水次数: $wateringCount", style = BodyLarge)
                Text(
                    "0-${WateringSimulator.maxWateringCount(24)}",
                    style = BodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
            Slider(
                value = wateringCount.toFloat(),
                onValueChange = { wateringCount = it.toInt() },
                valueRange = 0f..3f,
                steps = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            SmallTitle(text = "排序方式")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortMode.entries.forEach { mode ->
                    MiuixFilterChip(
                        selected = sortMode == mode,
                        onClick = { sortMode = mode }
                    ) {
                        Text(mode.label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            itemsIndexed(sortedCrops) { _, crop ->
                val originalIndex = Crop.CROPS.indexOf(crop)
                CropCard(
                    crop = crop,
                    wateringCount = wateringCount,
                    onClick = { onCropClick(originalIndex) }
                )
            }
        }
    }
}
