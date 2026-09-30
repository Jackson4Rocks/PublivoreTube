package tv.publivoretube

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
import tv.publivoretube.data.Video
import tv.publivoretube.data.VideoRepository
import tv.publivoretube.data.YoutubeDataApi
import tv.publivoretube.data.YoutubeVideoRepository
import tv.publivoretube.ui.HomeScreen
import tv.publivoretube.ui.PlaybackScreen
import tv.publivoretube.ui.theme.PublivoreTubeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository: VideoRepository by lazy {
        YoutubeVideoRepository(YoutubeDataApi(BuildConfig.YOUTUBE_API_KEY))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PublivoreTubeTheme {
                PublivoreTubeApp(repository = repository)
            }
        }
    }
}

@Composable
private fun PublivoreTubeApp(
    repository: VideoRepository,
) {
    var homeVideos by remember { mutableStateOf(emptyList<Video>()) }
    var searchResults by remember { mutableStateOf(emptyList<Video>()) }
    var homeLoading by remember { mutableStateOf(true) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var activeVideo by remember { mutableStateOf<Video?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(repository) {
        homeLoading = true
        homeVideos = repository.home()
        homeLoading = false

        statusMessage = when {
            !repository.isRemoteConfigured ->
                "Demo feed active — add YOUTUBE_API_KEY for live YouTube data."

            repository.lastError != null ->
                "YouTube API error: " + repository.lastError

            else -> null
        }
    }

    if (activeVideo != null) {
        PlaybackScreen(
            video = activeVideo!!,
            onBack = { activeVideo = null },
        )
    } else {
        HomeScreen(
        videos = homeVideos,
        searchResults = searchResults,
        searchQuery = searchQuery,
        homeLoading = homeLoading,
        searchLoading = searchLoading,
        statusMessage = statusMessage,
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
            onVideoSelected = { activeVideo = it },
        )
    }
}
