package tv.publivoretube.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import tv.publivoretube.data.Video

private val Canvas = Color(0xFF0B0B0F)
private val CanvasElevated = Color(0xFF111218)
private val Accent = Color(0xFFD6E2FF)
private val AccentStrong = Color(0xFFB7C8FF)
private val TextMuted = Color(0xFFB9BBC5)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    videos: List<Video>,
    onVideoSelected: (Video) -> Unit,
) {
    var selectedNav by remember { mutableIntStateOf(0) }

    Surface(
        modifier = modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = Canvas),
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                when (selectedNav) {
                    1 -> PlaceholderScreen(
                        title = "Search",
                        body = "Search will be available here in the next release.",
                    )
                    2 -> PlaceholderScreen(
                        title = "Subscriptions",
                        body = "Your subscribed channels will appear here.",
                    )
                    3 -> PlaceholderScreen(
                        title = "History",
                        body = "Your watch history will appear here.",
                    )
                    4 -> PlaceholderScreen(
                        title = "Settings",
                        body = "Playback, appearance, privacy, and TV settings will appear here.",
                    )
                    5 -> AboutScreen()
                    else -> HomeContent(
                        videos = videos,
                        onVideoSelected = onVideoSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    videos: List<Video>,
    onVideoSelected: (Video) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 38.dp,
            end = 54.dp,
            top = 30.dp,
            bottom = 54.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(34.dp),
    ) {
        item { TopBar() }

        item {
            HeroBanner(
                video = videos.firstOrNull(),
                onClick = { videos.firstOrNull()?.let(onVideoSelected) },
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

@Composable
private fun PlaceholderScreen(
    title: String,
    body: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun AboutScreen() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 48.dp,
            end = 72.dp,
            top = 42.dp,
            bottom = 56.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            Text(
                text = "About PublivoreTube",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
        }

        item {
            Text(
                text = "A TV-first, open-source video client foundation built for a clean 10-foot experience, D-pad navigation, modular playback, and future community-driven features.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "What is PublivoreTube?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )

                Text(
                    text = "PublivoreTube is an experimental Android TV application. The project separates the TV interface, content providers, playback layer, and filtering features so they can evolve independently.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Maintainers",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )

                MaintainerCard(
                    name = "Leon Sony",
                    bio = "Project creator and maintainer of PublivoreTube.",
                    onGithub = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/Jackson4Rocks"),
                            ),
                        )
                    },
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Project",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )

                Text(
                    text = "Version 0.1.0 • Open source • Built for Android TV",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                )
            }
        }
    }
}

@Composable
private fun MaintainerCard(
    name: String,
    bio: String,
    onGithub: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = SurfaceDefaults.colors(
            containerColor = CanvasElevated,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(22.dp),
                colors = SurfaceDefaults.colors(
                    containerColor = Accent.copy(alpha = 0.22f),
                    contentColor = AccentStrong,
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.first().uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = AccentStrong,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                )
            }

            Button(onClick = onGithub) {
                Text("GitHub  ↗")
            }
        }
    }
}

@Composable
private fun NavigationRail(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    val homeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        homeFocusRequester.requestFocus()
    }

    val destinations = listOf(
        "⌂" to "Home",
        "⌕" to "Search",
        "▤" to "Subscriptions",
        "◴" to "History",
        "⚙" to "Settings",
        "ⓘ" to "About",
    )

    Surface(
        modifier = Modifier
            .width(112.dp)
            .fillMaxHeight(),
        colors = SurfaceDefaults.colors(containerColor = CanvasElevated),
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
                    focusRequester = if (index == 0) homeFocusRequester else null,
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
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .then(
                focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier,
            ),
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
                    ),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            color = Color(0xDD000000),
                            shape = RoundedCornerShape(8.dp),
                        ),
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
