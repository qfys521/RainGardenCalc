package cn.qfys521.milthmgardencalc.ui.screen.level

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.qfys521.milthmgardencalc.model.CropType
import cn.qfys521.milthmgardencalc.model.LevelUpgrade
import cn.qfys521.milthmgardencalc.ui.component.MaterialCostChips
import cn.qfys521.milthmgardencalc.ui.theme.LabelMedium
import cn.qfys521.milthmgardencalc.ui.theme.TitleMedium
import cn.qfys521.milthmgardencalc.ui.theme.TitleSmall

@Composable
fun LevelUpgradeScreen() {
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = "升级耗材")

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SmallTitle(text = "目标等级")
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
                    Text(
                        "升级至 ${targetLevelInt} 级总计",
                        style = TitleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    cumulativeCosts.forEach { (type, amount) ->
                        Text("${type.displayName}: $amount")
                    }
                    if (cumulativeCosts.isEmpty()) {
                        Text("无需耗材")
                    }
                }
            }
        }

        SmallTitle(text = "升级详情")

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Lv.${upgrade.level}",
                                style = TitleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
