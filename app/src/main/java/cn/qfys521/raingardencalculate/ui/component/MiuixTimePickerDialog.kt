package cn.qfys521.raingardencalculate.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun MiuixTimePickerDialog(
    show: Boolean,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    var hour by remember(show) { mutableIntStateOf(initialHour) }
    var minute by remember(show) { mutableIntStateOf(initialMinute) }

    WindowDialog(
        show = show,
        title = "选择时间",
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPicker(
                    value = hour,
                    onValueChange = { hour = it },
                    modifier = Modifier.weight(1f),
                    range = 0..23,
                    label = { String.format("%02d", it) },
                    wrapAround = true
                )
                Text(":", style = MiuixTheme.textStyles.title2)
                NumberPicker(
                    value = minute,
                    onValueChange = { minute = it },
                    modifier = Modifier.weight(1f),
                    range = 0..59,
                    label = { String.format("%02d", it) },
                    wrapAround = true
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(text = "取消", onClick = onDismiss)
                TextButton(text = "确定", onClick = { onConfirm(hour, minute) }, colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}
