package cn.qfys521.milthmgardencalc.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixDropdownField(
    value: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    OverlayListPopup(
        show = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEachIndexed { index, option ->
            DropdownImpl(
                text = option,
                optionSize = options.size,
                isSelected = index == selectedIndex,
                index = index,
                onSelectedIndexChange = { onSelect(it); expanded = false }
            )
        }
    }

    TextField(
        value = value,
        onValueChange = {},
        modifier = modifier.fillMaxWidth().clickable { expanded = true },
        label = label,
        readOnly = true,
        enabled = true,
        useLabelAsPlaceholder = true,
        trailingIcon = {
            Icon(
                imageVector = MiuixIcons.Basic.ArrowUpDown,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
    )
}
