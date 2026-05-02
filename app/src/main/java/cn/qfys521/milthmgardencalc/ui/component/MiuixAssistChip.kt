package cn.qfys521.milthmgardencalc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixAssistChip(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MiuixTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        label()
    }
}
