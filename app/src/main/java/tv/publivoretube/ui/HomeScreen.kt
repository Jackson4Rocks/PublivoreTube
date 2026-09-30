package tv.publivoretube.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subscriptions
import coil.compose.AsyncImage
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import tv.publivoretube.R
import tv.publivoretube.data.Video

private val Canvas = Color(0xFF08070D)
private val CanvasElevated = Color(0xFF11101A)
private val CardSurface = Color(0xFF171521)
private val Accent = Color(0xFFE7DAFF)
private val AccentStrong = Color(0xFFC59BFF)
private val TextMuted = Color(0xFFB9B2C5)
private val PillShape = RoundedCornerShape(50.dp)

private data class NavDestination(
    val icon: ImageVector,
    val label: String,
)

private val NavDestinations = listOf(
    NavDestination(Icons.Rounded.Home, "Home"),
    NavDestination(Icons.Rounded.Search, "Search"),
    NavDestination(Icons.Rounded.Subscriptions, "Subscriptions"),
    NavDestination(Icons.Rounded.History, "History"),
    NavDestination(Icons.Rounded.Settings, "Settings"),
    NavDestination(Icons.Rounded.Info, "About"),
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    videos: List<Video>,
    searchResults: List<Video>,
    searchQuery: String,
    homeLoading: Boolean,
    searchLoading: Boolean,
    statusMessage: String?,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
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
                    1 -> SearchScreen(
                        query = searchQuery,
                        results = searchResults,
                        loading = searchLoading,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearch,
                        onVideoSelected = onVideoSelected,
                    )

                    5 -> AboutScreen()

                    else -> HomeContent(
                        videos = videos,
                        homeLoading = homeLoading,
                        statusMessage = statusMessage,
                        onSearch = { selectedNav = 1 },
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
    val homeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        homeFocusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
            .width(188.dp)
            .fillMaxHeight(),
        colors = SurfaceDefaults.colors(containerColor = CanvasElevated),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(62.dp),
                shape = RoundedCornerShape(20.dp),
                colors = SurfaceDefaults.colors(containerColor = CardSurface),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_publivoretube_mark),
                    contentDescription = "PublivoreTube",
                    modifier = Modifier.padding(7.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            NavDestinations.forEachIndexed { index, destination ->
                NavigationItem(
                    destination = destination,
                    selected = selected == index,
                    focusRequester = if (index == 0) homeFocusRequester else null,
                    onClick = { onSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    destination: NavDestination,
    selected: Boolean,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = ButtonDefaults.shape(PillShape),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .then(
                if (selected) {
                    Modifier.border(2.dp, AccentStrong, PillShape)
                } else {
                    Modifier
                },
            ),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) Accent else TextMuted,
            )

            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Accent else Color.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HomeContent(
    videos: List<Video>,
    homeLoading: Boolean,
    statusMessage: String?,
    onSearch: () -> Unit,
    onVideoSelected: (Video) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 42.dp,
            end = 52.dp,
            top = 28.dp,
            bottom = 50.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item { TopBar(onSearch = onSearch) }

        if (statusMessage != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color(0xFF21192B)),
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                    )
                }
            }
        }

        if (homeLoading) {
            item { LoadingSection() }
        } else {
            item {
                HeroBanner(
                    video = videos.firstOrNull(),
                    onClick = { videos.firstOrNull()?.let(onVideoSelected) },
                )
            }

            item { ContentRow("Recommended", videos, onVideoSelected) }
            item { ContentRow("Trending", videos.asReversed(), onVideoSelected) }
            item {
                ContentRow(
                    "Continue watching",
                    videos.drop(1) + videos.take(1),
                    onVideoSelected,
                )
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Loading YouTube…",
            style = MaterialTheme.typography.titleMedium,
            color = TextMuted,
        )
    }
}

@Composable
private fun TopBar(onSearch: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "PublivoreTube",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            Text(
                text = "A clean TV-first video experience",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSearch,
            shape = ButtonDefaults.shape(PillShape),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Text("Search")
            }
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
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = video?.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xF00B0910),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(30.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    maxLines = 2,
                )

                Text(
                    text = video?.channel ?: "YouTube-powered TV client",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                )

                Button(
                    onClick = onClick,
                    shape = ButtonDefaults.shape(PillShape),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Text("Open video")
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(end = 12.dp),
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
    val shape = RoundedCornerShape(22.dp)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(310.dp)
            .height(238.dp)
            .onFocusChanged { focused = if (it.isFocused) 1 else 0 }
            .then(
                if (focused == 1) {
                    Modifier.border(3.dp, AccentStrong, shape)
                } else {
                    Modifier
                },
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(174.dp)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF332455),
                                Color(0xFF171421),
                            ),
                        ),
                    ),
            ) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                if (video.duration.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .background(
                                color = Color(0xDD000000),
                                shape = RoundedCornerShape(9.dp),
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
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
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

@Composable
private fun SearchScreen(
    query: String,
    results: List<Video>,
    loading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onVideoSelected: (Video) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 42.dp,
                end = 52.dp,
                top = 28.dp,
                bottom = 38.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Search YouTube",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .focusRequester(focusRequester),
                shape = ButtonDefaults.shape(PillShape),
                colors = SurfaceDefaults.colors(containerColor = CardSurface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AccentStrong,
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (query.isBlank()) {
                                    Text(
                                        text = "Search YouTube…",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }

            Button(
                onClick = { onSearch(query) },
                shape = ButtonDefaults.shape(PillShape),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Text("Search")
                }
            }
        }

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Searching…",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMuted,
                )
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Type a search and press Search.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
            ) {
                items(results, key = { "search-" + it.id }) { video ->
                    SearchResultCard(
                        video = video,
                        onClick = { onVideoSelected(video) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    video: Video,
    onClick: () -> Unit,
) {
    var focused by remember { mutableIntStateOf(0) }
    val shape = RoundedCornerShape(22.dp)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .onFocusChanged { focused = if (it.isFocused) 1 else 0 }
            .then(
                if (focused == 1) {
                    Modifier.border(3.dp, AccentStrong, shape)
                } else {
                    Modifier
                },
            ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = video.thumbnail,
                contentDescription = video.title,
                modifier = Modifier
                    .width(238.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 22.dp,
                            bottomStart = 22.dp,
                        ),
                    ),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                )
                Text(
                    text = video.channel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    maxLines = 1,
                )
                if (video.duration.isNotBlank()) {
                    Text(
                        text = video.duration,
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentStrong,
                    )
                }
            }
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
        verticalArrangement = Arrangement.spacedBy(24.dp),
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
                text = "PublivoreTube is an open-source Android TV client foundation. It combines a remote-first Material 3 interface with a modular content layer and YouTube Data API metadata.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = SurfaceDefaults.colors(containerColor = CardSurface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = SurfaceDefaults.colors(containerColor = Color(0xFF281840)),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_publivoretube_mark),
                            contentDescription = null,
                            modifier = Modifier.padding(5.dp),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Leon Sony",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                        Text(
                            text = "Project creator and maintainer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                        )
                    }

                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/Jackson4Rocks"),
                                ),
                            )
                        },
                        shape = ButtonDefaults.shape(PillShape),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Text("GitHub")
                        }
                    }
                }
            }
        }
    }
}
