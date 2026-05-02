package cn.qfys521.raingardencalculate.ui.screen.crop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.qfys521.raingardencalculate.calc.GardenCalculator
import cn.qfys521.raingardencalculate.calc.HarvestProbability
import cn.qfys521.raingardencalculate.calc.WateringSimulator
import cn.qfys521.raingardencalculate.model.Crop
import cn.qfys521.raingardencalculate.ui.component.MaterialCostChips
import cn.qfys521.raingardencalculate.ui.component.StatRow
import cn.qfys521.raingardencalculate.ui.theme.LabelLarge
import cn.qfys521.raingardencalculate.ui.theme.TitleMedium
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CropDetailScreen(cropIndex: Int, onBack: () -> Unit) {
    val crop = Crop.CROPS.getOrNull(cropIndex) ?: return
    var wateringCount by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = crop.name,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallTitle(text = "基础信息")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatRow("类型", crop.types.joinToString("/") { it.displayName })
                    StatRow("基础收成", "${crop.baseYield}")
                    StatRow("生长时间", if (crop.growthTimeHours <= 0) "即时" else "${crop.growthTimeHours}h")
                    StatRow("解锁等级", crop.unlockLevel?.let { "${it}级" } ?: "无")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("耗材", style = LabelLarge)
                    MaterialCostChips(crop.materialCosts)
                }
            }

            SmallTitle(text = "浇水效果")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("浇水次数: $wateringCount")
                    }
                    Slider(
                        value = wateringCount.toFloat(),
                        onValueChange = { wateringCount = it.toInt() },
                        valueRange = 0f..3f,
                        steps = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (crop.growthTimeHours > 0) {
                        val income = GardenCalculator.netIncomePerHour(crop, wateringCount)
                        val avgIncome = GardenCalculator.averageNetIncomePerHour(crop, wateringCount)
                        val cooldown = WateringSimulator.wateringCooldown(crop.growthTimeHours)

                        StatRow("有效时长", GardenCalculator.effectiveGrowthTimeDisplay(crop.growthTimeHours, wateringCount))
                        StatRow("浇水冷却", "${cooldown}h")
                        StatRow("净收益/时", String.format("%.2f", income))
                        StatRow("平均收益/时", String.format("%.2f", avgIncome))

                        val outcomes = HarvestProbability.outcomes(wateringCount)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("丰收概率", style = LabelLarge)
                        outcomes.forEach { outcome ->
                            StatRow(
                                outcome.name,
                                String.format("%.1f%% (×%.1f)", outcome.probability * 100, outcome.yieldMultiplier)
                            )
                        }
                    } else {
                        Text("即时收获，无需浇水", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        StatRow("净收益/时", String.format("%.2f", crop.baseYield.toDouble()))
                    }
                }
            }

            if (crop.materialCosts.isNotEmpty()) {
                SmallTitle(text = "耗材分析")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val costTime = GardenCalculator.materialCostTime(crop)
                        StatRow("耗材生产时长", String.format("%.1fh", costTime))
                        val netWithCost = GardenCalculator.netIncomeWithMaterialCost(crop, wateringCount)
                        StatRow("含耗材净收益/时", String.format("%.2f", netWithCost))
                    }
                }
            }

            SmallTitle(text = "浇水效果表")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (crop.growthTimeHours > 0) {
                        for (w in 0..3) {
                            val income = GardenCalculator.netIncomePerHour(crop, w)
                            val avgIncome = GardenCalculator.averageNetIncomePerHour(crop, w)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("浇水${w}次", modifier = Modifier.weight(1f))
                                Text(
                                    GardenCalculator.effectiveGrowthTimeDisplay(crop.growthTimeHours, w),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    String.format("%.2f/h", income),
                                    modifier = Modifier.weight(1f),
                                    color = MiuixTheme.colorScheme.primary
                                )
                                Text(
                                    String.format("%.2f/h", avgIncome),
                                    modifier = Modifier.weight(1f),
                                    color = MiuixTheme.colorScheme.secondary
                                )
                            }
                            if (w < 3) HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
