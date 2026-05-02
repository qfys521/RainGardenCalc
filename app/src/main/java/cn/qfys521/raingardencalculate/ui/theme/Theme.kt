package cn.qfys521.raingardencalculate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme

@Composable
fun GardenCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val miuixColors = if (darkTheme) miuixDarkColorScheme() else miuixLightColorScheme()

    MiuixTheme(
        colors = miuixColors
    ) {
        content()
    }
}
