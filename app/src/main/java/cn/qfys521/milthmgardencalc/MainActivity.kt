package cn.qfys521.milthmgardencalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cn.qfys521.milthmgardencalc.ui.navigation.AppNavigation
import cn.qfys521.milthmgardencalc.ui.theme.GardenCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GardenCalcTheme {
                AppNavigation()
            }
        }
    }
}
