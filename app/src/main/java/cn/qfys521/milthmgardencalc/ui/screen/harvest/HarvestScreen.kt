package cn.qfys521.milthmgardencalc.ui.screen.harvest

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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.qfys521.milthmgardencalc.calc.HarvestProbability
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HarvestScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = "丰收概率")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SmallTitle(text = "说明")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("每次收割时有小概率使产量增加:")
                    Text("  丰收: 产量 ×2 (增加100%)")
                    Text("  大丰收: 产量 ×4 (增加300%)")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("浇水可以提高丰收概率，最多浇水3次。")
                }
            }

            SmallTitle(text = "概率表")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("浇水", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("普通", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("丰收", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("大丰收", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    for (w in 0..3) {
                        val outcomes = HarvestProbability.outcomes(w)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${w}次", modifier = Modifier.weight(1f))
                            outcomes.forEach { outcome ->
                                Text(
                                    String.format("%.1f%%", outcome.probability * 100),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (w < 3) HorizontalDivider()
                    }
                }
            }

            SmallTitle(text = "期望收益乘数")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    for (w in 0..3) {
                        val multiplier = HarvestProbability.expectedYieldMultiplier(w)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("浇水${w}次")
                            Text(
                                String.format("×%.3f", multiplier),
                                fontWeight = FontWeight.SemiBold,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
