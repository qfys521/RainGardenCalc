package cn.qfys521.raingardencalculate.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.qfys521.raingardencalculate.ui.screen.about.AboutScreen
import cn.qfys521.raingardencalculate.ui.screen.crop.CropDetailScreen
import cn.qfys521.raingardencalculate.ui.screen.info.InfoScreen
import cn.qfys521.raingardencalculate.ui.screen.planner.PlannerScreen
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Screen.tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = screen.title,
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Planner.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Info.route) {
                InfoScreen(onCropClick = { cropIndex ->
                    navController.navigate("crop/$cropIndex")
                })
            }
            composable("crop/{cropIndex}") { backStackEntry ->
                val cropIndex = backStackEntry.arguments?.getString("cropIndex")?.toIntOrNull() ?: 0
                CropDetailScreen(
                    cropIndex = cropIndex,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Planner.route) {
                PlannerScreen()
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
