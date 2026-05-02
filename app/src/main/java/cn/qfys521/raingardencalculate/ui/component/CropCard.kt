package cn.qfys521.raingardencalculate.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.qfys521.raingardencalculate.calc.GardenCalculator
import cn.qfys521.raingardencalculate.model.Crop
import cn.qfys521.raingardencalculate.ui.theme.BodySmall
import cn.qfys521.raingardencalculate.ui.theme.LabelSmall
import cn.qfys521.raingardencalculate.ui.theme.TitleMedium
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropCard(
    crop: Crop,
    wateringCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val income = GardenCalculator.netIncomePerHour(crop, wateringCount)
    val avgIncome = GardenCalculator.averageNetIncomePerHour(crop, wateringCount)
    val timeDisplay = GardenCalculator.effectiveGrowthTimeDisplay(crop.growthTimeHours, wateringCount)

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        pressFeedbackType = PressFeedbackType.Sink
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = crop.name,
                    style = TitleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%.2f/h", income),
                    style = TitleMedium,
                    color = MiuixTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                crop.types.forEach { type ->
                    MiuixAssistChip {
                        Text(type.displayName, style = LabelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "收成: ${crop.baseYield}",
                    style = BodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Text(
                    text = "时间: $timeDisplay",
                    style = BodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Text(
                    text = "平均: ${String.format("%.2f/h", avgIncome)}",
                    style = BodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            if (crop.materialCosts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "耗材:",
                        style = BodySmall,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    crop.materialCosts.forEach { cost ->
                        Text(
                            text = "${cost.type.displayName}*${cost.amount}",
                            style = BodySmall,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }

            if (crop.unlockLevel != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "解锁: ${crop.unlockLevel}级",
                    style = BodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}
