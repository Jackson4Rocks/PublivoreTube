package tv.publivoretube

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import tv.publivoretube.data.AppPreferences
import tv.publivoretube.data.SponsorBlockApi
import tv.publivoretube.data.SponsorSegment
import kotlin.concurrent.thread

class YouTubePlayerActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var statusText: TextView? = null

    private val videoId: String
        get() = intent.getStringExtra(EXTRA_VIDEO_ID).orEmpty()

    private val title: String
        get() = intent.getStringExtra(EXTRA_TITLE).orEmpty()

    private val channel: String
        get() = intent.getStringExtra(EXTRA_CHANNEL).orEmpty()

    private val channelId: String?
        get() = intent.getStringExtra(EXTRA_CHANNEL_ID)

    private val youtubeUrl: String
        get() = intent.getStringExtra(EXTRA_URL)
            ?: "https://www.youtube.com/watch?v=" + videoId

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.black)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 6, 11))
            setPadding(24, 18, 24, 18)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = tvButton("Back") {
            finish()
        }
        header.addView(
            back,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                56,
            ),
        )

        val titleBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 0, 0, 0)
        }

        titleBlock.addView(
            TextView(this).apply {
                text = title
                setTextColor(Color.WHITE)
                textSize = 20f
                maxLines = 2
            },
            LinearLayout.LayoutParams(0, 58).apply {
                weight = 1f
            },
        )

        titleBlock.addView(
            TextView(this).apply {
                text = channel
                setTextColor(Color.rgb(185, 178, 197))
                textSize = 14f
                maxLines = 1
            },
            LinearLayout.LayoutParams(0, 32).apply {
                weight = 1f
            },
        )

        header.addView(
            titleBlock,
            LinearLayout.LayoutParams(0, 76).apply {
                weight = 1f
            },
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                76,
            ),
        )

        webView = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = false
            isFocusableInTouchMode = false
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadsImagesAutomatically = true
            settings.allowContentAccess = true
            settings.allowFileAccess = false
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    updateStatus("YouTube player loaded.")
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    error: android.webkit.WebResourceError?,
                ) {
                    if (request?.isForMainFrame == true) {
                        updateStatus("YouTube player failed to load.")
                    }
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    errorResponse: android.webkit.WebResourceResponse?,
                ) {
                    if (request?.isForMainFrame == true &&
                        (errorResponse?.statusCode ?: 200) >= 400
                    ) {
                        updateStatus(
                            "YouTube returned HTTP " +
                                (errorResponse?.statusCode ?: 0) + ".",
                        )
                    }
                }
            }

            loadDataWithBaseURL(
                "https://tv.publivoretube/",
                playerHtml(videoId),
                "text/html",
                "UTF-8",
                "https://tv.publivoretube/",
            )
        }

        root.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
            ).apply {
                weight = 1f
                topMargin = 12
                bottomMargin = 12
            },
        )

        val actions = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        actionRow.addView(tvButton("Play") {
            runPlayerCommand("ptPlay")
        })
        actionRow.addView(tvButton("Pause") {
            runPlayerCommand("ptPause")
        })
        actionRow.addView(tvButton("Channel") {
            openChannel()
        })
        actionRow.addView(tvButton("Like") {
            signedInToast("Like")
        })
        actionRow.addView(tvButton("Dislike") {
            signedInToast("Dislike")
        })
        actionRow.addView(tvButton("Playlist") {
            signedInToast("Add to playlist")
        })
        actionRow.addView(tvButton("Captions") {
            runPlayerCommand("ptCaptions")
            updateStatus("Captions toggled. Use YouTube's language controls if needed.")
        })
        actionRow.addView(tvButton("SponsorBlock") {
            loadSponsorSegments()
        })
        actionRow.addView(tvButton("Settings") {
            showPlayerSettings()
        })
        actionRow.addView(tvButton("Share") {
            shareVideo()
        })
        actionRow.addView(tvButton("Open on YouTube") {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)))
        })
        actionRow.addView(tvButton("More") {
            Toast.makeText(
                this,
                "YouTube controls also provide quality, playback speed and fullscreen.",
                Toast.LENGTH_LONG,
            ).show()
        })

        actions.addView(actionRow)
        root.addView(
            actions,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                68,
            ),
        )

        statusText = TextView(this).apply {
            setTextColor(Color.rgb(185, 178, 197))
            textSize = 13f
            text = "Loading YouTube video…"
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 6, 8, 0)
        }
        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                42,
            ),
        )

        setContentView(root)
        back.requestFocus()
    }

    private fun runPlayerCommand(functionName: String) {
        webView.evaluateJavascript(
            "window." + functionName + " && window." + functionName + "();",
            null,
        )
    }

    private fun signedInToast(action: String) {
        val signedIn = getSharedPreferences("google_auth", MODE_PRIVATE)
            .getString("refresh_token", null)
            .isNullOrBlank()
            .not()

        if (!signedIn) {
            Toast.makeText(
                this,
                action + " needs a Google/YouTube account. Open Settings → Account.",
                Toast.LENGTH_LONG,
            ).show()
            return
        }

        Toast.makeText(
            this,
            action + " is ready for the authenticated YouTube account.",
            Toast.LENGTH_LONG,
        ).show()
    }

    private fun openChannel() {
        val id = channelId
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Channel information is unavailable.", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(
            Intent(this, ChannelActivity::class.java).apply {
                putExtra(ChannelActivity.EXTRA_CHANNEL_ID, id)
            },
        )
    }

    private fun shareVideo() {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, youtubeUrl)
                },
                "Share video",
            ),
        )
    }

    private fun showPlayerSettings() {
        val prefs = AppPreferences(this)
        val options = arrayOf(
            "Captions: " + if (prefs.captions) "On" else "Off",
            "Quality: " + prefs.quality,
            "Autoplay: " + if (prefs.autoplay) "On" else "Off",
            "Remember position: " + if (prefs.rememberPosition) "On" else "Off",
        )

        android.app.AlertDialog.Builder(this)
            .setTitle("Player settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        prefs.captions = !prefs.captions
                        runPlayerCommand("ptCaptions")
                    }
                    1 -> {
                        val qualities = arrayOf("Auto", "720p", "1080p")
                        android.app.AlertDialog.Builder(this)
                            .setTitle("Preferred quality")
                            .setItems(qualities) { _, index ->
                                prefs.quality = qualities[index]
                                updateStatus("Preferred quality set to " + prefs.quality + ".")
                            }
                            .show()
                    }
                    2 -> {
                        prefs.autoplay = !prefs.autoplay
                        updateStatus("Autoplay " + if (prefs.autoplay) "enabled." else "disabled.")
                    }
                    3 -> {
                        prefs.rememberPosition = !prefs.rememberPosition
                        updateStatus(
                            "Remember position " +
                                if (prefs.rememberPosition) "enabled." else "disabled.",
                        )
                    }
                }
            }
            .show()
    }

    private fun updateStatus(message: String) {
        statusText?.post {
            statusText?.text = message
        }
    }

    private fun loadSponsorSegments() {
        if (videoId.isBlank()) {
            Toast.makeText(this, "SponsorBlock is unavailable for this video.", Toast.LENGTH_SHORT).show()
            return
        }

        updateStatus("Checking SponsorBlock…")

        thread(name = "sponsorblock") {
            try {
                val segments = SponsorBlockApi().fetchSegments(videoId)
                runOnUiThread {
                    if (segments.isEmpty()) {
                        updateStatus("SponsorBlock: no submitted segments found.")
                    } else {
                        updateStatus(
                            "SponsorBlock: " + segments.size + " segment" +
                                if (segments.size == 1) "" else "s" + " found.",
                        )
                    }
                    showSponsorSegments(segments)
                }
            } catch (error: Throwable) {
                runOnUiThread {
                    updateStatus("SponsorBlock unavailable right now.")
                    Toast.makeText(
                        this,
                        "SponsorBlock could not be reached: " +
                            (error.message ?: "network error"),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun showSponsorSegments(segments: List<SponsorSegment>) {
        if (segments.isEmpty()) {
            android.app.AlertDialog.Builder(this)
                .setTitle("SponsorBlock")
                .setMessage(
                    "No submitted SponsorBlock segments were found for this video.\n\n" +
                        "SponsorBlock data is provided by sponsor.ajay.app.",
                )
                .setPositiveButton("Close", null)
                .show()
            return
        }

        val labels = segments.mapIndexed { index, segment ->
            val range =
                formatTimestamp(segment.startSeconds) + " → " +
                    formatTimestamp(segment.endSeconds)
            val category = segment.category
                .replace('_', ' ')
                .replaceFirstChar { it.uppercase() }

            (index + 1).toString() + ". " + category + "  •  " + range
        }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("SponsorBlock • " + segments.size + " segments")
            .setItems(labels, null)
            .setPositiveButton("Close", null)
            .setMessage(
                "These are crowdsourced segment markers. " +
                    "PublivoreTube does not alter the embedded YouTube player.",
            )
            .show()
    }

    private fun formatTimestamp(seconds: Double): String {
        val total = seconds.toLong().coerceAtLeast(0L)
        val hours = total / 3_600
        val minutes = (total % 3_600) / 60
        val secs = total % 60

        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, secs)
        } else {
            "%d:%02d".format(minutes, secs)
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.destroy()
        super.onDestroy()
    }

    private fun tvButton(text: String, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.rgb(37, 32, 47))
            }
            setPadding(22, 0, 22, 0)
            minWidth = 0
            minHeight = 52
            setOnClickListener { onClick() }
            setOnFocusChangeListener { view, hasFocus ->
                view.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 100f
                    setColor(
                        if (hasFocus) Color.rgb(197, 155, 255) else Color.rgb(37, 32, 47),
                    )
                }
                setTextColor(if (hasFocus) Color.rgb(7, 6, 11) else Color.WHITE)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                54,
            ).apply {
                rightMargin = 8
            }
        }

    private fun playerHtml(id: String): String {
        val safeId = id.filter { it.isLetterOrDigit() || it == '-' || it == '_' }

        return buildString {
            append("<!doctype html><html><head>")
            append("<meta name='viewport' content='width=device-width,initial-scale=1.0'>")
            append("<style>")
            append("html,body{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden;}")
            append("#player{width:100%;height:100%;border:0;display:block;}")
            append("</style></head><body>")
            append("<iframe id='player' width='100%' height='100%' ")
            append("src='https://www.youtube.com/embed/")
            append(safeId)
            append("?enablejsapi=1&autoplay=1&controls=1&playsinline=1&rel=0&fs=1")
            append("&origin=https%3A%2F%2Ftv.publivoretube' ")
            append("frameborder='0' allow='autoplay; encrypted-media; picture-in-picture' allowfullscreen></iframe>")
            append("<script>")
            append("var player=null;")
            append("function onYouTubeIframeAPIReady(){if(!player){player=new YT.Player('player',{events:{onReady:function(e){e.target.setVolume(100);e.target.unMute();}}});}}")
            append("var tag=document.createElement('script');")
            append("tag.src='https://www.youtube.com/iframe_api';")
            append("document.head.appendChild(tag);")
            append("window.ptPlay=function(){if(player)player.playVideo();};")
            append("window.ptPause=function(){if(player)player.pauseVideo();};")
            append("window.ptCaptions=function(){if(player)player.loadModule('captions');};")
            append("</script></body></html>")
        }
    }

    companion object {
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CHANNEL = "channel"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_URL = "youtube_url"
    }
}
