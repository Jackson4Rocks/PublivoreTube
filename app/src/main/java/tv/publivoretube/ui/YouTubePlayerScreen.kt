package tv.publivoretube.ui

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import tv.publivoretube.data.Video

private val PlayerBackground = Color(0xFF07060B)
private val PlayerPanel = Color(0xFF15121E)
private val PlayerPanelStrong = Color(0xFF1D1828)
private val Accent = Color(0xFFE7DAFF)
private val AccentStrong = Color(0xFFC59BFF)
private val TextMuted = Color(0xFFB9B2C5)
private val Pill = RoundedCornerShape(50.dp)
private val PlayerShape = RoundedCornerShape(24.dp)

@Composable
fun YouTubePlayerScreen(
    video: Video,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val videoId = video.id
    var captionsEnabled by remember(videoId) { mutableStateOf(false) }
    var settingsOpen by remember(videoId) { mutableStateOf(false) }
    var statusMessage by remember(videoId) {
        mutableStateOf("Use the YouTube player controls for captions, quality, fullscreen and playback settings.")
    }

    val webView = remember(videoId) {
        WebView(context).apply {
            setBackgroundColor(AndroidColor.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadsImagesAutomatically = true
            settings.allowContentAccess = true
            settings.allowFileAccess = false

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript(
                        "window.ptBindPlayer && window.ptBindPlayer();",
                        null,
                    )
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    error: android.webkit.WebResourceError?,
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        statusMessage = "YouTube player failed to load. Check the device internet connection."
                    }
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    errorResponse: android.webkit.WebResourceResponse?,
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    if (request?.isForMainFrame == true &&
                        errorResponse?.statusCode ?: 200 >= 400
                    ) {
                        statusMessage = "YouTube player returned HTTP " +
                            (errorResponse?.statusCode ?: 0) + "."
                    }
                }
            }
            webChromeClient = WebChromeClient()

            val embedUrl =
                "https://www.youtube.com/embed/$videoId" +
                    "?enablejsapi=1" +
                    "&autoplay=1" +
                    "&controls=1" +
                    "&playsinline=1" +
                    "&rel=0" +
                    "&fs=1" +
                    "&origin=https%3A%2F%2Ftv.publivoretube"

            loadUrl(
                embedUrl,
                mapOf("Referer" to "https://tv.publivoretube/"),
            )
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    }

    BackHandler(onBack = onBack)

    fun runPlayerCommand(command: String) {
        webView.post {
            webView.evaluateJavascript(command, null)
        }
    }

    fun shareVideo() {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, video.youtubeUrl ?: "https://youtu.be/$videoId")
                },
                "Share video",
            ),
        )
    }

    fun openChannel() {
        val channelId = video.channelId
        if (channelId.isNullOrBlank()) {
            statusMessage = "Channel information is unavailable for this video."
            return
        }

        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/channel/$channelId"),
            ),
        )
    }

    fun openOnYouTube() {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(video.youtubeUrl ?: "https://www.youtube.com/watch?v=$videoId"),
            ),
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = PlayerBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionButton(
                    icon = Icons.Rounded.ArrowBack,
                    text = "Back",
                    onClick = onBack,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        maxLines = 1,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = PlayerShape,
                colors = SurfaceDefaults.colors(containerColor = PlayerPanel),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(PlayerShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { webView },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(18.dp)),
                    )
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(end = 16.dp),
            ) {
                item {
                    ActionButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "Play",
                        onClick = {
                            runPlayerCommand("window.ptPlay && window.ptPlay();")
                            statusMessage = "Play"
                        },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.Pause,
                        text = "Pause",
                        onClick = {
                            runPlayerCommand("window.ptPause && window.ptPause();")
                            statusMessage = "Pause"
                        },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.VideoLibrary,
                        text = "Channel",
                        onClick = { openChannel() },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.ThumbUp,
                        text = "Like",
                        onClick = {
                            statusMessage = "Like needs a signed-in YouTube account."
                        },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.ThumbDown,
                        text = "Dislike",
                        onClick = {
                            statusMessage = "Dislike needs a signed-in YouTube account."
                        },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.Add,
                        text = "Playlist",
                        onClick = {
                            statusMessage = "Add to playlist needs a signed-in YouTube account."
                        },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.ClosedCaption,
                        text = if (captionsEnabled) "Captions On" else "Captions",
                        onClick = {
                            captionsEnabled = !captionsEnabled
                            if (captionsEnabled) {
                                runPlayerCommand(
                                    "window.ptCaptions && window.ptCaptions(true);",
                                )
                                statusMessage = "Captions requested. Use the YouTube player controls to choose a language."
                            } else {
                                runPlayerCommand(
                                    "window.ptCaptions && window.ptCaptions(false);",
                                )
                                statusMessage = "Captions requested to turn off."
                            }
                        },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.Settings,
                        text = "Settings",
                        onClick = {
                            settingsOpen = !settingsOpen
                            statusMessage =
                                if (settingsOpen) {
                                    "YouTube settings are available inside the player controls."
                                } else {
                                    "Player settings closed."
                                }
                        },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.Share,
                        text = "Share",
                        onClick = { shareVideo() },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.OpenInNew,
                        text = "Open on YouTube",
                        onClick = { openOnYouTube() },
                    )
                }

                item {
                    ActionButton(
                        icon = Icons.Rounded.MoreHoriz,
                        text = "More",
                        onClick = {
                            statusMessage =
                                "Use the YouTube player controls for subtitles, quality, fullscreen and other playback options."
                        },
                    )
                }
            }

            if (settingsOpen) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = SurfaceDefaults.colors(containerColor = PlayerPanelStrong),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = AccentStrong,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "YouTube's own settings menu remains inside the embedded player; PublivoreTube does not cover or replace it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                        )
                    }
                }
            }

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(50.dp),
        shape = ButtonDefaults.shape(Pill),
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF25202F),
            contentColor = Color.White,
            focusedContainerColor = AccentStrong,
            focusedContentColor = PlayerBackground,
            pressedContainerColor = AccentStrong,
            pressedContentColor = PlayerBackground,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(text)
        }
    }
}
private fun playerHtml(videoId: String): String {
    val safeId = videoId
        .replace("&", "")
        .replace(""", "")
        .replace("'", "")
        .replace("<", "")
        .replace(">", "")

    return """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
          <style>
            html, body {
              margin: 0;
              padding: 0;
              width: 100%;
              height: 100%;
              background: #000;
              overflow: hidden;
            }

            #player {
              display: block;
              width: 100%;
              height: 100%;
              border: 0;
            }
          </style>
        </head>
        <body>
          <iframe
            id="player"
            title="YouTube video"
            type="text/html"
            width="100%"
            height="100%"
            src="https://www.youtube.com/embed/$safeId?enablejsapi=1&autoplay=1&controls=1&playsinline=1&rel=0&fs=1&origin=https%3A%2F%2Ftv.publivoretube"
            frameborder="0"
            allow="autoplay; encrypted-media; picture-in-picture"
            allowfullscreen>
          </iframe>

          <script>
            var player = null;

            function onYouTubeIframeAPIReady() {
              window.ptBindPlayer();
            }

            window.ptBindPlayer = function() {
              if (player || !window.YT || !YT.Player) return;
              player = new YT.Player('player');
            };

            var tag = document.createElement('script');
            tag.src = 'https://www.youtube.com/iframe_api';
            document.head.appendChild(tag);

            window.ptPlay = function() {
              if (player) player.playVideo();
            };

            window.ptPause = function() {
              if (player) player.pauseVideo();
            };

            window.ptCaptions = function(enabled) {
              if (!player) return;
              if (enabled) {
                player.loadModule('captions');
                player.setOption(
                  'captions',
                  'track',
                  {'languageCode': 'en'}
                );
              } else {
                player.unloadModule('captions');
              }
            };
          </script>
        </body>
        </html>
    """.trimIndent()
}
