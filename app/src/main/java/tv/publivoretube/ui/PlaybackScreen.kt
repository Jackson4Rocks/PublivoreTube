package tv.publivoretube.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tv.publivoretube.data.Video
import tv.publivoretube.player.PlaybackManager
import tv.publivoretube.player.PlaybackSource
import tv.publivoretube.player.SponsorBlockRepository
import tv.publivoretube.player.SponsorSegment
import tv.publivoretube.player.YouTubePlaybackResolver

private val PlayerBackground = Color(0xFF050509)
private val Panel = Color(0xE6151320)
private val PanelStrong = Color(0xF51C1827)
private val Accent = Color(0xFFE7DAFF)
private val AccentStrong = Color(0xFFC59BFF)
private val TextMuted = Color(0xFFC0BACB)
private val ButtonIdle = Color(0xFF2A2435)
private val Pill = RoundedCornerShape(50.dp)

@Composable
fun PlaybackScreen(
    video: Video,
    onBack: () -> Unit,
) {
    val resolver = remember { YouTubePlaybackResolver() }
    val sponsorBlock = remember { SponsorBlockRepository() }
    val context = LocalContext.current
    val playback = remember { PlaybackManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val backFocusRequester = remember { FocusRequester() }

    var source by remember { mutableStateOf<PlaybackSource?>(null) }
    var segments by remember { mutableStateOf<List<SponsorSegment>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var skipNotice by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBack)

    DisposableEffect(playback) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = playback.player.duration.coerceAtLeast(0L)
                    isLoading = false
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(playerError: androidx.media3.common.PlaybackException) {
                isLoading = false
                error = playerError.message ?: "Playback failed."
            }
        }

        playback.player.addListener(listener)
        onDispose {
            playback.player.removeListener(listener)
            playback.release()
        }
    }

    LaunchedEffect(video.id) {
        isLoading = true
        error = null
        source = null

        try {
            val result = resolver.resolve(
                video.youtubeUrl ?: error("Video URL unavailable"),
            )
            source = result
            playback.play(result)
            segments = runCatching {
                sponsorBlock.getSegments(video.id)
            }.getOrElse { emptyList() }
        } catch (throwable: Throwable) {
            isLoading = false
            error = throwable.message ?: "Unable to resolve this YouTube video."
        }
    }

    LaunchedEffect(playback.player, segments) {
        while (isActive) {
            delay(250)
            positionMs = playback.player.currentPosition.coerceAtLeast(0L)
            durationMs = playback.player.duration.coerceAtLeast(0L)

            val positionSeconds = positionMs / 1000.0
            val segment = segments.firstOrNull {
                positionSeconds >= it.startSeconds &&
                    positionSeconds < it.endSeconds - 0.05
            }

            if (segment != null) {
                val skipTo = (segment.endSeconds * 1000.0).toLong()
                playback.player.seekTo(skipTo)
                skipNotice = when (segment.category) {
                    "sponsor" -> "Skipped sponsor segment"
                    "intro" -> "Skipped intro"
                    "outro" -> "Skipped outro"
                    "selfpromo" -> "Skipped self promotion"
                    "interaction" -> "Skipped interaction"
                    "preview" -> "Skipped preview"
                    else -> "Skipped " + segment.category
                }
            }
        }
    }

    LaunchedEffect(skipNotice) {
        if (skipNotice != null) {
            delay(2200)
            skipNotice = null
        }
    }

    LaunchedEffect(Unit) {
        backFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBackground)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when (event.key) {
                    Key.DirectionLeft -> {
                        playback.seekBy(-10_000)
                        true
                    }

                    Key.DirectionRight -> {
                        playback.seekBy(10_000)
                        true
                    }

                    Key.DirectionCenter,
                    Key.Enter -> {
                        playback.togglePlayback()
                        true
                    }

                    else -> false
                }
            },
    ) {
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    player = playback.player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { it.player = playback.player },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xDD050509),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(28.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            PlayerPillButton(
                modifier = Modifier.focusRequester(backFocusRequester),
                icon = Icons.Rounded.ArrowBack,
                text = "Back",
                onClick = onBack,
            )

            Column(
                modifier = Modifier.width(520.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleLarge,
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xEE050509),
                        ),
                    ),
                ),
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 26.dp),
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = Panel),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .background(
                            Color.White.copy(alpha = 0.16f),
                            RoundedCornerShape(50.dp),
                        ),
                ) {
                    val progress =
                        if (durationMs > 0) {
                            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(7.dp)
                            .background(AccentStrong, RoundedCornerShape(50.dp)),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PlayerPillButton(
                        icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        text = if (isPlaying) "Pause" else "Play",
                        onClick = { playback.togglePlayback() },
                    )

                    PlayerPillButton(
                        icon = Icons.Rounded.FastRewind,
                        text = "10s",
                        onClick = { playback.seekBy(-10_000) },
                    )

                    PlayerPillButton(
                        icon = Icons.Rounded.FastForward,
                        text = "10s",
                        onClick = { playback.seekBy(10_000) },
                    )

                    SponsorChip(enabled = true, segmentCount = segments.size)

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = formatTime(positionMs) + " / " + formatTime(durationMs),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
        }

        if (isLoading) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(22.dp),
                colors = SurfaceDefaults.colors(containerColor = PanelStrong),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Preparing video…",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = "Extracting a playable stream",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        }

        if (error != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(40.dp),
                shape = RoundedCornerShape(24.dp),
                colors = SurfaceDefaults.colors(containerColor = PanelStrong),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = AccentStrong,
                    )
                    Text(
                        text = "Playback unavailable",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )
                    PlayerPillButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "Retry",
                        onClick = {
                            scope.launch {
                                isLoading = true
                                error = null
                                runCatching {
                                    val result = resolver.resolve(
                                        video.youtubeUrl ?: error("Video URL unavailable"),
                                    )
                                    source = result
                                    playback.play(result)
                                    segments = sponsorBlock.getSegments(video.id)
                                }.onFailure {
                                    isLoading = false
                                    error = it.message ?: "Playback failed."
                                }
                            }
                        },
                    )
                }
            }
        }

        if (skipNotice != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(28.dp),
                shape = Pill,
                colors = SurfaceDefaults.colors(containerColor = PanelStrong),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Accent,
                    )
                    Text(
                        text = skipNotice.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerPillButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = ButtonDefaults.shape(Pill),
        colors = ButtonDefaults.colors(
            containerColor = ButtonIdle,
            contentColor = Color.White,
            focusedContainerColor = AccentStrong,
            focusedContentColor = PlayerBackground,
            pressedContainerColor = AccentStrong,
            pressedContentColor = PlayerBackground,
        ),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
            )
            Text(text)
        }
    }
}

@Composable
private fun SponsorChip(
    enabled: Boolean,
    segmentCount: Int,
) {
    Surface(
        shape = Pill,
        colors = SurfaceDefaults.colors(
            containerColor = if (enabled) Color(0xFF26213A) else ButtonIdle,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AccentStrong, Pill),
            )
            Text(
                text = if (segmentCount > 0) "SponsorBlock • $segmentCount" else "SponsorBlock",
                style = MaterialTheme.typography.labelMedium,
                color = Accent,
            )
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    if (milliseconds <= 0L) return "0:00"

    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        "%d:%02d:%02d".format(hours, remainingMinutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
