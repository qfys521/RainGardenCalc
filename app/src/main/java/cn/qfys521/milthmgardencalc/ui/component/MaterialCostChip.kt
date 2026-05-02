package cn.qfys521.milthmgardencalc.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import cn.qfys521.milthmgardencalc.model.MaterialCost

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialCostChips(costs: List<MaterialCost>) {
    if (costs.isEmpty()) {
        Text("无")
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        costs.forEach { cost ->
            MiuixAssistChip {
                Text("${cost.type.displayName}*${cost.amount}")
            }
        }
    }
}
