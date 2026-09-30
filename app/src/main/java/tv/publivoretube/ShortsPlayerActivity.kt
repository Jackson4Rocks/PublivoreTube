package tv.publivoretube

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class ShortsPlayerActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var titleView: TextView
    private lateinit var channelView: TextView
    private lateinit var positionView: TextView

    private val videoIds: List<String>
        get() = intent.getStringArrayListExtra(EXTRA_VIDEO_IDS).orEmpty()

    private val titles: List<String>
        get() = intent.getStringArrayListExtra(EXTRA_TITLES).orEmpty()

    private val channels: List<String>
        get() = intent.getStringArrayListExtra(EXTRA_CHANNELS).orEmpty()

    private val startIndex: Int
        get() = intent.getIntExtra(EXTRA_START_INDEX, 0)
            .coerceIn(0, (videoIds.size - 1).coerceAtLeast(0))

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (videoIds.isEmpty()) {
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 18, 28, 14)
            setBackgroundColor(Color.rgb(8, 7, 13))
        }

        titleView = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 20f
            maxLines = 2
        }

        channelView = TextView(this).apply {
            setTextColor(Color.rgb(185, 178, 197))
            textSize = 14f
            maxLines = 1
        }

        positionView = TextView(this).apply {
            setTextColor(Color.rgb(197, 155, 255))
            textSize = 13f
        }

        info.addView(titleView)
        info.addView(channelView)
        info.addView(positionView)

        webView = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = false
            isFocusableInTouchMode = false
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            addJavascriptInterface(PlayerBridge(), "PublivoreTube")
            loadDataWithBaseURL(
                "https://tv.publivoretube/",
                shortsPlayerHtml(videoIds, startIndex),
                "text/html",
                "UTF-8",
                "https://tv.publivoretube/",
            )
        }

        root.addView(
            info,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                104,
            ),
        )

        root.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
            ).apply { weight = 1f },
        )

        setContentView(root)
        updateInfo(startIndex)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event)
        }

        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                runPlayerCommand("ptNext")
                true
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                runPlayerCommand("ptPrevious")
                true
            }

            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                runPlayerCommand("ptToggle")
                true
            }

            KeyEvent.KEYCODE_BACK -> {
                finish()
                true
            }

            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun runPlayerCommand(name: String) {
        webView.evaluateJavascript(
            "window." + name + " && window." + name + "();",
            null,
        )
    }

    private fun updateInfo(index: Int) {
        val safeIndex = index.coerceIn(0, (videoIds.size - 1).coerceAtLeast(0))
        titleView.text = titles.getOrNull(safeIndex).orEmpty()
        channelView.text = channels.getOrNull(safeIndex).orEmpty()
        positionView.text = (safeIndex + 1).toString() + " / " + videoIds.size +
            "   •   ↑ previous   ↓ next   •   Center play/pause"
    }

    private fun shortsPlayerHtml(ids: List<String>, start: Int): String {
        val cleanIds = ids.map {
            it.filter { ch -> ch.isLetterOrDigit() || ch == '-' || ch == '_' }
        }.filter { it.isNotBlank() }

        val first = cleanIds.getOrNull(start) ?: cleanIds.first()
        val playlist = cleanIds.joinToString(",")

        return buildString {
            append("<!doctype html><html><head>")
            append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
            append("<style>html,body{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden;}#player{width:100%;height:100%;border:0;display:block;}</style>")
            append("</head><body>")
            append("<iframe id='player' width='100%' height='100%' ")
            append("src='https://www.youtube.com/embed/")
            append(first)
            append("?enablejsapi=1&autoplay=1&controls=1&playsinline=1&rel=0&fs=1&playlist=")
            append(playlist)
            append("&origin=https%3A%2F%2Ftv.publivoretube' ")
            append("frameborder='0' allow='autoplay; encrypted-media; picture-in-picture' allowfullscreen></iframe>")
            append("<script>")
            append("var player=null;")
            append("function onYouTubeIframeAPIReady(){")
            append("player=new YT.Player('player',{events:{")
            append("onReady:function(e){e.target.setVolume(100);e.target.unMute();e.target.loadPlaylist(")
            append("[").append(cleanIds.joinToString(",") { "'$it'" }).append("],").append(start).append(",0);")
            append("},")
            append("onStateChange:function(e){")
            append("if(e.data===0){return;} ")
            append("try{window.PublivoreTube.onPlaylistIndex(e.target.getPlaylistIndex());}catch(x){}")
            append("}")
            append("}});}")
            append("var tag=document.createElement('script');tag.src='https://www.youtube.com/iframe_api';document.head.appendChild(tag);")
            append("window.ptNext=function(){if(player)player.nextVideo();};")
            append("window.ptPrevious=function(){if(player)player.previousVideo();};")
            append("window.ptToggle=function(){if(!player)return;var s=player.getPlayerState();if(s===1){player.pauseVideo();}else{player.playVideo();}};")
            append("</script></body></html>")
        }
    }

    private inner class PlayerBridge {
        @JavascriptInterface
        fun onPlaylistIndex(index: Int) {
            runOnUiThread { updateInfo(index) }
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_VIDEO_IDS = "shorts_video_ids"
        const val EXTRA_TITLES = "shorts_titles"
        const val EXTRA_CHANNELS = "shorts_channels"
        const val EXTRA_START_INDEX = "shorts_start_index"
    }
}
