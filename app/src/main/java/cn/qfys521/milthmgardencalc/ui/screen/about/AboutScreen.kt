package cn.qfys521.milthmgardencalc.ui.screen.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.qfys521.milthmgardencalc.ui.theme.BodyMedium
import cn.qfys521.milthmgardencalc.ui.theme.TitleLarge
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = "关于")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SmallTitle(text = "应用信息")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("露晓卉庭计算器", style = TitleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Milthm Garden Calculator", style = BodyMedium, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("一款用于游戏《Milthm》中露晓卉庭花园系统的辅助规划工具。", style = BodyMedium)
                }
            }

            SmallTitle(text = "功能")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("• 作物收益计算与排序")
                    Text("• 升级路线规划与材料计算")
                    Text("• 歌曲解锁材料分析")
                    Text("• 自动种植规划与登录时间表")
                    Text("• 丰收概率与期望收益计算")
                    Text("• 浇水策略优化")
                }
            }

            SmallTitle(text = "数据来源")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("游戏数据基于 Milthm 社区 整理。", style = BodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        text = "Milthm 社区 Wiki",
                        onClick = { uriHandler.openUri("https://mkzi-nya.github.io/milthm-calculator-web/wiki/#garden") },
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
