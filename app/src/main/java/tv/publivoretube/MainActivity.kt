package tv.publivoretube

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import tv.publivoretube.data.AppPreferences
import tv.publivoretube.data.GoogleAccount
import tv.publivoretube.data.GoogleAuthManager
import tv.publivoretube.data.Subscription
import tv.publivoretube.data.Video
import tv.publivoretube.data.VideoRepository
import tv.publivoretube.data.WatchHistoryStore
import tv.publivoretube.data.YouTubeAccountApi
import tv.publivoretube.data.YoutubeDataApi
import tv.publivoretube.data.YoutubeVideoRepository
import tv.publivoretube.ui.HomeScreen
import tv.publivoretube.ui.theme.PublivoreTubeTheme

class MainActivity : ComponentActivity() {
    private val repository: VideoRepository by lazy {
        YoutubeVideoRepository(YoutubeDataApi(BuildConfig.YOUTUBE_API_KEY))
    }

    private val authManager: GoogleAuthManager by lazy {
        GoogleAuthManager(this, BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
    }

    private val historyStore: WatchHistoryStore by lazy {
        WatchHistoryStore(this)
    }

    private val appPreferences: AppPreferences by lazy {
        AppPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PublivoreTubeTheme {
                PublivoreTubeApp(
                    repository = repository,
                    authManager = authManager,
                    historyStore = historyStore,
                    appPreferences = appPreferences,
                )
            }
        }
    }
}

