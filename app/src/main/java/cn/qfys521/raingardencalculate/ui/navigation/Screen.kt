package cn.qfys521.raingardencalculate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Info : Screen("info", "信息", Icons.Default.Eco)
    object Planner : Screen("planner", "规划", Icons.Default.Calculate)
    object About : Screen("about", "关于", Icons.Default.Info)

    companion object {
        val tabs = listOf(Info, Planner, About)
    }
}
