package cn.qfys521.milthmgardencalc.ui.screen.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.qfys521.milthmgardencalc.model.Song
import cn.qfys521.milthmgardencalc.ui.component.MaterialCostChips
import cn.qfys521.milthmgardencalc.ui.theme.LabelMedium
import cn.qfys521.milthmgardencalc.ui.theme.TitleMedium

@Composable
fun SongListScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = "曲目解锁")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            item {
                SmallTitle(text = "歌曲列表")
            }
            items(Song.SONGS) { song ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = song.name,
                            style = TitleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("解锁耗材:", style = LabelMedium)
                        MaterialCostChips(song.materialCosts)
                    }
                }
            }
        }
    }
}
