package tv.publivoretube.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.Text
import tv.publivoretube.data.Video

@Composable
fun HomeScreen(
    videos: List<Video>,
    onVideoSelected: (Video) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Text("PublivoreTube")
        Text("A TV-first open-source YouTube client foundation")

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(videos, key = { it.id }) { video ->
                VideoCard(video = video, onClick = { onVideoSelected(video) })
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: Video,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .height(220.dp)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                    onClick()
                    true
                } else {
                    false
                }
            },
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(video.duration)
            Text(video.title)
            Text(video.channel)
        }
    }
}
