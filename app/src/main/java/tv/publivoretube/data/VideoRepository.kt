package tv.publivoretube.data

import android.util.Log

interface VideoRepository {
    suspend fun home(): List<Video>
    suspend fun search(query: String): List<Video>
    suspend fun shorts(): List<Video>
    suspend fun channelInfo(channelId: String): ChannelInfo
    suspend fun channelVideos(channelId: String): List<Video>
    val isRemoteConfigured: Boolean
    val lastError: String?
}

class YoutubeVideoRepository(
    private val api: YoutubeDataApi,
    private val fallback: DemoVideoRepository = DemoVideoRepository(),
) : VideoRepository {
    override val isRemoteConfigured: Boolean
        get() = api.isConfigured

    override var lastError: String? = null
        private set

    override suspend fun home(): List<Video> =
        runCatching { api.mostPopular() }
            .onSuccess { lastError = null }
            .getOrElse { error ->
                lastError = error.message ?: "Unknown YouTube API error."
                Log.e(TAG, "YouTube home request failed: " + lastError, error)
                fallback.homeStatic()
            }

    override suspend fun search(query: String): List<Video> =
        runCatching { api.search(query) }
            .onSuccess { lastError = null }
            .getOrElse { error ->
                lastError = error.message ?: "Unknown YouTube API error."
                Log.e(TAG, "YouTube search request failed: " + lastError, error)
                fallback.search(query)
            }

    
    override suspend fun shorts(): List<Video> =
        runCatching { api.shorts() }
            .onSuccess { lastError = null }
            .getOrElse { error ->
                lastError = error.message ?: "Unknown YouTube API error."
                Log.e(TAG, "YouTube Shorts request failed: " + lastError, error)
                emptyList()
            }

    override suspend fun channelInfo(channelId: String): ChannelInfo =
        api.channelInfo(channelId)

    override suspend fun channelVideos(channelId: String): List<Video> =
        api.channelVideos(channelId)

    private companion object {
        const val TAG = "PublivoreTubeAPI"
    }
}

class DemoVideoRepository : VideoRepository {
    override val isRemoteConfigured: Boolean = false
    override val lastError: String? = null

    override suspend fun home(): List<Video> = homeStatic()

    override suspend fun search(query: String): List<Video> = homeStatic().filter {
        it.title.contains(query, ignoreCase = true) ||
            it.channel.contains(query, ignoreCase = true)
    }

    override suspend fun shorts(): List<Video> = homeStatic().take(4).map {
        it.copy(duration = if (it.id.hashCode() % 2 == 0) "0:42" else "1:05")
    }

    override suspend fun channelInfo(channelId: String): ChannelInfo =
        ChannelInfo(channelId, "PublivoreTube Demo Channel", "Demo channel for offline mode.", null, "—", "6")

    override suspend fun channelVideos(channelId: String): List<Video> =
        homeStatic()

    fun homeStatic(): List<Video> = listOf(
        Video("demo-1", "PublivoreTube — First Look", "PublivoreTube", "08:42"),
        Video("demo-2", "Android TV UI Architecture", "PublivoreTube Labs", "14:18"),
        Video("demo-3", "Building a Leanback Experience", "PublivoreTube Labs", "11:07"),
        Video("demo-4", "Media3 Playback Foundations", "PublivoreTube Labs", "17:33"),
        Video("demo-5", "SponsorBlock Integration Plan", "PublivoreTube", "09:54"),
        Video("demo-6", "Remote-Friendly Navigation", "PublivoreTube Labs", "06:31"),
    )
}
