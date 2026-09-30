package tv.publivoretube.ui

import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
private val Accent = Color(0xFFE7DAFF)
private val AccentStrong = Color(0xFFC59BFF)
private val TextMuted = Color(0xFFB9B2C5)
private val Pill = RoundedCornerShape(50.dp)
private val PlayerShape = RoundedCornerShape(28.dp)

@Composable
fun YouTubePlayerScreen(
    video: Video,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val videoId = video.id
    val webView = remember(videoId) {
        WebView(context).apply {
            setBackgroundColor(AndroidColor.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadsImagesAutomatically = true
            settings.userAgentString =
                settings.userAgentString + " PublivoreTube/0.1 AndroidTV"

            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            loadDataWithBaseURL(
                "https://www.youtube.com/",
                playerHtml(videoId),
                "text/html",
                "UTF-8",
                null,
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = PlayerBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 36.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onBack,
                    shape = ButtonDefaults.shape(Pill),
                    colors = ButtonDefaults.colors(
                        containerColor = Color(0xFF24202D),
                        contentColor = Color.White,
                        focusedContainerColor = AccentStrong,
                        focusedContentColor = PlayerBackground,
                        pressedContainerColor = AccentStrong,
                        pressedContentColor = PlayerBackground,
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 11.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(21.dp),
                        )
                        Text("Back")
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
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
                        .padding(10.dp)
                        .clip(PlayerShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { webView },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(22.dp)),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = AccentStrong,
                )
                Text(
                    text = "Playing through YouTube's official embedded player",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
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
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
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
              width: 100%;
              height: 100%;
            }
          </style>
        </head>
        <body>
          <div id="player"></div>

          <script>
            var tag = document.createElement('script');
            tag.src = 'https://www.youtube.com/iframe_api';
            document.head.appendChild(tag);

            function onYouTubeIframeAPIReady() {
              new YT.Player('player', {
                width: '100%',
                height: '100%',
                videoId: '$safeId',
                playerVars: {
                  autoplay: 1,
                  controls: 1,
                  enablejsapi: 1,
                  fs: 1,
                  playsinline: 1,
                  rel: 0
                }
              });
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