@Composable
private fun PublivoreTubeApp(
    repository: VideoRepository,
    authManager: GoogleAuthManager,
    historyStore: WatchHistoryStore,
    appPreferences: AppPreferences,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var homeVideos by remember { mutableStateOf(emptyList<Video>()) }
    var searchResults by remember { mutableStateOf(emptyList<Video>()) }
    var historyVideos by remember { mutableStateOf(historyStore.load()) }
    var subscriptions by remember { mutableStateOf(emptyList<Subscription>()) }
    var shortsVideos by remember { mutableStateOf(emptyList<Video>()) }

    var homeLoading by remember { mutableStateOf(true) }
    var shortsLoading by remember { mutableStateOf(false) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var signedIn by remember { mutableStateOf(authManager.isSignedIn) }
    var account by remember { mutableStateOf<GoogleAccount?>(authManager.account) }
    var authBusy by remember { mutableStateOf(false) }
    var authCode by remember { mutableStateOf<String?>(null) }
    var authUrl by remember { mutableStateOf<String?>(null) }

    var autoplay by remember { mutableStateOf(appPreferences.autoplay) }
    var captions by remember { mutableStateOf(appPreferences.captions) }
    var rememberPosition by remember { mutableStateOf(appPreferences.rememberPosition) }
    var showThumbnails by remember { mutableStateOf(appPreferences.showThumbnails) }
    var reduceAnimations by remember { mutableStateOf(appPreferences.reduceAnimations) }
    var highContrast by remember { mutableStateOf(appPreferences.highContrast) }
    var quality by remember { mutableStateOf(appPreferences.quality) }

    fun openVideo(video: Video) {
        historyStore.add(video)
        historyVideos = historyStore.load()

        context.startActivity(
            Intent(context, YouTubePlayerActivity::class.java).apply {
                putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, video.id)
                putExtra(YouTubePlayerActivity.EXTRA_TITLE, video.title)
                putExtra(YouTubePlayerActivity.EXTRA_CHANNEL, video.channel)
                putExtra(YouTubePlayerActivity.EXTRA_CHANNEL_ID, video.channelId)
                putExtra(YouTubePlayerActivity.EXTRA_URL, video.youtubeUrl)
            },
        )
    }

    suspend fun refreshSubscriptions() {
        val token = authManager.accessToken()
        if (token.isNullOrBlank()) {
            subscriptions = emptyList()
            return
        }

        subscriptions = runCatching {
            YouTubeAccountApi(token).subscriptions()
        }.getOrElse {
            statusMessage = "YouTube subscription error: " + (it.message ?: "Unknown error")
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        homeLoading = true
        homeVideos = repository.home()
        homeLoading = false

        if (repository.isRemoteConfigured) {
            shortsLoading = true
            shortsVideos = repository.shorts()
            shortsLoading = false
        } else {
            shortsVideos = repository.shorts()
        }

        statusMessage = when {
            !repository.isRemoteConfigured ->
                "Demo feed active — add YOUTUBE_API_KEY for live YouTube data."

            repository.lastError != null ->
                "YouTube API error: " + repository.lastError

            else -> null
        }

        if (signedIn) {
            refreshSubscriptions()
        }
    }

    HomeScreen(
        videos = homeVideos,
        searchResults = searchResults,
        searchQuery = searchQuery,
        homeLoading = homeLoading,
        searchLoading = searchLoading,
        statusMessage = statusMessage,
        historyVideos = historyVideos,
        subscriptions = subscriptions,
        shortsVideos = shortsVideos,
        shortsLoading = shortsLoading,
        signedIn = signedIn,
        account = account,
        oauthConfigured = authManager.isConfigured,
        authBusy = authBusy,
        authCode = authCode,
        authUrl = authUrl,
        autoplay = autoplay,
        captions = captions,
        rememberPosition = rememberPosition,
        showThumbnails = showThumbnails,
        reduceAnimations = reduceAnimations,
        highContrast = highContrast,
        quality = quality,
        onSearchQueryChange = { searchQuery = it },
        onSearch = { query ->
            searchQuery = query.trim()

            if (searchQuery.isBlank()) {
                searchResults = emptyList()
                return@HomeScreen
            }

            scope.launch {
                searchLoading = true
                searchResults = repository.search(searchQuery)
                statusMessage = repository.lastError?.let {
                    "YouTube API error: " + it
                }
                searchLoading = false
            }
        },
        onVideoSelected = ::openVideo,
        onShortSelected = { shorts, startIndex ->
            context.startActivity(
                Intent(context, ShortsPlayerActivity::class.java).apply {
                    putStringArrayListExtra(
                        ShortsPlayerActivity.EXTRA_VIDEO_IDS,
                        ArrayList(shorts.map { it.id }),
                    )
                    putStringArrayListExtra(
                        ShortsPlayerActivity.EXTRA_TITLES,
                        ArrayList(shorts.map { it.title }),
                    )
                    putStringArrayListExtra(
                        ShortsPlayerActivity.EXTRA_CHANNELS,
                        ArrayList(shorts.map { it.channel }),
                    )
                    putExtra(ShortsPlayerActivity.EXTRA_START_INDEX, startIndex)
                },
            )
        },
        onSignIn = {
            if (!authManager.isConfigured) {
                statusMessage =
                    "Google sign-in is not configured. Add GOOGLE_OAUTH_CLIENT_ID to local.properties."
                return@HomeScreen
            }

            if (authBusy) return@HomeScreen

            scope.launch {
                authBusy = true
                statusMessage = null
                try {
                    val device = authManager.requestDeviceAuth()
                    authCode = device.userCode
                    authUrl = device.verificationUrl

                    val signedAccount = authManager.completeDeviceAuth(device.intervalSeconds)
                    signedIn = true
                    account = signedAccount
                    authCode = null
                    authUrl = null
                    refreshSubscriptions()
                    statusMessage = "Google account connected."
                } catch (error: Throwable) {
                    statusMessage = error.message ?: "Google sign-in failed."
                } finally {
                    authBusy = false
                }
            }
        },
        onSignOut = {
            authManager.signOut()
            signedIn = false
            account = null
            subscriptions = emptyList()
            statusMessage = "Google account disconnected. Sign in again to load subscriptions and account features."
        },
        onClearHistory = {
            historyStore.clear()
            historyVideos = emptyList()
            statusMessage = "Local watch history cleared."
        },
        onAutoplayChange = {
            autoplay = it
            appPreferences.autoplay = it
        },
        onCaptionsChange = {
            captions = it
            appPreferences.captions = it
        },
        onRememberPositionChange = {
            rememberPosition = it
            appPreferences.rememberPosition = it
        },
        onThumbnailsChange = {
            showThumbnails = it
            appPreferences.showThumbnails = it
        },
        onReduceAnimationsChange = {
            reduceAnimations = it
            appPreferences.reduceAnimations = it
        },
        onHighContrastChange = {
            highContrast = it
            appPreferences.highContrast = it
        },
        onQualityChange = {
            quality = it
            appPreferences.quality = it
        },
    )
}
