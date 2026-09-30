package tv.publivoretube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import android.content.Intent
import tv.publivoretube.data.ChannelInfo
import tv.publivoretube.data.YoutubeDataApi
import tv.publivoretube.ui.theme.PublivoreTubeTheme

class ChannelActivity : ComponentActivity() {
    private val channelId: String
        get() = intent.getStringExtra(EXTRA_CHANNEL_ID).orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PublivoreTubeTheme {
                ChannelPage(
                    channelId = channelId,
                    onBack = { finish() },
                    onVideoSelected = { video ->
                        startActivity(
                            Intent(this, YouTubePlayerActivity::class.java).apply {
                                putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, video.id)
                                putExtra(YouTubePlayerActivity.EXTRA_TITLE, video.title)
                                putExtra(YouTubePlayerActivity.EXTRA_CHANNEL, video.channel)
                                putExtra(YouTubePlayerActivity.EXTRA_CHANNEL_ID, video.channelId)
                                putExtra(YouTubePlayerActivity.EXTRA_URL, video.youtubeUrl)
                            },
                        )
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "channel_id"
    }
}

@androidx.compose.runtime.Composable
private fun ChannelPage(
    channelId: String,
    onBack: () -> Unit,
    onVideoSelected: (tv.publivoretube.data.Video) -> Unit,
) {
    val api = remember { YoutubeDataApi(BuildConfig.YOUTUBE_API_KEY) }
    var info by remember { mutableStateOf<ChannelInfo?>(null) }
    var videos by remember { mutableStateOf(emptyList<tv.publivoretube.data.Video>()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(channelId) {
        try {
            info = api.channelInfo(channelId)
            videos = api.channelVideos(channelId)
        } catch (t: Throwable) {
            error = t.message ?: "Unable to load channel."
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = Color(0xFF08070D)),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(42.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onBack,
                        shape = ButtonDefaults.shape(RoundedCornerShape(50.dp)),
                    ) {
                        Icon(Icons.Rounded.ArrowBack, null)
                        Text(" Back")
                    }
                    Spacer(Modifier.size(18.dp))
                    Text(
                        info?.title ?: "Channel",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                    )
                }
            }

            item {
                val current = info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color(0xFF171521)),
                ) {
                    Row(
                        modifier = Modifier.padding(22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        AsyncImage(
                            model = current?.thumbnail,
                            contentDescription = current?.title,
                            modifier = Modifier.size(96.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                current?.title ?: "Loading channel…",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                            )
                            val stats = buildString {
                                if (!current?.subscriberCount.isNullOrBlank()) {
                                    append(current?.subscriberCount)
                                    append(" subscribers")
                                }
                                if (!current?.videoCount.isNullOrBlank()) {
                                    if (isNotEmpty()) append("  •  ")
                                    append(current?.videoCount)
                                    append(" videos")
                                }
                            }
                            Text(
                                stats.ifBlank { "YouTube channel" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFB9B2C5),
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Latest videos",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }

            if (error != null) {
                item {
                    Text(error.orEmpty(), color = Color(0xFFE7DAFF))
                }
            } else if (videos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Loading channel videos…", color = Color(0xFFB9B2C5))
                    }
                }
            } else {
                items(videos, key = { it.id }) { video ->
                    Card(
                        onClick = { onVideoSelected(video) },
                        modifier = Modifier.fillMaxWidth().height(128.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = video.title,
                                modifier = Modifier.size(width = 188.dp, height = 106.dp),
                            )
                            Column(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(
                                    video.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 2,
                                )
                                Text(
                                    video.duration,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB9B2C5),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                modifier = Modifier.padding(end = 22.dp).size(30.dp),
                                tint = Color(0xFFC59BFF),
                            )
                        }
                    }
                }
            }
        }
    }
}
