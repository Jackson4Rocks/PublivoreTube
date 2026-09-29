package tv.publivoretube.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import tv.publivoretube.data.Video

private val Canvas = Color(0xFF0B0B0F)
private val CanvasElevated = Color(0xFF111218)
private val Accent = Color(0xFFD6E2FF)
private val AccentStrong = Color(0xFFB7C8FF)
private val TextMuted = Color(0xFFB9BBC5)

@Composable
fun HomeScreen(
    videos: List<Video>,
    onVideoSelected: (Video) -> Unit,
) {
    var selectedNav by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Canvas,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            NavigationRail(
                selected = selectedNav,
                onSelected = { selectedNav = it },
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 38.dp,
                    end = 54.dp,
                    top = 30.dp,
                    bottom = 54.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(34.dp),
            ) {
                item {
                    TopBar()
                }

                item {
                    HeroBanner(
                        video = videos.firstOrNull(),
                        onClick = {
                            videos.firstOrNull()?.let(onVideoSelected)
                        },
                    )
                }

                item {
                    ContentRow(
                        title = "Recommended for you",
                        videos = videos,
                        onVideoSelected = onVideoSelected,
                    )
                }

                item {
                    ContentRow(
                        title = "Trending now",
                        videos = videos.asReversed(),
                        onVideoSelected = onVideoSelected,
                    )
                }

                item {
                    ContentRow(
                        title = "Continue watching",
                        videos = videos.drop(1) + videos.take(1),
                        onVideoSelected = onVideoSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationRail(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    val destinations = listOf(
        "⌂" to "Home",
        "⌕" to "Search",
        "▤" to "Subscriptions",
        "◴" to "History",
        "⚙" to "Settings",
    )

    Surface(
        modifier = Modifier
            .width(112.dp)
            .fillMaxHeight(),
        color = CanvasElevated,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "P",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentStrong,
            )

            Spacer(modifier = Modifier.height(12.dp))

            destinations.forEachIndexed { index, (icon, label) ->
                RailDestination(
                    icon = icon,
                    label = label,
                    selected = selected == index,
                    onClick = { onSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun RailDestination(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "PublivoreTube",
            style = MaterialTheme.typography.headlineMedium,
            color = Accent,
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = { }) {
            Text("Search")
        }

        Button(onClick = { }) {
            Text("Sign in")
        }
    }
}

@Composable
private fun HeroBanner(
    video: Video?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2A3044),
                            Color(0xFF171A24),
                            Color(0xFF101116),
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "FEATURED",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentStrong,
                )

                Text(
                    text = video?.title ?: "Welcome to PublivoreTube",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )

                Text(
                    text = video?.channel ?: "An open-source TV-first video client",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onClick) {
                        Text("Play")
                    }
                    Button(onClick = { }) {
                        Text("More info")
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentRow(
    title: String,
    videos: List<Video>,
    onVideoSelected: (Video) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "See all  ›",
                style = MaterialTheme.typography.labelLarge,
                color = AccentStrong,
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(end = 16.dp),
        ) {
            items(videos, key = { it.id + title }) { video ->
                VideoCard(
                    video = video,
                    onClick = { onVideoSelected(video) },
                )
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: Video,
    onClick: () -> Unit,
) {
    var focused by remember { mutableIntStateOf(0) }

    val cardShape = RoundedCornerShape(24.dp)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(300.dp)
            .height(230.dp)
            .onFocusChanged { focused = if (it.isFocused) 1 else 0 }
            .then(
                if (focused == 1) {
                    Modifier.border(
                        width = 3.dp,
                        color = AccentStrong,
                        shape = cardShape,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Accent.copy(alpha = 0.42f),
                                Color(0xFF2C3040),
                            ),
                        ),
                    )
                    .focusable(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Surface(
                    modifier = Modifier.padding(10.dp),
                    color = Color(0xDD000000),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = video.duration,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                )
                Text(
                    text = video.channel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                )
            }
        }
    }
}
