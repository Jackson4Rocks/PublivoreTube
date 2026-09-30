package tv.publivoretube

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import tv.publivoretube.data.ChannelInfo
import tv.publivoretube.data.ChannelShelf
import tv.publivoretube.data.ChannelShelfRow
import tv.publivoretube.data.Video
import tv.publivoretube.data.YoutubeDataApi
import tv.publivoretube.ui.theme.PublivoreTubeTheme

private val Background = Color(0xFF08070D)
private val Panel = Color(0xFF171521)
private val AccentStrong = Color(0xFFC59BFF)
private val Muted = Color(0xFFB9B2C5)

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

@Composable
private fun ChannelPage(
    channelId: String,
    onBack: () -> Unit,
    onVideoSelected: (Video) -> Unit,
) {
    val api = remember { YoutubeDataApi(BuildConfig.YOUTUBE_API_KEY) }
    var info by remember { mutableStateOf<ChannelInfo?>(null) }
    var shelves by remember { mutableStateOf(emptyList<ChannelShelf>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(channelId) {
        try {
            info = api.channelInfo(channelId)
            shelves = api.channelShelves(channelId)
        } catch (t: Throwable) {
            error = t.message ?: "Unable to load channel."
        } finally {
            loading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = Background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 42.dp,
                end = 52.dp,
                top = 24.dp,
                bottom = 56.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp),
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
                        Text("  Back")
                    }
                    Spacer(Modifier.width(18.dp))
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
                    shape = RoundedCornerShape(26.dp),
                    colors = SurfaceDefaults.colors(containerColor = Panel),
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF241936),
                                            Color(0xFF11101A),
                                        ),
                                    ),
                                ),
                        )

                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            AsyncImage(
                                model = current?.thumbnail,
                                contentDescription = current?.title,
                                modifier = Modifier
                                    .size(106.dp)
                                    .clip(RoundedCornerShape(28.dp)),
                                contentScale = ContentScale.Crop,
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
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
                                    color = Muted,
                                )

                                if (!current?.description.isNullOrBlank()) {
                                    Text(
                                        current?.description.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Muted,
                                        maxLines = 3,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (error != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = SurfaceDefaults.colors(containerColor = Color(0xFF21192B)),
                    ) {
                        Text(
                            error.orEmpty(),
                            modifier = Modifier.padding(18.dp),
                            color = Color(0xFFE7DAFF),
                        )
                    }
                }
            }

            if (loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Loading channel…",
                            color = Muted,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            } else if (shelves.isEmpty() && error == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No public videos or channel shelves found.",
                            color = Muted,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            shelves.forEach { shelf ->
                item(key = "section-" + shelf.position + "-" + shelf.type) {
                    ChannelShelfBlock(
                        shelf = shelf,
                        onVideoSelected = onVideoSelected,
                    )
                }
            }

            if (shelves.isNotEmpty()) {
                item {
                    Text(
                        "Channel layout loaded from YouTube",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelShelfBlock(
    shelf: ChannelShelf,
    onVideoSelected: (Video) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            shelf.title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )

        shelf.rows.forEachIndexed { rowIndex, row ->
            ChannelShelfRowView(
                row = row,
                rowIndex = rowIndex,
                onVideoSelected = onVideoSelected,
            )
        }
    }
}

@Composable
private fun ChannelShelfRowView(
    row: ChannelShelfRow,
    rowIndex: Int,
    onVideoSelected: (Video) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        if (row.title.isNotBlank()) {
            Text(
                row.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (rowIndex == 0) Color.White else Muted,
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(end = 22.dp),
        ) {
            items(
                row.videos,
                key = { "channel-" + row.playlistId.orEmpty() + "-" + it.id },
            ) { video ->
                ChannelVideoCard(video, onVideoSelected)
            }
        }
    }
}

@Composable
private fun ChannelVideoCard(
    video: Video,
    onVideoSelected: (Video) -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)

    Card(
        onClick = { onVideoSelected(video) },
        modifier = Modifier
            .width(310.dp)
            .height(230.dp),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = Panel,
            contentColor = Color.White,
            focusedContainerColor = Panel,
            focusedContentColor = Color.White,
            pressedContainerColor = Panel,
            pressedContentColor = Color.White,
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(3.dp, AccentStrong),
                shape = shape,
            ),
        ),
        scale = CardDefaults.scale(focusedScale = 1.04f),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(Color(0xFF171421)),
            ) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                if (video.duration.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = SurfaceDefaults.colors(containerColor = Color(0xDD000000)),
                    ) {
                        Text(
                            video.duration,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                )
                Text(
                    video.channel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                )
            }
        }
    }
}
